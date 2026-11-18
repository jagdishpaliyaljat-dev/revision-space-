with open("app/src/main/java/com/example/ui/screens/revision/TodayRevisionScreen.kt", "r") as f:
    lines = f.readlines()

depth = 0
for i, line in enumerate(lines):
    line_num = i + 1
    opens = line.count('{')
    closes = line.count('}')
    old_depth = depth
    depth += opens - closes
    if line_num in [66, 620, 630, 690, 759, 1009, 1154, 1189, 1210, 1273, 1320, 1353, 1982, 2014, 2051, 2078, 2084, 2092, 2106] or depth < 0 or (old_depth > 0 and depth == 0):
        print(f"Line {line_num}: depth={depth} (opens={opens}, closes={closes}) | {line.strip()[:60]}")

