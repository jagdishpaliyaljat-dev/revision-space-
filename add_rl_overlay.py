with open("app/src/main/java/com/example/ui/screens/revision/RevisionListScreen.kt", "r") as f:
    content = f.read()

target = '''            }
        }


    }
}'''

replacement = '''            }
        }

        if (showTopicExplosion) {
            CheckmarkExplosionEffect(
                modifier = Modifier.fillMaxSize(),
                title = completedTopicTitle,
                onAnimationEnd = { showTopicExplosion = false }
            )
        }
    }
}'''

if target in content:
    content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/revision/RevisionListScreen.kt", "w") as f:
    f.write(content)

print("Added overlay to RevisionListScreen!")
