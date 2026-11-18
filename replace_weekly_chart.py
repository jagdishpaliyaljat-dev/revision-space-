import re

with open('app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Define the new function
new_section = """@Composable
fun WeeklyVisualizationSection(metrics: com.example.data.AnalyticsMetrics) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.InsertChart, contentDescription = null, tint = DashPurplePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("SESSIONS OVER TIME", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DashTextDark)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                val weeklyData = metrics.weeklyActivity
                if (weeklyData.isEmpty()) {
                    Text("No study data for this period", modifier = Modifier.padding(20.dp), color = DashTextGray)
                    return@Box
                }

                val modelProducer = remember { com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer.build() }
                LaunchedEffect(weeklyData) {
                    modelProducer.tryRunTransaction {
                        lineSeries {
                            series(weeklyData.map { it.sessions.takeIf { s -> s > 0 } ?: 0 })
                        }
                    }
                }
                
                com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost(
                    chart = com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart(
                        com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer(),
                        startAxis = com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis(),
                        bottomAxis = com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis(
                            valueFormatter = { value, _, _ -> 
                                weeklyData.getOrNull(value.toInt())?.day ?: ""
                            }
                        )
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }
    }
}"""

# Find WeeklyVisualizationSection and replace it
# The original function goes from fun WeeklyVisualizationSection(...) to the end of its block.
# We will use regex to find it.
pattern = r'@Composable\s*fun WeeklyVisualizationSection\(metrics: com\.example\.data\.AnalyticsMetrics\).*?(?=\n@Composable\s*fun BulletRow)'
content = re.sub(pattern, new_section + "\n", content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt', 'w') as f:
    f.write(content)

