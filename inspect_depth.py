with open("app/src/main/java/com/example/ui/screens/revision/TodayRevisionScreen.kt", "r") as f:
    lines = f.readlines()

depth = 0
for i, line in enumerate(lines):
    line_num = i + 1
    opens = line.count('{')
    closes = line.count('}')
    old_depth = depth
    depth += opens - closes
    if line.strip().startswith("@Composable") or line.strip().startswith("fun ") or line.strip().startswith("private fun "):
        print(f"Line {line_num}: depth before={old_depth}, depth after={depth} | {line.strip()[:60]}")

