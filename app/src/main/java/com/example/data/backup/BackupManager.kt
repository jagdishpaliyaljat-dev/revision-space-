package com.example.data.backup

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.example.data.local.AppDatabase
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import java.io.FileNotFoundException
import java.io.IOException

/** Which strategy was used when restoring a backup file. */
enum class ImportMode {
    /** Keep current data, update rows with the same id and add new ones (default). */
    MERGE,

    /** Delete everything on this device first, then restore exactly the file contents. */
    REPLACE
}

/** What actually happened during an import, used for the result dialog. */
data class ImportOutcome(
    val importedRows: Map<String, Int>,
    val skippedTables: List<String>,
    val droppedColumns: Int,
    val failedRows: Int,
    val clearedRows: Int,
    val prefsApplied: Int
) {
    val totalImported: Int get() = importedRows.values.sum()
}

/**
 * Reads and writes the local Room database as a single portable JSON file.
 *
 * Why raw SQLite instead of DAOs? A generic table dump/restore automatically
 * covers every table (now and in the future), keeps empty and archived rows, and
 * cannot silently forget a column. Room still owns the database — we only use
 * its open helper, and every write happens inside one SQLite transaction.
 */
object BackupManager {

    /** Same preferences file used by the Settings screen. */
    const val PREFS_NAME = "revision_app_prefs"

    /** 25 MB is far beyond a realistic study backup but protects the phone. */
    const val MAX_FILE_BYTES = 25 * 1024 * 1024

    private const val BLOB_PREFIX = "base64:"
    private val TABLE_NAME = Regex("^[A-Za-z_][A-Za-z0-9_]*$")

    // ---------------------------------------------------------------- export

    /** Builds the full backup JSON: every app table + user preferences. */
    fun createBackup(context: Context, db: AppDatabase, appVersion: String): String {
        val readable = db.openHelper.readableDatabase
        val tables = LinkedHashMap<String, List<Map<String, JsonElement>>>()
        appTableNames(readable).forEach { table ->
            tables[table] = readTable(readable, table)
        }
        return BackupFormat.encode(
            createdAtMs = System.currentTimeMillis(),
            appVersion = appVersion,
            tables = tables,
            prefs = readPrefs(context)
        )
    }

    /** Writes the already generated JSON to the Uri the user picked. */
    fun writeBackupFile(context: Context, uri: Uri, content: String) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val stream = try {
            context.contentResolver.openOutputStream(uri, "wt")
                ?: context.contentResolver.openOutputStream(uri)
        } catch (e: FileNotFoundException) {
            null
        } ?: throw IOException("Couldn't open the selected file for writing.")

        stream.use { out ->
            out.write(bytes)
            out.flush()
        }
    }

    /** Reads the picked file with a hard size cap and a UTF-8 decode. */
    fun readBackupFile(context: Context, uri: Uri): String {
        val stream = try {
            context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            null
        } ?: throw IOException("Couldn't open the selected file.")

        stream.use { input ->
            val buffer = ByteArray(16 * 1024)
            val out = java.io.ByteArrayOutputStream()
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                total += read
                if (total > MAX_FILE_BYTES) {
                    throw BackupFormatException("This file is too large to be a Revision Space backup.")
                }
                out.write(buffer, 0, read)
            }
            return out.toByteArray().toString(Charsets.UTF_8)
        }
    }

    /** Best-effort human readable name of the picked document. */
    fun displayName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    // ---------------------------------------------------------------- import

    /**
     * Restores [payload] into the local database.
     *
     * Runs on the caller's thread (use Dispatchers.IO) and performs all writes in
     * a single transaction, so a failure can never leave a half-imported state.
     */
    fun importBackup(
        context: Context,
        db: AppDatabase,
        payload: BackupPayload,
        mode: ImportMode
    ): ImportOutcome {
        val writable = db.openHelper.writableDatabase
        val localTables = appTableNames(writable)
        val skippedTables = payload.tables.keys.filter { it !in localTables }.sorted()

        val imported = LinkedHashMap<String, Int>()
        var droppedColumns = 0
        var failedRows = 0
        var clearedRows = 0

        writable.beginTransaction()
        try {
            if (mode == ImportMode.REPLACE) {
                // Clear everything we know about, not only the tables present in
                // the file, so a restore really is a restore.
                localTables.forEach { table ->
                    clearedRows += runCatching { writable.delete(table, null, null) }.getOrDefault(0)
                }
            }

            payload.tables.forEach { (table, rows) ->
                if (table !in localTables) return@forEach
                val localColumns = columnsOf(writable, table)
                var ok = 0
                rows.forEach { row ->
                    val values = ContentValues()
                    var rowDropped = 0
                    row.forEach { (column, cell) ->
                        if (column in localColumns) {
                            putCell(values, column, cell)
                        } else {
                            rowDropped++
                        }
                    }
                    droppedColumns += rowDropped
                    if (values.size() == 0) return@forEach

                    val result = runCatching {
                        writable.insertWithOnConflict(
                            table,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE
                        )
                    }
                    if (result.getOrNull() != null && result.getOrNull() != -1L) {
                        ok++
                    } else {
                        failedRows++
                    }
                }
                imported[table] = ok
            }

            writable.setTransactionSuccessful()
        } finally {
            writable.endTransaction()
        }

        val prefsApplied = applyPrefs(context, payload.prefs)

        return ImportOutcome(
            importedRows = imported,
            skippedTables = skippedTables,
            droppedColumns = droppedColumns,
            failedRows = failedRows,
            clearedRows = clearedRows,
            prefsApplied = prefsApplied
        )
    }

    // ------------------------------------------------------------- internals

    private fun appTableNames(db: SQLiteDatabase): List<String> {
        val names = ArrayList<String>()
        db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type = 'table' " +
                "AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'room_%' " +
                "AND name NOT LIKE 'android_%'",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(0) ?: continue
                if (TABLE_NAME.matches(name)) names += name
            }
        }
        return names.sortedBy { BackupFormat.orderOf(it) }
    }

    private fun columnsOf(db: SQLiteDatabase, table: String): Set<String> {
        val columns = HashSet<String>()
        db.rawQuery("PRAGMA table_info(`$table`)", null).use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex >= 0) cursor.getString(nameIndex)?.let { columns += it }
            }
        }
        return columns
    }

    private fun readTable(
        db: SQLiteDatabase,
        table: String
    ): List<Map<String, JsonElement>> {
        val rows = ArrayList<Map<String, JsonElement>>()
        db.rawQuery("SELECT * FROM `$table`", null).use { cursor ->
            val columns = cursor.columnNames
            while (cursor.moveToNext()) {
                val row = LinkedHashMap<String, JsonElement>(columns.size)
                for (index in columns.indices) {
                    row[columns[index]] = when (cursor.getType(index)) {
                        Cursor.FIELD_TYPE_NULL -> JsonNull
                        Cursor.FIELD_TYPE_INTEGER -> JsonPrimitive(cursor.getLong(index))
                        Cursor.FIELD_TYPE_FLOAT -> JsonPrimitive(cursor.getDouble(index))
                        Cursor.FIELD_TYPE_BLOB -> JsonPrimitive(
                            BLOB_PREFIX + Base64.encodeToString(
                                cursor.getBlob(index),
                                Base64.NO_WRAP
                            )
                        )
                        else -> JsonPrimitive(cursor.getString(index) ?: "")
                    }
                }
                rows += row
            }
        }
        return rows
    }

    private fun readPrefs(context: Context): Map<String, JsonElement> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val result = LinkedHashMap<String, JsonElement>()
        prefs.all.forEach { (key, value) ->
            result[key] = when (value) {
                null -> JsonNull
                is Boolean -> JsonPrimitive(value)
                is Int, is Long -> JsonPrimitive((value as Number).toLong())
                is Float -> JsonPrimitive(value.toDouble())
                is Double -> JsonPrimitive(value)
                is String -> JsonPrimitive(value)
                is Set<*> -> buildJsonArray {
                    value.forEach { item -> if (item is String) add(JsonPrimitive(item)) }
                }
                else -> JsonPrimitive(value.toString())
            }
        }
        return result
    }

    private fun putCell(values: ContentValues, column: String, cell: JsonElement) {
        when {
            cell is JsonNull -> values.putNull(column)
            cell !is JsonPrimitive -> values.putNull(column)
            cell.isString -> {
                val text = cell.content
                if (text.startsWith(BLOB_PREFIX)) {
                    val decoded = runCatching {
                        Base64.decode(text.removePrefix(BLOB_PREFIX), Base64.NO_WRAP)
                    }.getOrNull()
                    if (decoded != null) values.put(column, decoded) else values.put(column, text)
                } else {
                    values.put(column, text)
                }
            }
            cell.booleanOrNull != null -> values.put(column, if (cell.boolean) 1L else 0L)
            cell.longOrNull != null -> values.put(column, cell.long)
            cell.doubleOrNull != null -> values.put(column, cell.double)
            else -> values.put(column, cell.content)
        }
    }

    private fun applyPrefs(context: Context, prefs: Map<String, JsonElement>): Int {
        if (prefs.isEmpty()) return 0
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        var applied = 0
        prefs.forEach { (key, value) ->
            when {
                value is JsonNull -> {
                    editor.remove(key); applied++
                }
                value is JsonArray -> {
                    val items = value.mapNotNull { element ->
                        (element as? JsonPrimitive)?.takeIf { it.isString }?.content
                    }.toSet()
                    editor.putStringSet(key, items); applied++
                }
                value is JsonPrimitive && value.isString -> {
                    editor.putString(key, value.content); applied++
                }
                value is JsonPrimitive && value.booleanOrNull != null -> {
                    editor.putBoolean(key, value.boolean); applied++
                }
                value is JsonPrimitive && value.longOrNull != null -> {
                    val longValue = value.long
                    if (longValue in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                        editor.putInt(key, longValue.toInt())
                    } else {
                        editor.putLong(key, longValue)
                    }
                    applied++
                }
                value is JsonPrimitive && value.doubleOrNull != null -> {
                    editor.putFloat(key, value.double.toFloat()); applied++
                }
            }
        }
        editor.commit()
        return applied
    }
}
