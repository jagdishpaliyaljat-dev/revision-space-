package com.example

import com.example.data.Subject
import com.example.data.Chapter
import com.example.data.Revision
import com.example.services.AnalyticsService
import org.junit.Assert.assertEquals
import org.junit.Test

class OverallProgressSystemTest {

    private fun calculateChapterProgress(completedRevisions: Int): Float {
        return (completedRevisions.toFloat() / 10f * 100f).coerceAtMost(100f)
    }

    @Test
    fun testChapterProgress_allRevisionCounts() {
        // Verify Chapter Progress from 0 to 10 completed revisions
        for (i in 0..10) {
            val expectedProgress = i * 10f
            assertEquals(expectedProgress, calculateChapterProgress(i), 0.001f)
        }
        
        // Verify maximum progress constraint (not exceeding 100% if revision count exceeds 10)
        assertEquals(100f, calculateChapterProgress(11), 0.001f)
        assertEquals(100f, calculateChapterProgress(15), 0.001f)
    }

    @Test
    fun testSubjectProgress_averageOfChapters() {
        // Chapter 1: 10 completed revisions -> 100%
        // Chapter 2: 6 completed revisions -> 60%
        // Chapter 3: 2 completed revisions -> 20%
        // Expected Subject Progress = (100 + 60 + 20) / 3 = 60%
        val ch1Progress = calculateChapterProgress(10) // 100%
        val ch2Progress = calculateChapterProgress(6)  // 60%
        val ch3Progress = calculateChapterProgress(2)  // 20%
        
        val averageSubjectProgress = listOf(ch1Progress, ch2Progress, ch3Progress).average().toFloat()
        assertEquals(60f, averageSubjectProgress, 0.001f)
    }

    @Test
    fun testExamProgress_averageOfSubjects() {
        // Subject 1: average progress = 60%
        // Subject 2: average progress = 40%
        // Expected Exam Progress = (60 + 40) / 2 = 50%
        val sub1Progress = 60f
        val sub2Progress = 40f
        
        val examProgress = listOf(sub1Progress, sub2Progress).average().toFloat()
        assertEquals(50f, examProgress, 0.001f)
    }

    @Test
    fun testAnalyticsService_chapterBasedProgress() {
        val service = AnalyticsService()
        val examId = "exam_1"
        
        val subjects = listOf(
            Subject(id = "sub_1", examId = examId, name = "Subject 1"),
            Subject(id = "sub_2", examId = examId, name = "Subject 2")
        )
        
        val chapters = listOf(
            // Chapters for Subject 1
            Chapter(id = "chap_1", userId = "user_1", subjectId = "sub_1", name = "Chapter 1"),
            Chapter(id = "chap_2", userId = "user_1", subjectId = "sub_1", name = "Chapter 2"),
            // Chapters for Subject 2
            Chapter(id = "chap_3", userId = "user_1", subjectId = "sub_2", name = "Chapter 3")
        )
        
        val revisions = listOf(
            // Revisions for Chapter 1: 10 completed revisions -> 100%
            Revision(id = "rev_1_1", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_2", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_3", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_4", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_5", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_6", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_7", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_8", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_9", chapterId = "chap_1", status = "Completed"),
            Revision(id = "rev_1_10", chapterId = "chap_1", status = "Completed"),
            
            // Revisions for Chapter 2: 6 completed revisions -> 60%
            Revision(id = "rev_2_1", chapterId = "chap_2", status = "Completed"),
            Revision(id = "rev_2_2", chapterId = "chap_2", status = "Completed"),
            Revision(id = "rev_2_3", chapterId = "chap_2", status = "Completed"),
            Revision(id = "rev_2_4", chapterId = "chap_2", status = "Completed"),
            Revision(id = "rev_2_5", chapterId = "chap_2", status = "Completed"),
            Revision(id = "rev_2_6", chapterId = "chap_2", status = "Completed"),
            
            // Revisions for Chapter 3: 2 completed revisions -> 20%
            Revision(id = "rev_3_1", chapterId = "chap_3", status = "Completed"),
            Revision(id = "rev_3_2", chapterId = "chap_3", status = "Completed")
        )
        
        val metrics = service.calculateAnalyticsMetrics(
            allTopics = emptyList(),
            activeId = examId,
            logs = emptyList(),
            profile = null,
            timeFilter = "Overall",
            revisions = revisions,
            tests = emptyList(),
            allChapters = chapters,
            allSubjects = subjects
        )
        
        // Subject 1 progress: (100% + 60%) / 2 = 80%
        // Subject 2 progress: 20% / 1 = 20%
        // Overall/Exam progress: (80% + 20%) / 2 = 50%
        
        val sub1Prog = metrics.subjectProgress.find { it.id == "sub_1" }
        val sub2Prog = metrics.subjectProgress.find { it.id == "sub_2" }
        
        assertEquals(80, sub1Prog?.completionPercentage)
        assertEquals(20, sub2Prog?.completionPercentage)
        assertEquals(50, metrics.overallCompletionPercentage)
    }

    @Test
    fun testAnalyticsService_neverShow100PercentUnlessAllRevisionsDone() {
        val service = AnalyticsService()
        val examId = "exam_1"
        
        val subjects = listOf(
            Subject(id = "sub_1", examId = examId, name = "Subject 1")
        )
        
        val chapters = listOf(
            Chapter(id = "chap_1", userId = "user_1", subjectId = "sub_1", name = "Chapter 1"),
            Chapter(id = "chap_2", userId = "user_1", subjectId = "sub_1", name = "Chapter 2")
        )
        
        // Let's create revisions: chap_1 has 10 completed revisions (100%), chap_2 has 9 completed revisions (90%)
        val revisions = mutableListOf<Revision>()
        for (i in 1..10) {
            revisions.add(Revision(id = "rev_1_$i", chapterId = "chap_1", status = "Completed"))
        }
        for (i in 1..9) {
            revisions.add(Revision(id = "rev_2_$i", chapterId = "chap_2", status = "Completed"))
        }
        
        val metrics = service.calculateAnalyticsMetrics(
            allTopics = emptyList(),
            activeId = examId,
            logs = emptyList(),
            profile = null,
            timeFilter = "Overall",
            revisions = revisions,
            tests = emptyList(),
            allChapters = chapters,
            allSubjects = subjects
        )
        
        // Since chap_2 has only 9 completed revisions, overall completion percentage must NOT be 100%
        // Math average: (100 + 90) / 2 = 95%
        assertEquals(95, metrics.overallCompletionPercentage)
    }
}
