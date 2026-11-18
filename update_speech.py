import re

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "r") as f:
    content = f.read()

content = content.replace('''    private fun speakCompletionAlert() {
        val speechText = if (currentSessionType.value == "Break") {
            "Break is over. It's time to continue your study session."
        } else {
            "Great job! Your focus session is complete. Time to take a short break."
        }''', '''    private fun speakCompletionAlert() {
        val isHi = java.util.Locale.getDefault().language == "hi"
        val speechText = if (currentSessionType.value == "Break") {
            if (isHi) "ब्रेक खत्म हो गया है। अपनी पढ़ाई जारी रखने का समय आ गया है।"
            else "Break is over. It's time to continue your study session."
        } else {
            if (isHi) "बहुत बढ़िया! आपका फोकस सेशन पूरा हो गया है। अब एक छोटा ब्रेक लेने का समय है।"
            else "Great job! Your focus session is complete. Time to take a short break."
        }''')

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "w") as f:
    f.write(content)
