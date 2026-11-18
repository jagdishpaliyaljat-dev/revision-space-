with open("app/src/main/java/com/example/ui/screens/subject/TodoListScreen.kt", "r") as f:
    content = f.read()

# 1. Trigger showSuccessEffect on quick task complete
target_quick = '''                                                        if (success) {
                                                            Toast.makeText(context, "Task completed! 🎉", Toast.LENGTH_SHORT).show()
                                                        }'''

replacement_quick = '''                                                        if (success) {
                                                            showSuccessEffect = true
                                                            Toast.makeText(context, "Task completed! 🎉", Toast.LENGTH_SHORT).show()
                                                        }'''

if target_quick in content:
    content = content.replace(target_quick, replacement_quick)

# 2. Add explosion overlay before closing TodoListScreen Box
target_end = '''                }
            )
        }
    }
}'''

replacement_end = '''                }
            )
        }

        if (showSuccessEffect) {
            CheckmarkExplosionEffect(
                modifier = Modifier.fillMaxSize(),
                title = "Topic Marked as Revised! 🎉",
                onAnimationEnd = { showSuccessEffect = false }
            )
        }
    }
}'''

if target_end in content:
    content = content.replace(target_end, replacement_end)

with open("app/src/main/java/com/example/ui/screens/subject/TodoListScreen.kt", "w") as f:
    f.write(content)

print("Updated TodoListScreen.kt successfully!")
