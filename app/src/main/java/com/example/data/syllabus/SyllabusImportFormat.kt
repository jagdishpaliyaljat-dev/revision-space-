package com.example.data.syllabus

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.Locale

/** The file cannot be understood. The message is shown to the user as-is. */
class SyllabusImportException(message: String) : IllegalArgumentException(message)

/** A parsed, de-duplicated syllabus tree, ready to be written to the database. */
data class SyllabusTree(
    val subjects: List<SyllabusSubject>,
    val warnings: List<String> = emptyList()
) {
    val subjectCount: Int get() = subjects.size
    val chapterCount: Int get() = subjects.sumOf { it.chapters.size }
    val topicCount: Int get() = subjects.sumOf { subject -> subject.chapters.sumOf { it.topics.size } }
    val isEmpty: Boolean get() = subjects.isEmpty()
    val summary: String get() = "$subjectCount subjects · $chapterCount chapters · $topicCount topics"
}

data class SyllabusSubject(val name: String, val chapters: List<SyllabusChapter>)

data class SyllabusChapter(val name: String, val topics: List<String>)

/** What an import actually changed. Existing rows are reused, never duplicated. */
data class SyllabusImportOutcome(
    val subjectsAdded: Int,
    val subjectsMatched: Int,
    val chaptersAdded: Int,
    val chaptersMatched: Int,
    val topicsAdded: Int,
    val topicsMatched: Int
) {
    val totalAdded: Int get() = subjectsAdded + chaptersAdded + topicsAdded
    val totalMatched: Int get() = subjectsMatched + chaptersMatched + topicsMatched
}

/**
 * Reads a syllabus file written as CSV or JSON.
 *
 * Both formats describe the same three levels: subject -> chapter -> topic.
 * Names are compared ignoring case and extra spaces, so the same file can be
 * imported twice without creating duplicates.
 */
object SyllabusImportFormat {

    const val MIME_TYPE_CSV = "text/csv"
    const val SAMPLE_CSV_FILE_NAME = "Revision-Space-Syllabus-Sample.csv"

    /** Hard limits so a malformed file can never freeze the app. */
    const val MAX_FILE_CHARS = 2_000_000
    const val MAX_SUBJECTS = 500
    const val MAX_CHAPTERS = 5_000
    const val MAX_TOPICS = 50_000
    const val MAX_NAME_LENGTH = 200
    const val MAX_WARNINGS = 20

    private val JSON = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowTrailingComma = true
    }

    /** Lower-case, single-spaced name used for matching ("Maths " == "maths"). */
    fun normalize(name: String): String =
        name.trim().replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)

    fun parse(text: String, fileName: String? = null): SyllabusTree {
        if (text.length > MAX_FILE_CHARS) {
            throw SyllabusImportException(
                "This syllabus file is too large. Keep it under 2 MB or split it into smaller files."
            )
        }
        val cleaned = text.trim().trimStart('\uFEFF')
        if (cleaned.isBlank()) {
            throw SyllabusImportException("This file is empty. Add subjects, chapters and topics first.")
        }
        return if (looksLikeJson(cleaned, fileName)) parseJson(cleaned) else parseCsv(cleaned)
    }

    fun looksLikeJson(text: String, fileName: String? = null): Boolean {
        val name = fileName?.lowercase(Locale.ROOT).orEmpty()
        if (name.endsWith(".json")) return true
        if (name.endsWith(".csv")) return false
        val first = text.firstOrNull { !it.isWhitespace() }
        return first == '{' || first == '['
    }

    fun sampleCsv(): String = """
        subject,chapter,topic
        Mathematics,Algebra,Linear Equations
        Mathematics,Algebra,Quadratic Equations
        Mathematics,Geometry,Triangles
        Science,Physics,Light
        Science,Chemistry,Acids and Bases
        English,Grammar,Nouns
    """.trimIndent()

    fun sampleJson(): String = """
        {
          "subjects": [
            {
              "name": "Mathematics",
              "chapters": [
                {
                  "name": "Algebra",
                  "topics": ["Linear Equations", "Quadratic Equations"]
                }
              ]
            }
          ]
        }
    """.trimIndent()

    // ------------------------------------------------------------------ CSV

    private fun parseCsv(text: String): SyllabusTree {
        val builder = SyllabusTreeBuilder()
        val delimiter = detectDelimiter(text)
        var columns: IntArray? = null
        var lastSubject: String? = null
        var lastChapter: String? = null
        var dataRows = 0

        text.lineSequence().forEachIndexed { index, raw ->
            val rowNumber = index + 1
            val line = raw.trimEnd('\r')
            if (line.isBlank()) return@forEachIndexed
            if (line.trimStart().startsWith("#")) return@forEachIndexed

            val cells = splitCsvLine(line, rowNumber, delimiter)
            if (columns == null) {
                val header = headerColumns(cells)
                if (header != null) {
                    columns = header
                    return@forEachIndexed
                }
                columns = intArrayOf(0, 1, 2)
            }
            val cols = columns!!

            fun cell(position: Int): String =
                if (position < 0) "" else cells.getOrNull(position)?.trim().orEmpty()

            val rawSubject = cell(cols[0])
            val rawChapter = cell(cols[1])
            val rawTopic = cell(cols[2])
            if (rawSubject.isBlank() && rawChapter.isBlank() && rawTopic.isBlank()) {
                return@forEachIndexed
            }

            // Spreadsheets often leave the subject/chapter cell blank on
            // follow-up rows. Inherit the value above instead of dropping data.
            if (rawSubject.isBlank()) {
                if (lastSubject == null) {
                    builder.warn("Row $rowNumber: subject is empty — skipped.")
                    return@forEachIndexed
                }
            } else {
                val previousSubject = lastSubject
                if (previousSubject == null ||
                    normalize(previousSubject) != normalize(rawSubject)
                ) {
                    lastChapter = null
                }
                lastSubject = rawSubject
            }

            val subject = rawSubject.ifBlank { lastSubject.orEmpty() }
            val chapter = when {
                rawChapter.isNotBlank() -> rawChapter
                rawTopic.isNotBlank() -> lastChapter.orEmpty()
                else -> ""
            }
            if (rawChapter.isNotBlank()) lastChapter = rawChapter

            dataRows++
            builder.add(subject, chapter, rawTopic, "Row $rowNumber")
            builder.enforceLimits()
        }

        if (dataRows == 0) {
            throw SyllabusImportException(
                "No syllabus rows found. Use columns: subject, chapter, topic."
            )
        }
        return builder.build()
    }

    private fun detectDelimiter(text: String): Char {
        val sample = text.lineSequence()
            .firstOrNull { it.isNotBlank() && !it.trimStart().startsWith("#") }
            ?: return ','
        val candidates = listOf(',', ';', '\t', '|')
        return candidates.maxByOrNull { candidate ->
            var count = 0
            var inQuotes = false
            sample.forEach { char ->
                if (char == '"') inQuotes = !inQuotes
                else if (char == candidate && !inQuotes) count++
            }
            count
        } ?: ','
    }

    private fun splitCsvLine(line: String, rowNumber: Int, delimiter: Char): List<String> {
        val cells = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' -> {
                    if (inQuotes && index + 1 < line.length && line[index + 1] == '"') {
                        current.append('"')
                        index++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                char == delimiter && !inQuotes -> {
                    cells += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            index++
        }
        if (inQuotes) {
            throw SyllabusImportException(
                "Row $rowNumber has an unclosed quote (\"). Fix the file and try again."
            )
        }
        cells += current.toString()
        return cells
    }

    /** Returns the subject/chapter/topic column indexes, or null for a data row. */
    private fun headerColumns(cells: List<String>): IntArray? {
        val names = cells.map { normalize(it) }
        fun find(vararg keys: String): Int = names.indexOfFirst { it in keys }
        val subject = find("subject", "subjects", "subject name")
        if (subject < 0) return null
        val chapter = find("chapter", "chapters", "chapter name", "unit", "units")
        val topic = find("topic", "topics", "topic name", "subtopic", "subtopics")
        return intArrayOf(subject, chapter, if (topic >= 0) topic else -1)
    }

    // ----------------------------------------------------------------- JSON

    private fun parseJson(text: String): SyllabusTree {
        val root = try {
            JSON.parseToJsonElement(text)
        } catch (e: Exception) {
            throw SyllabusImportException(
                "This JSON file isn't valid. Check for a missing comma or bracket, then try again."
            )
        }

        val builder = SyllabusTreeBuilder()
        when (root) {
            is JsonArray -> addJsonSubjects(root, builder)
            is JsonObject -> {
                val list = root["subjects"] ?: root["syllabus"] ?: root["data"]
                when (list) {
                    is JsonArray -> addJsonSubjects(list, builder)
                    is JsonObject -> addJsonSubject(list, builder, "Subject 1")
                    null -> {
                        if (root.containsKey("subject") || root.containsKey("chapter")) {
                            addJsonRow(root, builder, "Entry 1")
                        } else {
                            addJsonSubject(root, builder, "Subject 1")
                        }
                    }
                    else -> throw SyllabusImportException("\"subjects\" must be a list of subjects.")
                }
            }
            else -> throw SyllabusImportException(
                "The JSON file must start with a list of subjects, for example {\"subjects\": [...]}."
            )
        }
        builder.enforceLimits()
        return builder.build()
    }

    private fun addJsonSubjects(array: JsonArray, builder: SyllabusTreeBuilder) {
        array.forEachIndexed { index, element ->
            val label = "Subject ${index + 1}"
            when (element) {
                is JsonPrimitive -> builder.add(element.textOrEmpty(), "", "", label)
                is JsonObject -> {
                    val hasHierarchy = element.containsKey("chapters") ||
                        element.containsKey("units") ||
                        element.containsKey("name") ||
                        element.containsKey("title")
                    if (!hasHierarchy && (element.containsKey("subject") || element.containsKey("chapter"))) {
                        addJsonRow(element, builder, "Entry ${index + 1}")
                    } else {
                        addJsonSubject(element, builder, label)
                    }
                }
                else -> builder.warn("$label: not a subject object — skipped.")
            }
        }
    }

    private fun addJsonSubject(subject: JsonObject, builder: SyllabusTreeBuilder, label: String) {
        val subjectName = subject.stringOrNull("name", "subject", "title")
        if (subjectName.isNullOrBlank()) {
            builder.warn("$label: subject name is missing — skipped.")
            return
        }

        val chapters = (subject["chapters"] ?: subject["units"]) as? JsonArray ?: JsonArray(emptyList())
        val topics = subject["topics"] as? JsonArray
        if (chapters.isEmpty() && topics == null) {
            builder.add(subjectName, "", "", label)
            return
        }
        if (chapters.isEmpty() && topics != null) {
            builder.warn("${subjectName.trim()}: topics need a chapter — add a \"chapters\" list.")
            return
        }

        chapters.forEachIndexed { chapterIndex, element ->
            val chapterLabel = "$label chapter ${chapterIndex + 1}"
            when (element) {
                is JsonPrimitive -> builder.add(subjectName, element.textOrEmpty(), "", chapterLabel)
                is JsonObject -> {
                    val chapterName = element.stringOrNull("name", "chapter", "title")
                    if (chapterName.isNullOrBlank()) {
                        builder.warn("$chapterLabel: chapter name is missing — skipped.")
                    } else {
                        val chapterTopics = (element["topics"] ?: element["subtopics"]) as? JsonArray
                        if (chapterTopics == null) {
                            builder.add(subjectName, chapterName, "", chapterLabel)
                        } else {
                            chapterTopics.forEachIndexed { topicIndex, topicElement ->
                                val topicName = when (topicElement) {
                                    is JsonPrimitive -> topicElement.textOrEmpty()
                                    is JsonObject -> topicElement.stringOrNull("name", "topic", "title")
                                    else -> null
                                }
                                if (topicName.isNullOrBlank()) {
                                    builder.warn("$chapterLabel: topic ${topicIndex + 1} is empty — skipped.")
                                } else {
                                    builder.add(subjectName, chapterName, topicName, chapterLabel)
                                }
                            }
                        }
                    }
                }
                else -> builder.warn("$chapterLabel: not a chapter — skipped.")
            }
        }
    }

    private fun addJsonRow(row: JsonObject, builder: SyllabusTreeBuilder, label: String) {
        builder.add(
            row.stringOrNull("subject"),
            row.stringOrNull("chapter"),
            row.stringOrNull("topic"),
            label
        )
    }

    private fun JsonObject.stringOrNull(vararg keys: String): String? {
        keys.forEach { key ->
            val value = this[key] as? JsonPrimitive ?: return@forEach
            if (value is kotlinx.serialization.json.JsonNull) return@forEach
            return value.content
        }
        return null
    }

    /** Empty string for a JSON null so it is treated like a blank cell. */
    private fun JsonPrimitive.textOrEmpty(): String =
        if (this is kotlinx.serialization.json.JsonNull) "" else content
}

/**
 * Collects rows into ordered subject -> chapter -> topic maps, merging duplicate
 * names as it goes. Keeping this separate from the file parsers makes the merge
 * behaviour identical for CSV and JSON.
 */
private class SyllabusTreeBuilder {

    private class MutableSubject(val name: String) {
        val chapters = LinkedHashMap<String, MutableChapter>()
    }

    private class MutableChapter(val name: String) {
        val topics = LinkedHashMap<String, String>()
    }

    private val subjects = LinkedHashMap<String, MutableSubject>()
    private val warnings = mutableListOf<String>()

    fun warn(message: String) {
        if (warnings.size < SyllabusImportFormat.MAX_WARNINGS) warnings += message
    }

    fun add(subjectName: String?, chapterName: String?, topicName: String?, where: String) {
        val subject = cleanName(subjectName, where, "subject") ?: return
        val subjectNode = subjects.getOrPut(SyllabusImportFormat.normalize(subject)) {
            MutableSubject(subject)
        }

        if (chapterName.isNullOrBlank() && topicName.isNullOrBlank()) return
        if (chapterName.isNullOrBlank()) {
            warn("$where: topic \"${topicName.orEmpty().trim()}\" has no chapter — skipped.")
            return
        }

        val chapter = cleanName(chapterName, where, "chapter") ?: return
        val chapterNode = subjectNode.chapters.getOrPut(SyllabusImportFormat.normalize(chapter)) {
            MutableChapter(chapter)
        }

        if (topicName.isNullOrBlank()) return
        val topic = cleanName(topicName, where, "topic") ?: return
        chapterNode.topics.getOrPut(SyllabusImportFormat.normalize(topic)) { topic }
    }

    fun enforceLimits() {
        if (subjects.size > SyllabusImportFormat.MAX_SUBJECTS) {
            throw SyllabusImportException(
                "This file has more than ${SyllabusImportFormat.MAX_SUBJECTS} subjects. Split it into smaller files."
            )
        }
        val chapters = subjects.values.sumOf { it.chapters.size }
        if (chapters > SyllabusImportFormat.MAX_CHAPTERS) {
            throw SyllabusImportException(
                "This file has more than ${SyllabusImportFormat.MAX_CHAPTERS} chapters. Split it into smaller files."
            )
        }
        val topics = subjects.values.sumOf { subject -> subject.chapters.values.sumOf { it.topics.size } }
        if (topics > SyllabusImportFormat.MAX_TOPICS) {
            throw SyllabusImportException(
                "This file has more than ${SyllabusImportFormat.MAX_TOPICS} topics. Split it into smaller files."
            )
        }
    }

    fun build(): SyllabusTree {
        if (subjects.isEmpty()) {
            throw SyllabusImportException(
                "No subjects found. Each row needs a subject name (for example: Mathematics, Algebra, Linear Equations)."
            )
        }
        return SyllabusTree(
            subjects = subjects.values.map { subject ->
                SyllabusSubject(
                    name = subject.name,
                    chapters = subject.chapters.values.map { chapter ->
                        SyllabusChapter(chapter.name, chapter.topics.values.toList())
                    }
                )
            },
            warnings = warnings.toList()
        )
    }

    private fun cleanName(raw: String?, where: String, label: String): String? {
        val trimmed = raw?.trim()?.replace(Regex("\\s+"), " ") ?: return null
        if (trimmed.isEmpty()) {
            warn("$where: $label is empty — skipped.")
            return null
        }
        if (trimmed.length > SyllabusImportFormat.MAX_NAME_LENGTH) {
            warn("$where: $label is longer than ${SyllabusImportFormat.MAX_NAME_LENGTH} characters — skipped.")
            return null
        }
        return trimmed
    }
}
