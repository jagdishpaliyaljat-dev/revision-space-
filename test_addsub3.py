with open("app/src/main/java/com/example/viewmodel/RevisionViewModel.kt", "r") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if "fun addSubject" in line and "addSubjectTest" not in line:
        for j in range(i, min(i+15, len(lines))):
            print(f"{j+1}: {lines[j]}", end="")
        break
