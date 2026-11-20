package com.example.data.chat

/**
 * A small, UI-free snapshot of what the student is working on. Keeping the
 * prompt building out of the Android classes means it can be unit tested and
 * that the AI never receives more data than these explicit fields.
 */
data class StudySnapshot(
    val studentName: String = "",
    val currentStreak: Int = 0,
    val coins: Int = 0,
    val studyHours: Long = 0L,
    val revisionsDone: Int = 0,
    val subjects: List<String> = emptyList(),
    val pendingTopics: List<StudyTopicRef> = emptyList(),
    val overdueTopics: List<String> = emptyList()
)

data class StudyTopicRef(
    val subject: String,
    val chapter: String,
    val topic: String,
    val status: String = "Pending"
)

/** One line of the conversation, used to build the follow-up prompt. */
data class StudyTurn(
    val fromUser: Boolean,
    val text: String
)

/**
 * Builds the "Study Buddy" prompt. The whole conversation is sent as text each
 * time (no chat session is persisted), so the model always gets the study
 * context plus the last few turns.
 */
object StudyChatPrompt {

    const val MAX_TOPICS = 25
    const val MAX_OVERDUE = 10
    const val MAX_SUBJECTS = 30
    const val MAX_TURNS = 12

    private const val PERSONA = "You are \"Study Buddy\", a friendly and concise AI study tutor inside the Revision Space app.\n" +
        "Help the student study better: explain concepts simply, give short examples,\n" +
        "create quick quizzes, and suggest practical revision plans.\n" +
        "Answer in the same language the student used (Hindi, English or Hinglish).\n" +
        "Keep answers focused and easy to scan. Use short paragraphs and bullet points."

    /** Compact description of the student's data, safe to send to the model. */
    fun context(snapshot: StudySnapshot): String = buildString {
        val name = snapshot.studentName.trim()
        if (name.isNotEmpty()) appendLine("Student: $name")
        appendLine("Current streak: ${snapshot.currentStreak} day(s); Coins: ${snapshot.coins}")
        appendLine("Study total: ${snapshot.studyHours} hours; Revisions done: ${snapshot.revisionsDone}")

        if (snapshot.subjects.isNotEmpty()) {
            appendLine(
                "Subjects: " + snapshot.subjects
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .take(MAX_SUBJECTS)
                    .joinToString(", ")
            )
        }

        val pending = snapshot.pendingTopics.take(MAX_TOPICS)
        if (pending.isNotEmpty()) {
            appendLine("Topics to revise (max $MAX_TOPICS):")
            pending.forEach { topic ->
                appendLine(
                    "- ${topic.subject} > ${topic.chapter} > ${topic.topic} " +
                        "[${topic.status.ifBlank { "Pending" }}]"
                )
            }
        }

        val overdue = snapshot.overdueTopics
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(MAX_OVERDUE)
        if (overdue.isNotEmpty()) {
            appendLine("Overdue revisions (max $MAX_OVERDUE): " + overdue.joinToString(", "))
        }
    }.trim()

    /**
     * Builds the full prompt. [studyContext] is skipped when blank so a new
     * student still gets a working tutor.
     */
    fun build(studyContext: String, turns: List<StudyTurn>): String = buildString {
        appendLine(PERSONA)
        if (studyContext.isNotBlank()) {
            appendLine()
            appendLine("Here is the student's current study data. Use it to personalise the answer,")
            appendLine("but never invent data that is not listed:")
            appendLine(studyContext.trim())
        }
        appendLine()
        appendLine("Conversation so far:")
        turns.takeLast(MAX_TURNS).forEach { turn ->
            if (turn.text.isNotBlank()) {
                appendLine((if (turn.fromUser) "Student: " else "Study Buddy: ") + turn.text.trim())
            }
        }
        append("Study Buddy:")
    }
}
