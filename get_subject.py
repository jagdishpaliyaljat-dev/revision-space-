import re
with open("app/src/main/java/com/example/data/Models.kt", "r") as f:
    text = f.read()
match = re.search(r'data class Subject.*?}', text, re.DOTALL)
if match:
    print(match.group(0))
