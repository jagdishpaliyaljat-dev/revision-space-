import re

with open('app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt', 'r') as f:
    content = f.read()

new_section = """
@Composable
fun RecentSessionsListSection(metrics: com.example.data.AnalyticsMetrics) {
    if (metrics.recentSessions.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.History, contentDescription = null, tint = DashPurplePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("RECENT SESSIONS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DashTextDark)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                metrics.recentSessions.take(5).forEachIndexed { index, session ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.topicName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DashTextDark,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = com.example.utils.DateUtils.formatTimestampToDateString(session.completedDateMs),
                                fontSize = 12.sp,
                                color = DashTextGray
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DashPurplePrimary.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "${session.durationMs / 60000}m",
                                color = DashPurplePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (index < minOf(metrics.recentSessions.size, 5) - 1) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}
"""

if "RecentSessionsListSection" not in content:
    content += new_section

# Call it in the list
old_item = """                // 5. WEEKLY VISUALIZATION
                item {
                    WeeklyVisualizationSection(analyticsMetrics)
                }

                // 6. SUBJECT BREAKDOWN"""

new_item = """                // 5. WEEKLY VISUALIZATION
                item {
                    WeeklyVisualizationSection(analyticsMetrics)
                }

                // 5.5. RECENT SESSIONS
                item {
                    RecentSessionsListSection(analyticsMetrics)
                }

                // 6. SUBJECT BREAKDOWN"""

content = content.replace(old_item, new_item)

with open('app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt', 'w') as f:
    f.write(content)
