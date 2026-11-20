package com.example.data.syllabus

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SyllabusImportFormatTest {

    // ------------------------------------------------------------------ CSV

    @Test
    fun csvWithHeaderParsesThreeLevels() {
        val tree = SyllabusImportFormat.parse(
            """
            subject,chapter,topic
            Mathematics,Algebra,Linear Equations
            Mathematics,Algebra,Quadratic Equations
            Mathematics,Geometry,Triangles
            """.trimIndent(),
            "syllabus.csv"
        )

        assertEquals(1, tree.subjectCount)
        assertEquals(2, tree.chapterCount)
        assertEquals(3, tree.topicCount)
        assertEquals("Mathematics", tree.subjects.first().name)
        assertEquals(
            listOf("Linear Equations", "Quadratic Equations"),
            tree.subjects.first().chapters.first().topics
        )
    }

    @Test
    fun csvWithoutHeaderWorks() {
        val tree = SyllabusImportFormat.parse("Math,Algebra,Vectors\nMath,Algebra,Matrices")

        assertEquals(1, tree.subjectCount)
        assertEquals(1, tree.chapterCount)
        assertEquals(2, tree.topicCount)
    }

    @Test
    fun csvSupportsQuotedValuesWithCommas() {
        val tree = SyllabusImportFormat.parse(
            """
            subject,chapter,topic
            "Math, Advanced",Algebra,"Vectors, Part 1"
            """.trimIndent()
        )

        assertEquals("Math, Advanced", tree.subjects.first().name)
        assertEquals("Vectors, Part 1", tree.subjects.first().chapters.first().topics.first())
    }

    @Test
    fun csvInheritsBlankSubjectAndChapterCells() {
        val tree = SyllabusImportFormat.parse(
            """
            subject,chapter,topic
            Mathematics,Algebra,Linear Equations
            ,,Quadratic Equations
            ,Geometry,Triangles
            """.trimIndent()
        )

        assertEquals(1, tree.subjectCount)
        assertEquals(2, tree.chapterCount)
        assertEquals(3, tree.topicCount)
    }

    @Test
    fun csvMergesDuplicateNamesIgnoringCaseAndSpacing() {
        val tree = SyllabusImportFormat.parse(
            """
            subject,chapter,topic
            Mathematics,Algebra,Linear Equations
             mathematics ,  algebra , linear   equations
            """.trimIndent()
        )

        assertEquals(1, tree.subjectCount)
        assertEquals(1, tree.chapterCount)
        assertEquals(1, tree.topicCount)
        assertEquals("Mathematics", tree.subjects.first().name)
    }

    @Test
    fun csvAllowsChapterWithoutTopics() {
        val tree = SyllabusImportFormat.parse(
            """
            subject,chapter,topic
            Science,Physics,
            """.trimIndent()
        )

        assertEquals(1, tree.chapterCount)
        assertEquals(0, tree.topicCount)
    }

    @Test
    fun csvWarnsWhenTopicHasNoChapter() {
        val tree = SyllabusImportFormat.parse(
            """
            subject,chapter,topic
            Science,,Light
            """.trimIndent()
        )

        assertEquals(1, tree.subjectCount)
        assertEquals(0, tree.chapterCount)
        assertTrue(tree.warnings.any { it.contains("no chapter") })
    }

    @Test
    fun csvSupportsSemicolonDelimiter() {
        val tree = SyllabusImportFormat.parse("subject;chapter;topic\nMath;Algebra;Vectors")

        assertEquals(1, tree.subjectCount)
        assertEquals(1, tree.chapterCount)
        assertEquals(1, tree.topicCount)
    }

    @Test
    fun csvIgnoresCommentsAndBlankLines() {
        val tree = SyllabusImportFormat.parse(
            """
            # my syllabus

            subject,chapter,topic
            Math,Algebra,Vectors

            """.trimIndent()
        )

        assertEquals(1, tree.topicCount)
    }

    @Test
    fun csvWindowsLineEndingsWork() {
        val tree = SyllabusImportFormat.parse("subject,chapter,topic\r\nMath,Algebra,Vectors\r\n")

        assertEquals(1, tree.subjectCount)
        assertEquals(1, tree.topicCount)
    }

    @Test
    fun csvThrowsOnUnclosedQuote() {
        val error = assertThrows(SyllabusImportException::class.java) {
            SyllabusImportFormat.parse("subject,chapter,topic\nMath,\"Algebra,Vectors")
        }
        assertTrue(error.message.orEmpty().contains("unclosed quote"))
    }

    @Test
    fun emptyFileThrows() {
        assertThrows(SyllabusImportException::class.java) {
            SyllabusImportFormat.parse("   \n\n")
        }
    }

    @Test
    fun headerOnlyFileThrows() {
        assertThrows(SyllabusImportException::class.java) {
            SyllabusImportFormat.parse("subject,chapter,topic\n")
        }
    }

    // ----------------------------------------------------------------- JSON

    @Test
    fun jsonObjectRootParses() {
        val tree = SyllabusImportFormat.parse(
            """
            {
              "subjects": [
                {
                  "name": "Mathematics",
                  "chapters": [
                    { "name": "Algebra", "topics": ["Vectors", "Matrices"] },
                    { "name": "Geometry", "topics": ["Triangles"] }
                  ]
                }
              ]
            }
            """.trimIndent(),
            "syllabus.json"
        )

        assertEquals(1, tree.subjectCount)
        assertEquals(2, tree.chapterCount)
        assertEquals(3, tree.topicCount)
    }

    @Test
    fun jsonArrayRootParses() {
        val tree = SyllabusImportFormat.parse(
            """[{"name": "Math"}, {"name": "Science"}]""",
            "syllabus.json"
        )

        assertEquals(2, tree.subjectCount)
        assertEquals(0, tree.chapterCount)
    }

    @Test
    fun jsonFlatRowArrayParses() {
        val tree = SyllabusImportFormat.parse(
            """[{"subject":"Math","chapter":"Algebra","topic":"Vectors"}]""",
            "syllabus.json"
        )

        assertEquals(1, tree.subjectCount)
        assertEquals(1, tree.chapterCount)
        assertEquals(1, tree.topicCount)
    }

    @Test
    fun jsonIgnoresUnknownKeys() {
        val tree = SyllabusImportFormat.parse(
            """
            {
              "exportedBy": "spreadsheet",
              "subjects": [
                {
                  "name": "Math",
                  "color": "purple",
                  "chapters": [
                    { "name": "Algebra", "hours": 3, "topics": ["Vectors"] }
                  ]
                }
              ]
            }
            """.trimIndent(),
            "syllabus.json"
        )

        assertEquals(1, tree.subjectCount)
        assertEquals(1, tree.topicCount)
    }

    @Test
    fun jsonTopicsCanBeObjects() {
        val tree = SyllabusImportFormat.parse(
            """
            {"subjects":[{"name":"Math","chapters":[{"name":"Algebra","topics":[{"name":"Vectors"}]}]}]}
            """.trimIndent(),
            "syllabus.json"
        )

        assertEquals("Vectors", tree.subjects.first().chapters.first().topics.first())
    }

    @Test
    fun invalidJsonThrowsFriendlyError() {
        val error = assertThrows(SyllabusImportException::class.java) {
            SyllabusImportFormat.parse("{\"subjects\": [}", "syllabus.json")
        }
        assertTrue(error.message.orEmpty().contains("isn't valid"))
    }

    @Test
    fun jsonWithoutSubjectsThrows() {
        assertThrows(SyllabusImportException::class.java) {
            SyllabusImportFormat.parse("""{"subjects": []}""", "syllabus.json")
        }
    }

    // -------------------------------------------------------------- helpers

    @Test
    fun sampleCsvIsValidAndParses() {
        val tree = SyllabusImportFormat.parse(SyllabusImportFormat.sampleCsv(), "sample.csv")

        assertEquals(3, tree.subjectCount)
        assertEquals(5, tree.chapterCount)
        assertEquals(6, tree.topicCount)
        assertTrue(tree.warnings.isEmpty())
    }

    @Test
    fun sampleJsonIsValidAndParses() {
        val tree = SyllabusImportFormat.parse(SyllabusImportFormat.sampleJson(), "sample.json")

        assertEquals(1, tree.subjectCount)
        assertEquals(1, tree.chapterCount)
        assertEquals(2, tree.topicCount)
    }

    @Test
    fun normalizeIgnoresCaseAndExtraSpaces() {
        assertEquals(
            SyllabusImportFormat.normalize("linear equations"),
            SyllabusImportFormat.normalize("  Linear   Equations ")
        )
    }

    @Test
    fun tooLongChapterIsSkippedWithWarning() {
        val longName = "C".repeat(SyllabusImportFormat.MAX_NAME_LENGTH + 1)
        val tree = SyllabusImportFormat.parse("Math,$longName,Vectors")

        assertEquals(1, tree.subjectCount)
        assertEquals(0, tree.chapterCount)
        assertTrue(tree.warnings.any { it.contains("longer than") })
    }
}
