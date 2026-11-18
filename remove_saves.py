import re

with open("app/src/main/java/com/example/viewmodel/RevisionViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('''                            try {
                                savePomodoroSession("Completed", durationMs, true, "Focus")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }''', '')

content = content.replace('''                            try {
                                savePomodoroSession("Completed", durationMs, true, "Break")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }''', '')

with open("app/src/main/java/com/example/viewmodel/RevisionViewModel.kt", "w") as f:
    f.write(content)
