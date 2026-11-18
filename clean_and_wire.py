import re

# 1. Clean TodayRevisionScreen.kt
with open("app/src/main/java/com/example/ui/screens/revision/TodayRevisionScreen.kt", "r") as f:
    tr = f.read()

# Remove all extra CheckmarkExplosionEffect added at end of sub-composables
tr = re.sub(
    r'\n\s*if \(showTopicExplosion\) \{\s*CheckmarkExplosionEffect\([^}]+\)\s*\}',
    '',
    tr
)

# Ensure showTopicExplosion and completedTopicTitle exist in TodayRevisionScreen
if 'var showTopicExplosion by remember { mutableStateOf(false) }' not in tr:
    tr = tr.replace(
        'var showConfetti by remember { mutableStateOf(false) }',
        'var showConfetti by remember { mutableStateOf(false) }\n    var showTopicExplosion by remember { mutableStateOf(false) }\n    var completedTopicTitle by remember { mutableStateOf("Topic Marked as Revised! 🎉") }'
    )

# Add single CheckmarkExplosionEffect overlay at the end of TodayRevisionScreen (before closing brace)
explosion_block = '''
    if (showTopicExplosion) {
        CheckmarkExplosionEffect(
            modifier = Modifier.fillMaxSize(),
            title = completedTopicTitle,
            onAnimationEnd = { showTopicExplosion = false }
        )
    }
'''

if 'if (showTopicExplosion)' not in tr:
    tr = tr.replace(
        '    if (showGoalDialog) {\n        DailyGoalCongratsDialog(\n            goalTarget = settings.dailyGoal,\n            unitName = "revision sessions",\n            streakDays = userProfile?.currentStreak ?: 1,\n            onDismiss = { showGoalDialog = false },\n            onReplayAnimation = { showConfetti = true }\n        )\n    }',
        '    if (showGoalDialog) {\n        DailyGoalCongratsDialog(\n            goalTarget = settings.dailyGoal,\n            unitName = "revision sessions",\n            streakDays = userProfile?.currentStreak ?: 1,\n            onDismiss = { showGoalDialog = false },\n            onReplayAnimation = { showConfetti = true }\n        )\n    }' + explosion_block
    )

# Pass onTopicCompleted to TodayRevisionsView call in TodayRevisionScreen
tr = tr.replace(
    'TodayRevisionsView(\n                viewModel = viewModel,\n                navController = navController,\n                onBack = { currentView = "dashboard" }\n            )',
    'TodayRevisionsView(\n                viewModel = viewModel,\n                navController = navController,\n                onBack = { currentView = "dashboard" },\n                onTopicCompleted = { title ->\n                    completedTopicTitle = title\n                    showTopicExplosion = true\n                }\n            )'
)

# Update TodayRevisionsView signature and quick complete click
tr = tr.replace(
    'fun TodayRevisionsView(\n    viewModel: RevisionViewModel,\n    navController: NavController,\n    onBack: () -> Unit\n) {',
    'fun TodayRevisionsView(\n    viewModel: RevisionViewModel,\n    navController: NavController,\n    onBack: () -> Unit,\n    onTopicCompleted: (String) -> Unit = {}\n) {'
)

tr = tr.replace(
    'var isCompleting by remember { mutableStateOf(false) }\n                                FilledIconButton(\n                                    onClick = {\n                                        isCompleting = true\n                                        completedTopicTitle = "${topic.topicName.take(24)} Marked as Revised! 🎉"\n                                        showTopicExplosion = true\n                                        viewModel.recordTimerSession(\n                                            topic.topicId,\n                                            isRevision = true,\n                                            durationMs = topic.estimatedRevisionTimeMins * 60 * 1000L\n                                        )\n                                    },',
    'var isCompleting by remember { mutableStateOf(false) }\n                                FilledIconButton(\n                                    onClick = {\n                                        isCompleting = true\n                                        onTopicCompleted("${topic.topicName.take(24)} Marked as Revised! 🎉")\n                                        viewModel.recordTimerSession(\n                                            topic.topicId,\n                                            isRevision = true,\n                                            durationMs = topic.estimatedRevisionTimeMins * 60 * 1000L\n                                        )\n                                    },'
)

with open("app/src/main/java/com/example/ui/screens/revision/TodayRevisionScreen.kt", "w") as f:
    f.write(tr)


# 2. Clean RevisionListScreen.kt
with open("app/src/main/java/com/example/ui/screens/revision/RevisionListScreen.kt", "r") as f:
    rl = f.read()

rl = re.sub(
    r'\n\s*if \(showTopicExplosion\) \{\s*CheckmarkExplosionEffect\([^}]+\)\s*\}',
    '',
    rl
)

if 'var showTopicExplosion by remember { mutableStateOf(false) }' not in rl:
    rl = rl.replace(
        'var searchQuery by remember { mutableStateOf("") }',
        'var searchQuery by remember { mutableStateOf("") }\n    var showTopicExplosion by remember { mutableStateOf(false) }\n    var completedTopicTitle by remember { mutableStateOf("Topic Marked as Revised! 🎉") }'
    )

# Add explosion overlay before closing brace of RevisionListScreen Box (around line 278)
rl = rl.replace(
    '            }\n        }\n    }\n}\n\n@Composable\nfun SearchEmptyState',
    '            }\n        }\n\n        if (showTopicExplosion) {\n            CheckmarkExplosionEffect(\n                modifier = Modifier.fillMaxSize(),\n                title = completedTopicTitle,\n                onAnimationEnd = { showTopicExplosion = false }\n            )\n        }\n    }\n}\n\n@Composable\nfun SearchEmptyState'
)

with open("app/src/main/java/com/example/ui/screens/revision/RevisionListScreen.kt", "w") as f:
    f.write(rl)

print("Cleaned and rewired successfully!")
