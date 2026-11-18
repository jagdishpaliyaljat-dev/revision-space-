import re
with open("app/src/main/java/com/example/viewmodel/RevisionViewModel.kt", "r") as f:
    text = f.read()
match = re.search(r'fun addSubject.*?}', text, re.DOTALL)
if match:
    print(match.group(0))
