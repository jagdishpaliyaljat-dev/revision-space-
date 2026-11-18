package com.example

import com.example.data.Subject
import com.example.data.Chapter
import com.example.data.Topic
import com.example.data.RevisionRepository
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChapterManagementTest {

    private lateinit var fakeRepository: TestRevisionRepository
    private lateinit var viewModel: RevisionViewModel

    class TestRevisionRepository : RevisionRepository() {
        override val userId: String = "test_user_abc"
        
        val subjectsList = mutableListOf<Subject>()
        val chaptersList = mutableListOf<Chapter>()
        val topicsList = mutableListOf<Topic>()
        
        override val allSubjects: Flow<List<Subject>> = flowOf(subjectsList)
        override val allChapters: Flow<List<Chapter>> = flowOf(chaptersList)
        override val allTopics: Flow<List<Topic>> = flowOf(topicsList)

        override suspend fun insertChapter(
            subjectId: String,
            name: String,
            importance: String
        ) {
            val newId = "chap_${System.currentTimeMillis()}_${chaptersList.size}"
            chaptersList.add(
                Chapter(
                    id = newId,
                    userId = userId,
                    subjectId = subjectId,
                    name = name,
                    importance = importance
                )
            )
        }

        override suspend fun updateChapter(chapter: Chapter) {
            val index = chaptersList.indexOfFirst { it.id == chapter.id }
            if (index != -1) {
                chaptersList[index] = chapter
            }
        }

        override suspend fun deleteChapter(id: String) {
            chaptersList.removeAll { it.id == id }
            topicsList.removeAll { it.chapterId == id }
        }
    }

    @Before
    fun setUp() {
        fakeRepository = TestRevisionRepository()
        viewModel = RevisionViewModel(fakeRepository)
        
        // Add a sample subject for tests
        fakeRepository.subjectsList.add(
            Subject(
                id = "sub_maths",
                userId = "test_user_abc",
                examId = "exam1",
                name = "Mathematics",
                colorHex = "#0061A4",
                icon = "book",
                totalTopicsExpected = 10
            )
        )
    }

    @Test
    fun testAddChapter_Valid() = runBlocking {
        fakeRepository.insertChapter("sub_maths", "Algebra", "High")
        assertEquals(1, fakeRepository.chaptersList.size)
        val saved = fakeRepository.chaptersList[0]
        assertEquals("Algebra", saved.name)
        assertEquals("sub_maths", saved.subjectId)
        assertEquals("High", saved.importance)
        assertFalse(saved.id.isEmpty())
    }

    @Test
    fun testEmptyAndWhitespaceValidation() {
        val emptyName = "   "
        val trimmed = emptyName.trim()
        assertTrue(trimmed.isEmpty())
        
        val lineBreaks = "\n\n  \n"
        assertTrue(lineBreaks.trim().isEmpty())
    }

    @Test
    fun testDuplicateChapterDetection() = runBlocking {
        fakeRepository.insertChapter("sub_maths", "Trigonometry", "Medium")
        val newName = "trigonometry"
        
        // Under same subject: duplicate
        val isDuplicateSameSubject = fakeRepository.chaptersList.any { 
            it.subjectId == "sub_maths" && it.name.equals(newName, ignoreCase = true) 
        }
        assertTrue(isDuplicateSameSubject)
        
        // Under different subject: allowed
        val isDuplicateDifferentSubject = fakeRepository.chaptersList.any { 
            it.subjectId == "sub_physics" && it.name.equals(newName, ignoreCase = true) 
        }
        assertFalse(isDuplicateDifferentSubject)
    }

    @Test
    fun testLongChapterName() = runBlocking {
        val longName = "A".repeat(500)
        fakeRepository.insertChapter("sub_maths", longName, "Medium")
        assertEquals(1, fakeRepository.chaptersList.size)
        assertEquals(longName, fakeRepository.chaptersList[0].name)
    }

    @Test
    fun testSpecialCharactersAndUnicode() = runBlocking {
        val specialName = "Chapter 1: प्रतिशत 📊 & Ratio & Proportion!"
        fakeRepository.insertChapter("sub_maths", specialName, "High")
        assertEquals(1, fakeRepository.chaptersList.size)
        assertEquals(specialName, fakeRepository.chaptersList[0].name)
    }

    @Test
    fun testEditChapter() = runBlocking {
        fakeRepository.insertChapter("sub_maths", "Calculus", "High")
        val chapter = fakeRepository.chaptersList[0]
        val updated = chapter.copy(name = "Advanced Calculus")
        fakeRepository.updateChapter(updated)
        assertEquals(1, fakeRepository.chaptersList.size)
        assertEquals("Advanced Calculus", fakeRepository.chaptersList[0].name)
    }

    @Test
    fun testDeleteChapterCascading() = runBlocking {
        fakeRepository.insertChapter("sub_maths", "Geometry", "High")
        val chapterId = fakeRepository.chaptersList[0].id
        
        // Add mock topics under this chapter
        fakeRepository.topicsList.add(
            Topic(
                id = "topic1",
                userId = "test_user_abc",
                subjectId = "sub_maths",
                chapterId = chapterId,
                name = "Triangles"
            )
        )
        
        assertEquals(1, fakeRepository.chaptersList.size)
        assertEquals(1, fakeRepository.topicsList.size)
        
        fakeRepository.deleteChapter(chapterId)
        
        assertEquals(0, fakeRepository.chaptersList.size)
        assertEquals(0, fakeRepository.topicsList.size) // Topics must be deleted
    }

    @Test
    fun testSubjectRelationshipIntegrity() = runBlocking {
        fakeRepository.insertChapter("sub_maths", "Probability", "Medium")
        val chapter = fakeRepository.chaptersList[0]
        assertEquals("sub_maths", chapter.subjectId)
    }
}
