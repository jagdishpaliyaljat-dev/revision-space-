with open("app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt", "r") as f:
    text = f.read()
import re
match = re.search(r'sortedExams.*?}', text, re.DOTALL)
if match:
    print(match.group(0))
