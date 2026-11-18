with open("app/src/main/java/com/example/ui/screens/revision/TodayRevisionScreen.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if line.strip().startswith("fun ") or line.strip().startswith("private fun "):
        print(f"Line {i+1}: {line.strip()}")

