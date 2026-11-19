package com.example.data.backup

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Thrown for every user-facing problem with a backup file. */
class BackupFormatException(message: String) : IllegalArgumentException(message)

/**
 * A validated, in-memory representation of a Revision Space backup file.
 *
 * Rows are stored as plain JSON values (no typed DTOs) so that the backup keeps
 * working when the database gains or loses columns in a future release: unknown
 * columns are simply ignored on import and missing columns fall back to their
 * SQLite defaults.
 */
data class BackupPayload(
    val createdAtMs: Long,
    val appVersion: String,
    val tables: Map<String, List<Map<String, JsonElement>>>,
    val prefs: Map<String, JsonElement>
) {
    val totalRows: Int get() = tables.values.sumOf { it.size }

    /** Row counts in a stable, UI friendly order. */
    fun rowCounts(): Map<String, Int> =
        tables.entries
            .sortedBy { BackupFormat.orderOf(it.key) }
            .associate { it.key to it.value.size }
}

/**
 * The on-disk JSON contract for "Export Data" / "Import Data".
 *
 * Everything in this object is pure Kotlin (no Android classes) so it can be
 * unit-tested on the JVM and reused by any future client.
 */
object BackupFormat {
    /** Written into every file so unrelated JSON can never be imported by mistake. */
    const val MAGIC = "revision-space-backup"

    /** Current schema version of the backup envelope (bump when the shape changes). */
    const val VERSION = 1

    const val MIME_TYPE = "application/json"

    /** Safety limits so a malicious or corrupted file cannot exhaust memory. */
    const val MAX_TABLES = 100
    const val MAX_ROWS = 200_000

    private val TABLE_NAME = Regex("^[A-Za-z_][A-Za-z0-9_]*$")

    /** Preferred display order (unknown tables go last, alphabetically). */
    private val ORDER = listOf(
        "user_profiles",
        "subjects",
        "chapters",
        "topics",
        "revisions",
        "revision_history",
        "study_logs",
        "exams",
        "subject_tests",
        "quick_tasks",
        "coin_transactions",
        "app_notifications",
        "offline_notes"
    )

    private val LABELS = mapOf(
        "user_profiles" to "Profile",
        "subjects" to "Subjects",
        "chapters" to "Chapters",
        "topics" to "Topics",
        "revisions" to "Revision schedule",
        "revision_history" to "Revision history",
        "study_logs" to "Study logs",
        "exams" to "Exams",
        "subject_tests" to "Test scores",
        "quick_tasks" to "To-do tasks",
        "coin_transactions" to "Coin history",
        "app_notifications" to "Notifications",
        "offline_notes" to "Offline notes"
    )

    fun labelFor(table: String): String =
        LABELS[table] ?: table.replace('_', ' ').replaceFirstChar { it.uppercase() }

    fun orderOf(table: String): Int = ORDER.indexOf(table).let { if (it < 0) ORDER.size else it }

    /** Default file name, e.g. `RevisionSpace-Backup-2026-11-19_1745.json`. */
    fun fileName(now: Long = System.currentTimeMillis()): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date(now))
        return "RevisionSpace-Backup-$stamp.json"
    }

    /** Prefix of the automatic copies taken right before an import. */
    const val SAFETY_PREFIX = "RevisionSpace-Safety-"

    /** How many automatic pre-import copies are kept on the device. */
    const val SAFETY_KEEP = 3

    /**
     * Name of the automatic pre-import safety copy.
     *
     * Seconds (not minutes) are used so two imports inside the same minute never
     * overwrite each other's safety copy.
     */
    fun safetyFileName(now: Long = System.currentTimeMillis()): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date(now))
        return "$SAFETY_PREFIX$stamp.json"
    }

    /**
     * Picks the safety copies that can be deleted so that at most [keep] newest
     * copies remain. [entries] are `(fileName, modifiedAtMs)` pairs.
     *
     * Pure Kotlin so the pruning rule is unit-tested without touching the disk.
     */
    fun snapshotsToDelete(entries: List<Pair<String, Long>>, keep: Int): List<String> {
        if (keep < 0) return entries.map { it.first }
        if (entries.size <= keep) return emptyList()
        return entries
            .sortedWith(
                compareByDescending<Pair<String, Long>> { it.second }
                    .thenByDescending { it.first }
            )
            .drop(keep)
            .map { it.first }
    }

    /** Serializable envelope (kept separate from [BackupPayload] for clarity). */
    fun encode(
        createdAtMs: Long,
        appVersion: String,
        tables: Map<String, List<Map<String, JsonElement>>>,
        prefs: Map<String, JsonElement>
    ): String {
        require(tables.size <= MAX_TABLES) { "Too many tables to export" }
        require(tables.values.sumOf { it.size } <= MAX_ROWS) { "Too many rows to export" }

        val root = buildJsonObject {
            put("format", MAGIC)
            put("version", VERSION)
            put("createdAtMs", createdAtMs)
            put("appVersion", appVersion)
            put("totalRows", tables.values.sumOf { it.size })
            put("counts", buildJsonObject {
                tables.forEach { (table, rows) -> put(table, rows.size) }
            })
            put("tables", buildJsonObject {
                tables.forEach { (table, rows) ->
                    put(table, buildJsonArray {
                        rows.forEach { row -> add(JsonObject(row)) }
                    })
                }
            })
            put("prefs", JsonObject(prefs))
        }
        return root.toString()
    }

    /**
     * Parses and validates a backup file.
     *
     * @throws BackupFormatException with a message that is safe to show to the user.
     */
    fun decode(raw: String): BackupPayload {
        if (raw.isBlank()) {
            throw BackupFormatException("This file is empty.")
        }

        val rootElement = try {
            Json.parseToJsonElement(raw)
        } catch (e: Exception) {
            throw BackupFormatException("This is not a valid backup file (unreadable JSON).")
        }
        val root = rootElement as? JsonObject
            ?: throw BackupFormatException("This is not a valid backup file.")

        val magic = (root["format"] as? JsonPrimitive)?.takeIf { it.isString }?.content
        if (magic != MAGIC) {
            throw BackupFormatException("This file was not created by Revision Space.")
        }

        val version = (root["version"] as? JsonPrimitive)?.intOrNull
            ?: throw BackupFormatException("The backup file is missing its version.")
        if (version < 1) {
            throw BackupFormatException("The backup file has an invalid version.")
        }
        if (version > VERSION) {
            throw BackupFormatException(
                "This backup was created by a newer version of Revision Space. Please update the app and try again."
            )
        }

        val tablesElement = root["tables"] as? JsonObject
            ?: throw BackupFormatException("The backup file contains no data.")
        if (tablesElement.isEmpty()) {
            throw BackupFormatException("The backup file contains no data.")
        }
        if (tablesElement.size > MAX_TABLES) {
            throw BackupFormatException("The backup file contains too many tables.")
        }

        var runningTotal = 0
        val tables = LinkedHashMap<String, List<Map<String, JsonElement>>>()
        tablesElement.forEach { (table, value) ->
            if (!TABLE_NAME.matches(table)) {
                throw BackupFormatException("The backup file has an invalid table name: $table")
            }
            val rowsArray = value as? JsonArray
                ?: throw BackupFormatException("The table \"$table\" is malformed.")
            runningTotal += rowsArray.size
            if (runningTotal > MAX_ROWS) {
                throw BackupFormatException("The backup file contains too many records.")
            }
            val rows = ArrayList<Map<String, JsonElement>>(rowsArray.size)
            rowsArray.forEachIndexed { index, element ->
                val row = element as? JsonObject
                    ?: throw BackupFormatException("Row ${index + 1} of \"$table\" is malformed.")
                row.forEach { (column, cell) ->
                    if (cell is JsonObject || cell is JsonArray) {
                        throw BackupFormatException(
                            "Column \"$column\" of \"$table\" has an unsupported value."
                        )
                    }
                }
                rows += row
            }
            tables[table] = rows
        }

        val prefsElement = root["prefs"]
        val prefs = LinkedHashMap<String, JsonElement>()
        if (prefsElement != null && prefsElement !is JsonNull) {
            val prefsObject = prefsElement as? JsonObject
                ?: throw BackupFormatException("The settings section of the backup is malformed.")
            prefsObject.forEach { (key, value) ->
                when {
                    value is JsonNull || value is JsonPrimitive -> prefs[key] = value
                    value is JsonArray -> {
                        val allStrings = value.all { it is JsonPrimitive && it.isString }
                        if (!allStrings) {
                            throw BackupFormatException("The setting \"$key\" has an unsupported value.")
                        }
                        prefs[key] = value
                    }
                    else -> throw BackupFormatException("The setting \"$key\" has an unsupported value.")
                }
            }
        }

        val createdAtMs = (root["createdAtMs"] as? JsonPrimitive)?.let { it.longOrNull } ?: 0L
        val appVersion = (root["appVersion"] as? JsonPrimitive)
            ?.takeIf { it.isString }
            ?.content
            ?.takeIf { it.isNotBlank() }
            ?: "unknown"

        return BackupPayload(
            createdAtMs = createdAtMs,
            appVersion = appVersion,
            tables = tables,
            prefs = prefs
        )
    }
}
