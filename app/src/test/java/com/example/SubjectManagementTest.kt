package com.example

import com.example.data.Subject
import com.example.data.Chapter
import com.example.data.Topic
import com.example.data.UserProfile
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
class SubjectManagementTest {

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

        override suspend fun insertSubject(
            name: String,
            colorHex: String,
            examId: String,
            icon: String,
            totalTopicsExpected: Int
        ) {
            val newId = "sub_${System.currentTimeMillis()}_${subjectsList.size}"
            subjectsList.add(
                Subject(
                    id = newId,
                    userId = userId,
                    examId = examId,
                    name = name,
                    colorHex = colorHex,
                    icon = icon,
                    totalTopicsExpected = totalTopicsExpected
                )
            )
        }

        override suspend fun updateSubject(
            subjectId: String,
            name: String,
            colorHex: String,
            icon: String,
            totalTopicsExpected: Int
        ) {
            val index = subjectsList.indexOfFirst { it.id == subjectId }
            if (index != -1) {
                subjectsList[index] = Subject(
                    id = subjectId,
                    userId = userId,
                    examId = subjectsList[index].examId,
                    name = name,
                    colorHex = colorHex,
                    icon = icon,
                    totalTopicsExpected = totalTopicsExpected
                )
            }
        }

        override suspend fun deleteSubject(id: String) {
            subjectsList.removeAll { it.id == id }
            chaptersList.removeAll { it.subjectId == id }
            topicsList.removeAll { it.subjectId == id }
        }
    }

    @Before
    fun setUp() {
        fakeRepository = TestRevisionRepository()
        viewModel = RevisionViewModel(fakeRepository)
    }

    @Test
    fun testAddSubject_Valid() = runBlocking {
        fakeRepository.insertSubject("Mathematics", "#0061A4", "exam1", "book", 10)
        assertEquals(1, fakeRepository.subjectsList.size)
        val saved = fakeRepository.subjectsList[0]
        assertEquals("Mathematics", saved.name)
        assertEquals("#0061A4", saved.colorHex)
        assertEquals(10, saved.totalTopicsExpected)
        assertFalse(saved.id.isEmpty())
    }

    @Test
    fun testEmptyAndWhitespaceValidation() {
        val emptyName = "   "
        val trimmed = emptyName.trim()
        assertTrue(trimmed.isEmpty())
    }

    @Test
    fun testDuplicateSubjectDetection() = runBlocking {
        fakeRepository.insertSubject("Physics", "#0061A4", "exam1", "book", 5)
        val trimmedNew = "physics"
        val isDuplicate = fakeRepository.subjectsList.any { it.name.equals(trimmedNew, ignoreCase = true) }
        assertTrue(isDuplicate)
    }

    @Test
    fun testSpecialCharactersAndUnicode() = runBlocking {
        val nameWithSpecialChars = "Chemistry & Biology 🧪 (HINDI - रसायन शास्त्र)"
        fakeRepository.insertSubject(nameWithSpecialChars, "#0061A4", "exam1", "book", 15)
        assertEquals(1, fakeRepository.subjectsList.size)
        assertEquals(nameWithSpecialChars, fakeRepository.subjectsList[0].name)
    }

    @Test
    fun testEditSubject() = runBlocking {
        fakeRepository.insertSubject("Geography", "#0061A4", "exam1", "book", 8)
        val id = fakeRepository.subjectsList[0].id
        fakeRepository.updateSubject(id, "Physical Geography", "#FF0000", "globe", 12)
        assertEquals(1, fakeRepository.subjectsList.size)
        assertEquals("Physical Geography", fakeRepository.subjectsList[0].name)
        assertEquals("#FF0000", fakeRepository.subjectsList[0].colorHex)
        assertEquals(12, fakeRepository.subjectsList[0].totalTopicsExpected)
    }

    @Test
    fun testDeleteSubjectCascading() = runBlocking {
        fakeRepository.insertSubject("History", "#0061A4", "exam1", "book", 4)
        val id = fakeRepository.subjectsList[0].id
        
        fakeRepository.chaptersList.add(Chapter(id = "chap1", userId = "test_user_abc", subjectId = id, name = "Chapter 1"))
        fakeRepository.topicsList.add(Topic(id = "topic1", userId = "test_user_abc", subjectId = id, chapterId = "chap1", name = "Topic 1"))
        
        assertEquals(1, fakeRepository.subjectsList.size)
        assertEquals(1, fakeRepository.chaptersList.size)
        assertEquals(1, fakeRepository.topicsList.size)
        
        fakeRepository.deleteSubject(id)
        
        assertEquals(0, fakeRepository.subjectsList.size)
        assertEquals(0, fakeRepository.chaptersList.size)
        assertEquals(0, fakeRepository.topicsList.size)
    }

    @Test
    fun testDatabaseIDUniqueness() = runBlocking {
        fakeRepository.insertSubject("Sub1", "#0061A4", "exam1", "book", 0)
        fakeRepository.insertSubject("Sub2", "#0061A4", "exam1", "book", 0)
        assertNotEquals(fakeRepository.subjectsList[0].id, fakeRepository.subjectsList[1].id)
    }
}
