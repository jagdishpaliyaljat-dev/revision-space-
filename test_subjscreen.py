import re
with open("app/src/main/java/com/example/ui/screens/subject/SubjectsScreen.kt", "r") as f:
    text = f.read()
match = re.search(r'viewModel.insertSubject.*?\)', text, re.DOTALL)
if match:
    print(match.group(0))
