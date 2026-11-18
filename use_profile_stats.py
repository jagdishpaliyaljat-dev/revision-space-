import re

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "r") as f:
    content = f.read()

# Add profileStats collection
collect_line = """    val allRevisions by viewModel.allRevisions.collectAsStateWithLifecycle(emptyList())
    val profileStats by viewModel.profileStats.collectAsStateWithLifecycle()"""
content = content.replace("    val allRevisions by viewModel.allRevisions.collectAsStateWithLifecycle(emptyList())", collect_line)

# Remove the old calculation block
old_calc_regex = r"// 3\. Study Time: Calculate from completed study sessions only[\s\S]*?val displayMastery = if \(allChaptersReachedTen\) \{[\s\S]*?else \{[\s\S]*?\}\n"
new_calc = """
    val displayStudyHours = profileStats.displayStudyHours
    val displayStudyTimeStr = profileStats.displayStudyTimeStr
    val displayRevisions = profileStats.displayRevisions
    val completedChaptersCount = profileStats.completedChaptersCount
    val displayMastery = profileStats.displayMastery
"""
content = re.sub(old_calc_regex, new_calc, content)

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "w") as f:
    f.write(content)

