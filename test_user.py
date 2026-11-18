import re
with open("app/src/main/java/com/example/data/RevisionRepository.kt", "r") as f:
    text = f.read()
match = re.search(r'val userId: String.*?}', text, re.DOTALL)
if match:
    print(match.group(0))
