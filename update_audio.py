import sys

with open('app/src/main/java/com/example/ui/TodoListScreen.kt', 'r') as f:
    content = f.read()

# For pending quick tasks (line 240)
content = content.replace(
    'delay(350)\n                                                        val updated = task.copy(',
    'AudioFeedback.playCompletionSound(context)\n                                                        delay(350)\n                                                        val updated = task.copy('
)

# For pending revisions (line 301)
content = content.replace(
    'delay(350)\n                                                        showSuccessEffect = true\n                                                        viewModel.markRevisionComplete(rev.id)',
    'AudioFeedback.playCompletionSound(context)\n                                                        delay(350)\n                                                        showSuccessEffect = true\n                                                        viewModel.markRevisionComplete(rev.id)'
)

# For overdue revisions dialog (line 539)
content = content.replace(
    'delay(350)\n                                showSuccessEffect = true\n                                viewModel.markRevisionComplete(targetId)',
    'AudioFeedback.playCompletionSound(context)\n                                delay(350)\n                                showSuccessEffect = true\n                                viewModel.markRevisionComplete(targetId)'
)

with open('app/src/main/java/com/example/ui/TodoListScreen.kt', 'w') as f:
    f.write(content)
