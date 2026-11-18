with open('app/src/main/java/com/example/ui/CalendarScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'showSuccessEffect = true\n                                                viewModel.markRevisionComplete(rev.id)',
    'AudioFeedback.playCompletionSound(context)\n                                                showSuccessEffect = true\n                                                viewModel.markRevisionComplete(rev.id)'
)

content = content.replace(
    'showSuccessEffect = true\n                            viewModel.markRevisionComplete(targetId)',
    'AudioFeedback.playCompletionSound(context)\n                            showSuccessEffect = true\n                            viewModel.markRevisionComplete(targetId)'
)

content = content.replace(
    'showSuccessEffect = true\n                        viewModel.markRevisionComplete(targetId)',
    'AudioFeedback.playCompletionSound(context)\n                        showSuccessEffect = true\n                        viewModel.markRevisionComplete(targetId)'
)

with open('app/src/main/java/com/example/ui/CalendarScreen.kt', 'w') as f:
    f.write(content)
