with open("app/src/main/java/com/example/ui/screens/subject/SubjectsScreen.kt", "r") as f:
    for line in f:
        if "insertSubject" in line or "addSubject" in line:
            print(line.strip())
