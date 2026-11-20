package com.example.data.chat

import org.junit.Assert.assertTrue
import org.junit.Test

class StudyChatPromptTest {

    @Test
    fun contextIncludesBasicFieldsAndLimitsTopics() {
        val pending = (1..30).map { index ->
            StudyTopicRef("Math", "Algebra", "Topic $index", "Pending")
        }
        val snapshot = StudySnapshot(
            studentName = "Asha",
            currentStreak = 4,
            coins = 120,
            studyHours = 12,
            revisionsDone = 8,
            subjects = (1..40).map { "Subject $it" },
            pendingTopics = pending,
            overdueTopics = (1..20).map { "Overdue $it" }
        )

        val context = StudyChatPrompt.context(snapshot)

        assertTrue(context.contains("Student: Asha"))
        assertTrue(context.contains("Current streak: 4 day(s); Coins: 120"))
        assertTrue(context.contains("Study total: 12 hours; Revisions done: 8"))
        assertTrue(context.contains("Subjects:"))
        // Subjects are capped.
        assertTrue(!context.contains("Subject 31"))
        // Topics are capped at 25.
        assertTrue(context.contains("Topic 25"))
        assertTrue(!context.contains("Topic 26"))
        // Overdue is capped at 10.
        assertTrue(context.contains("Overdue 10"))
        assertTrue(!context.contains("Overdue 11"))
    }

    @Test
    fun contextSkipsEmptyStudentName() {
        val context = StudyChatPrompt.context(StudySnapshot())
        assertTrue(!context.contains("Student: "))
        assertTrue(context.contains("Current streak:"))
    }

    @Test
    fun buildIncludesPersonaAndLanguageInstruction() {
        val prompt = StudyChatPrompt.build("", emptyList())
        assertTrue(prompt.contains("Study Buddy"))
        assertTrue(prompt.contains("Hindi, English or Hinglish"))
        assertTrue(prompt.endsWith("Study Buddy:"))
    }

    @Test
    fun buildAddsContextSectionOnlyWhenNonBlank() {
        val withContext = StudyChatPrompt.build("Subjects: Math", emptyList())
        assertTrue(withContext.contains("Here is the student's current study data"))
        assertTrue(withContext.contains("Subjects: Math"))

        val without = StudyChatPrompt.build("   ", emptyList())
        assertTrue(!without.contains("Here is the student's current study data"))
    }

    @Test
    fun buildLabelsTurnsAndKeepsLastTwelve() {
        val turns = (1..20).map { index ->
            StudyTurn(fromUser = index % 2 == 0, text = "Message $index")
        }

        val prompt = StudyChatPrompt.build("ctx", turns)

        assertTrue(prompt.contains("Student: Message 20"))
        assertTrue(prompt.contains("Study Buddy: Message 19"))
        assertTrue(!prompt.contains("Message 7"))
        assertTrue(!prompt.contains("Message 8"))
    }

    @Test
    fun buildSkipsBlankTurns() {
        val prompt = StudyChatPrompt.build(
            "",
            listOf(StudyTurn(true, "   "), StudyTurn(false, "Real answer"))
        )
        assertTrue(!prompt.contains("Student:"))
        assertTrue(prompt.contains("Study Buddy: Real answer"))
    }
}
