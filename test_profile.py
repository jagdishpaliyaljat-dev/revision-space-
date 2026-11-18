import re

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "r") as f:
    lines = f.readlines()

# line 1483 is repeated, meaning line 1480 and 1483. Let's see what is there.
print("Line 1480: " + lines[1479].strip())
print("Line 1481: " + lines[1480].strip())
print("Line 1482: " + lines[1481].strip())
print("Line 1483: " + lines[1482].strip())
print("Line 1484: " + lines[1483].strip())
