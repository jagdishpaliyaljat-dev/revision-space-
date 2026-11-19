package com.example.data.backup

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.long
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Pure JVM tests for the backup file contract.
 *
 * These cover the risky part of Export/Import: parsing an arbitrary file from
 * the file picker and deciding whether it is safe to restore. The SQLite side
 * is exercised by [BackupManager] and covered by the manual test plan.
 */
class BackupFormatTest {

    private fun sampleTables(): Map<String, List<Map<String, JsonElement>>> = linkedMapOf(
        "subjects" to listOf(
            linkedMapOf(
                "id" to JsonPrimitive("sub-1"),
                "userId" to JsonPrimitive("offline_user"),
                "name" to JsonPrimitive("भौतिक विज्ञान 🎓"),
                "colorHex" to JsonPrimitive("#7C3AED"),
                "totalTopicsExpected" to JsonPrimitive(12L),
                "isArchived" to JsonPrimitive(0L)
            )
        ),
        "topics" to listOf(
            linkedMapOf(
                "id" to JsonPrimitive("topic-1"),
                "subjectId" to JsonPrimitive("sub-1"),
                "name" to JsonPrimitive("Newton's Laws"),
                "masteryScore" to JsonPrimitive(85L),
                "estimatedRevisionTimeMins" to JsonPrimitive(30L),
                "nextRevisionDateMs" to JsonPrimitive(1_760_000_000_000L),
                "notes" to JsonPrimitive(""),
                "scoreObtained" to JsonNull
            ),
            linkedMapOf(
                "id" to JsonPrimitive("topic-2"),
                "subjectId" to JsonPrimitive("sub-1"),
                "name" to JsonPrimitive("Friction"),
                "progress" to JsonPrimitive(0.75),
                "notes" to JsonPrimitive("Line 1\nLine 2, with \"quotes\" and a comma,")
            )
        ),
        "study_logs" to emptyList()
    )

    private fun samplePrefs(): Map<String, JsonElement> = linkedMapOf(
        "dark_mode_enabled" to JsonPrimitive(true),
        "notifications_enabled" to JsonPrimitive(false),
        "last_tab" to JsonPrimitive(3L),
        "revision_goal" to JsonPrimitive(4.5),
        "user_name" to JsonPrimitive("जगदीश"),
        "hidden_subjects" to JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))),
        "cleared_key" to JsonNull
    )

    private fun encodeSample(): String = BackupFormat.encode(
        createdAtMs = 1_700_000_000_000L,
        appVersion = "1.0.76",
        tables = sampleTables(),
        prefs = samplePrefs()
    )

    @Test
    fun roundTripPreservesEveryValue() {
        val payload = BackupFormat.decode(encodeSample())

        assertEquals(1_700_000_000_000L, payload.createdAtMs)
        assertEquals("1.0.76", payload.appVersion)
        assertEquals(3, payload.totalRows)
        assertEquals(setOf("subjects", "topics", "study_logs"), payload.tables.keys)

        val subject = payload.tables.getValue("subjects").first()
        assertEquals("भौतिक विज्ञान 🎓", (subject["name"] as JsonPrimitive).content)
        assertEquals(12L, (subject["totalTopicsExpected"] as JsonPrimitive).long)

        val topics = payload.tables.getValue("topics")
        assertEquals(1_760_000_000_000L, (topics[0]["nextRevisionDateMs"] as JsonPrimitive).long)
        assertTrue(topics[0]["scoreObtained"] is JsonNull)
        assertEquals(0.75, (topics[1]["progress"] as JsonPrimitive).double, 0.00001)

        assertEquals(7, payload.prefs.size)
        assertEquals("जगदीश", (payload.prefs.getValue("user_name") as JsonPrimitive).content)
    }

    @Test
    fun rowCountsAreDerivedAndOrdered() {
        val payload = BackupFormat.decode(encodeSample())
        val counts = payload.rowCounts()

        assertEquals(listOf("subjects", "topics", "study_logs"), counts.keys.toList())
        assertEquals(1, counts["subjects"])
        assertEquals(2, counts["topics"])
        assertEquals(0, counts["study_logs"])
    }

    @Test
    fun decodeAcceptsFileWithoutOptionalMetadata() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", 1)
            put("tables", buildJsonObject {
                put("subjects", JsonArray(emptyList()))
            })
        }.toString()

        val payload = BackupFormat.decode(raw)
        assertEquals(0L, payload.createdAtMs)
        assertEquals("unknown", payload.appVersion)
        assertTrue(payload.prefs.isEmpty())
        assertEquals(0, payload.totalRows)
    }

    @Test
    fun rejectsEmptyFile() {
        assertFailure("This file is empty.") { BackupFormat.decode("   ") }
    }

    @Test
    fun rejectsBrokenJson() {
        assertFailure("not a valid backup file") { BackupFormat.decode("{ this is not json") }
    }

    @Test
    fun rejectsJsonThatIsNotAnObject() {
        assertFailure("not a valid backup file") { BackupFormat.decode("[1,2,3]") }
    }

    @Test
    fun rejectsFilesFromOtherApps() {
        val raw = buildJsonObject {
            put("format", "some-other-app")
            put("version", 1)
            put("tables", buildJsonObject { })
        }.toString()
        assertFailure("was not created by Revision Space") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsMissingVersion() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("tables", buildJsonObject { })
        }.toString()
        assertFailure("missing its version") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsBackupFromNewerAppVersion() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", BackupFormat.VERSION + 5)
            put("tables", buildJsonObject { })
        }.toString()
        assertFailure("newer version") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsFileWithoutTables() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", 1)
        }.toString()
        assertFailure("contains no data") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsEmptyTableMap() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", 1)
            put("tables", buildJsonObject { })
        }.toString()
        assertFailure("contains no data") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsInvalidTableName() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", 1)
            put("tables", buildJsonObject {
                put("subjects; DROP TABLE topics", JsonArray(emptyList()))
            })
        }.toString()
        assertFailure("invalid table name") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsTableThatIsNotAnArray() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", 1)
            put("tables", buildJsonObject {
                put("subjects", buildJsonObject { put("id", "sub-1") })
            })
        }.toString()
        assertFailure("malformed") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsRowThatIsNotAnObject() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", 1)
            put("tables", buildJsonObject {
                put("subjects", JsonArray(listOf(JsonPrimitive("nope"))))
            })
        }.toString()
        assertFailure("Row 1") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsNestedValuesInsideRows() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", 1)
            put("tables", buildJsonObject {
                put("subjects", JsonArray(listOf(buildJsonObject {
                    put("id", "sub-1")
                    put("meta", buildJsonObject { put("nested", true) })
                })))
            })
        }.toString()
        assertFailure("unsupported value") { BackupFormat.decode(raw) }
    }

    @Test
    fun rejectsMalformedPreferences() {
        val raw = buildJsonObject {
            put("format", BackupFormat.MAGIC)
            put("version", 1)
            put("tables", buildJsonObject {
                put("subjects", JsonArray(emptyList()))
            })
            put("prefs", JsonArray(listOf(JsonPrimitive("nope"))))
        }.toString()
        assertFailure("settings section") { BackupFormat.decode(raw) }
    }

    @Test
    fun labelsAndFileNamesAreStable() {
        assertEquals("Test scores", BackupFormat.labelFor("subject_tests"))
        assertEquals("Some table", BackupFormat.labelFor("some_table"))
        assertTrue(BackupFormat.fileName(1_700_000_000_000L).startsWith("RevisionSpace-Backup-"))
        assertTrue(BackupFormat.fileName(1_700_000_000_000L).endsWith(".json"))
    }

    @Test
    fun safetyFileNamesAreUniquePerSecond() {
        val name = BackupFormat.safetyFileName(1_700_000_000_000L)
        assertTrue(name.startsWith(BackupFormat.SAFETY_PREFIX))
        assertTrue(name.endsWith(".json"))
        // Two imports inside the same second must still not collide, so the name
        // carries seconds instead of minutes.
        val oneSecondLater = BackupFormat.safetyFileName(1_700_000_001_000L)
        assertTrue(name != oneSecondLater)
    }

    @Test
    fun safetyPruningKeepsOnlyNewestCopies() {
        val entries = listOf(
            "RevisionSpace-Safety-old.json" to 1_000L,
            "RevisionSpace-Safety-new.json" to 3_000L,
            "RevisionSpace-Safety-mid.json" to 2_000L
        )

        assertEquals(
            listOf("RevisionSpace-Safety-old.json"),
            BackupFormat.snapshotsToDelete(entries, keep = 2)
        )
        assertEquals(
            listOf(
                "RevisionSpace-Safety-mid.json",
                "RevisionSpace-Safety-old.json"
            ),
            BackupFormat.snapshotsToDelete(entries, keep = 1)
        )
        assertTrue(BackupFormat.snapshotsToDelete(entries, keep = 3).isEmpty())
        assertTrue(BackupFormat.snapshotsToDelete(entries, keep = 10).isEmpty())
        assertEquals(3, BackupFormat.snapshotsToDelete(entries, keep = -1).size)
        assertTrue(BackupFormat.snapshotsToDelete(emptyList(), keep = 3).isEmpty())
    }

    private fun assertFailure(expectedPart: String, block: () -> Unit) {
        try {
            block()
            fail("Expected BackupFormatException containing: $expectedPart")
        } catch (e: BackupFormatException) {
            assertNotNull(e.message)
            assertTrue(
                "Expected message to contain \"$expectedPart\" but was \"${e.message}\"",
                e.message!!.contains(expectedPart, ignoreCase = true)
            )
        }
    }
}
