import re

with open('app/src/main/java/com/example/viewmodel/RevisionViewModel.kt', 'r') as f:
    content = f.read()

old_block = """    val analyticsMetrics = combine(
        combine(allTopicsWithDetails, activeExamId, allStudyLogs, allSubjectTests) { topics, activeId, logs, tests ->
            com.example.data.AnalyticsQuadruple(topics, activeId, logs, tests)
        },
        currentUserProfile,
        analyticsTimeFilter,
        combine(allRevisions, allChapters, allSubjects) { revisions, chapters, subjects ->
            Triple(revisions, chapters, subjects)
        }
    ) { quadruple, profile, timeFilter, triple ->
        val (revisions, chapters, subjects) = triple
        analyticsService.calculateAnalyticsMetrics(
            quadruple.topics,
            quadruple.activeId,
            quadruple.logs,
            profile,
            timeFilter,
            revisions,
            quadruple.tests,
            chapters,
            subjects
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)"""

new_block = """    val analyticsMetrics = combine(
        combine(allTopicsWithDetails, activeExamId, allStudyLogs, allSubjectTests) { topics, activeId, logs, tests ->
            com.example.data.AnalyticsQuadruple(topics, activeId, logs, tests)
        },
        currentUserProfile,
        analyticsTimeFilter,
        combine(allRevisions, allChapters, allSubjects) { revisions, chapters, subjects ->
            Triple(revisions, chapters, subjects)
        },
        repository.recentRevisionHistory
    ) { quadruple, profile, timeFilter, triple, recentHistory ->
        val (revisions, chapters, subjects) = triple
        analyticsService.calculateAnalyticsMetrics(
            quadruple.topics,
            quadruple.activeId,
            quadruple.logs,
            profile,
            timeFilter,
            revisions,
            quadruple.tests,
            chapters,
            subjects,
            recentHistory
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)"""

if old_block in content:
    content = content.replace(old_block, new_block)
else:
    print("Could not find the block to replace!")

with open('app/src/main/java/com/example/viewmodel/RevisionViewModel.kt', 'w') as f:
    f.write(content)
