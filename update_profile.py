import re

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "r") as f:
    content = f.read()

# Add edit button next to name
target = """                                Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = ProfilePurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }"""

replacement = """                                Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = ProfilePurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { showEditProfileDialog = true }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = ProfileTextGray, modifier = Modifier.size(14.dp))
                                }
                            }"""
content = content.replace(target, replacement)

# Add edit button to Exam Goal
target2 = """                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(ProfilePurpleSuperLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Event, contentDescription = null, tint = ProfilePurple, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Target Exam", color = ProfileTextGray, fontSize = 11.sp)
                                        Text(examGoalNameField, color = ProfileTextDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }"""

replacement2 = """                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(ProfilePurpleSuperLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Event, contentDescription = null, tint = ProfilePurple, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Target Exam", color = ProfileTextGray, fontSize = 11.sp)
                                        Text(examGoalNameField, color = ProfileTextDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    IconButton(onClick = { showExamGoalDialog = true }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Exam", tint = ProfileTextGray, modifier = Modifier.size(16.dp))
                                    }"""
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "w") as f:
    f.write(content)

