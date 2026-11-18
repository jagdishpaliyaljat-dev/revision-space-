package com.example.viewmodel

class RevisionViewModel {
    private val cachedTopicsDetails = java.util.concurrent.ConcurrentHashMap<String, com.example.data.TopicWithSubjectAndChapter>()
    private val cachedTopics = java.util.concurrent.ConcurrentHashMap<String, com.example.data.Topic>()
    private var lastChaptersMap = emptyMap<String, com.example.data.Chapter>()
    private var lastSubjectsMap = emptyMap<String, com.example.data.Subject>()
}
