#!/bin/bash

# Create new package directories
mkdir -p app/src/main/java/com/example/ui/screens/auth
mkdir -p app/src/main/java/com/example/ui/screens/home
mkdir -p app/src/main/java/com/example/ui/screens/revision
mkdir -p app/src/main/java/com/example/ui/screens/subject
mkdir -p app/src/main/java/com/example/ui/screens/profile
mkdir -p app/src/main/java/com/example/ui/screens/rewards
mkdir -p app/src/main/java/com/example/ui/screens/settings
mkdir -p app/src/main/java/com/example/ui/screens/admin
mkdir -p app/src/main/java/com/example/ui/components
mkdir -p app/src/main/java/com/example/ui/utils

# Move files
mv app/src/main/java/com/example/ui/SignInScreen.kt app/src/main/java/com/example/ui/screens/auth/
mv app/src/main/java/com/example/ui/SplashScreen.kt app/src/main/java/com/example/ui/screens/auth/
mv app/src/main/java/com/example/ui/ErrorScreen.kt app/src/main/java/com/example/ui/screens/auth/

mv app/src/main/java/com/example/ui/DashboardScreen.kt app/src/main/java/com/example/ui/screens/home/

mv app/src/main/java/com/example/ui/AddRevisionScreen.kt app/src/main/java/com/example/ui/screens/revision/
mv app/src/main/java/com/example/ui/RevisionListScreen.kt app/src/main/java/com/example/ui/screens/revision/
mv app/src/main/java/com/example/ui/TodayRevisionScreen.kt app/src/main/java/com/example/ui/screens/revision/
mv app/src/main/java/com/example/ui/RevisionSettingsScreen.kt app/src/main/java/com/example/ui/screens/revision/
mv app/src/main/java/com/example/ui/SessionCompleteScreen.kt app/src/main/java/com/example/ui/screens/revision/
mv app/src/main/java/com/example/ui/TimerScreen.kt app/src/main/java/com/example/ui/screens/revision/

mv app/src/main/java/com/example/ui/SubjectsScreen.kt app/src/main/java/com/example/ui/screens/subject/
mv app/src/main/java/com/example/ui/SubjectDetailsScreen.kt app/src/main/java/com/example/ui/screens/subject/
mv app/src/main/java/com/example/ui/TopicListScreen.kt app/src/main/java/com/example/ui/screens/subject/
mv app/src/main/java/com/example/ui/TopicDetailScreen.kt app/src/main/java/com/example/ui/screens/subject/
mv app/src/main/java/com/example/ui/SyllabusScreen.kt app/src/main/java/com/example/ui/screens/subject/
mv app/src/main/java/com/example/ui/TodoListScreen.kt app/src/main/java/com/example/ui/screens/subject/
mv app/src/main/java/com/example/ui/MyTestScoresScreen.kt app/src/main/java/com/example/ui/screens/subject/

mv app/src/main/java/com/example/ui/ProfileScreen.kt app/src/main/java/com/example/ui/screens/profile/
mv app/src/main/java/com/example/ui/AnalyticsScreen.kt app/src/main/java/com/example/ui/screens/profile/
mv app/src/main/java/com/example/ui/CalendarScreen.kt app/src/main/java/com/example/ui/screens/profile/
mv app/src/main/java/com/example/ui/LeaderboardScreen.kt app/src/main/java/com/example/ui/screens/profile/
mv app/src/main/java/com/example/ui/NotificationsScreen.kt app/src/main/java/com/example/ui/screens/profile/
mv app/src/main/java/com/example/ui/HelpAndSupportScreen.kt app/src/main/java/com/example/ui/screens/profile/

mv app/src/main/java/com/example/ui/CoinHistoryScreen.kt app/src/main/java/com/example/ui/screens/rewards/
mv app/src/main/java/com/example/ui/WithdrawalScreen.kt app/src/main/java/com/example/ui/screens/rewards/
mv app/src/main/java/com/example/ui/StreakDetailsScreen.kt app/src/main/java/com/example/ui/screens/rewards/

mv app/src/main/java/com/example/ui/SettingsScreen.kt app/src/main/java/com/example/ui/screens/settings/
mv app/src/main/java/com/example/ui/LanguageScreen.kt app/src/main/java/com/example/ui/screens/settings/

mv app/src/main/java/com/example/ui/AdminScreens.kt app/src/main/java/com/example/ui/screens/admin/

mv app/src/main/java/com/example/ui/BottomNavBar.kt app/src/main/java/com/example/ui/components/
mv app/src/main/java/com/example/ui/ChapterSelectionBottomSheet.kt app/src/main/java/com/example/ui/components/
mv app/src/main/java/com/example/ui/DeleteSubjectDialog.kt app/src/main/java/com/example/ui/components/
mv app/src/main/java/com/example/ui/PdfPreviewDialog.kt app/src/main/java/com/example/ui/components/
mv app/src/main/java/com/example/ui/TagComponents.kt app/src/main/java/com/example/ui/components/
mv app/src/main/java/com/example/ui/CheckmarkExplosionEffect.kt app/src/main/java/com/example/ui/components/
mv app/src/main/java/com/example/ui/StreakSuccessEffect.kt app/src/main/java/com/example/ui/components/
mv app/src/main/java/com/example/ui/CongratsBadgeLottie.kt app/src/main/java/com/example/ui/components/
mv app/src/main/java/com/example/ui/GlobalScreens.kt app/src/main/java/com/example/ui/components/

mv app/src/main/java/com/example/ui/AnimationHelpers.kt app/src/main/java/com/example/ui/utils/
mv app/src/main/java/com/example/ui/AmbientSoundPlayer.kt app/src/main/java/com/example/ui/utils/
mv app/src/main/java/com/example/ui/AudioFeedback.kt app/src/main/java/com/example/ui/utils/
mv app/src/main/java/com/example/ui/ExportUtils.kt app/src/main/java/com/example/ui/utils/
mv app/src/main/java/com/example/ui/SecureStorage.kt app/src/main/java/com/example/ui/utils/
mv app/src/main/java/com/example/ui/TimeFormatter.kt app/src/main/java/com/example/ui/utils/

# Now update package declarations
find app/src/main/java/com/example/ui/screens/auth -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.screens.auth/g' {} +
find app/src/main/java/com/example/ui/screens/home -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.screens.home/g' {} +
find app/src/main/java/com/example/ui/screens/revision -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.screens.revision/g' {} +
find app/src/main/java/com/example/ui/screens/subject -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.screens.subject/g' {} +
find app/src/main/java/com/example/ui/screens/profile -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.screens.profile/g' {} +
find app/src/main/java/com/example/ui/screens/rewards -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.screens.rewards/g' {} +
find app/src/main/java/com/example/ui/screens/settings -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.screens.settings/g' {} +
find app/src/main/java/com/example/ui/screens/admin -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.screens.admin/g' {} +
find app/src/main/java/com/example/ui/components -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.components/g' {} +
find app/src/main/java/com/example/ui/utils -type f -name "*.kt" -exec sed -i 's/package com.example.ui/package com.example.ui.utils/g' {} +

# Function to add import to file
add_import() {
    local file=$1
    local pkg=$2
    if ! grep -q "import $pkg" "$file"; then
        sed -i "/package /a \import $pkg" "$file"
    fi
}

export -f add_import

# Navigation.kt needs to import all these new packages
# Let's just find and replace in Navigation.kt
sed -i '/package com.example.ui/a \
import com.example.ui.screens.auth.*\
import com.example.ui.screens.home.*\
import com.example.ui.screens.revision.*\
import com.example.ui.screens.subject.*\
import com.example.ui.screens.profile.*\
import com.example.ui.screens.rewards.*\
import com.example.ui.screens.settings.*\
import com.example.ui.screens.admin.*\
import com.example.ui.components.*\
import com.example.ui.utils.*' app/src/main/java/com/example/ui/Navigation.kt

# Replace other usage across all files (import com.example.ui.* should be expanded if necessary)
# Since everything in com.example.ui is moved, we will add * imports in all files in com.example.ui and subdirectories
find app/src/main/java/com/example/ui -type f -name "*.kt" -exec sed -i '/package /a \
import com.example.ui.screens.auth.*\
import com.example.ui.screens.home.*\
import com.example.ui.screens.revision.*\
import com.example.ui.screens.subject.*\
import com.example.ui.screens.profile.*\
import com.example.ui.screens.rewards.*\
import com.example.ui.screens.settings.*\
import com.example.ui.screens.admin.*\
import com.example.ui.components.*\
import com.example.ui.utils.*' {} +

