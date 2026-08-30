# FlashLearn Stage 21 - UI Redesign

## Overview
Stage 21 implements a complete redesign of three core screens with improved layouts, colors, and user experience:
- **Home Screen** - Streak display, language flags, colored review queue
- **Review Type Selection** - Compact grouped layout with Daily/Weekly/Monthly, Random, Difficulty levels, and Learned
- **Settings Screen** - Compact appearance options with theme selection, language pair, backup, and AI settings

## Files Included

### Screens
- `HomeScreen.kt` - Home screen with redesigned layout
- `ReviewTypeSelectionScreen.kt` - Review selection with new grouping
- `SettingsScreen.kt` - Settings with compact appearance section

### ViewModels
- `ViewModels.kt` - HomeViewModel, ReviewTypeSelectionViewModel, SettingsViewModel

### Theme & Components
- `Color.kt` - Updated color palette (Blue primary, removed purple)
- `Components.kt` - Reusable composables (ReviewQueueCard, SessionTypeButton, etc.)

## Installation Instructions

### 1. Update Your Project Structure
```bash
cd ~/Flash
```

### 2. Copy Files to Correct Locations

#### Screens
```bash
cp HomeScreen.kt app/src/main/java/com/app/flashlearn/ui/home/
cp ReviewTypeSelectionScreen.kt app/src/main/java/com/app/flashlearn/ui/review/
cp SettingsScreen.kt app/src/main/java/com/app/flashlearn/ui/settings/
```

#### ViewModels
```bash
cp ViewModels.kt app/src/main/java/com/app/flashlearn/ui/
```

#### Theme & Components
```bash
cp Color.kt app/src/main/java/com/app/flashlearn/ui/theme/
cp Components.kt app/src/main/java/com/app/flashlearn/ui/components/
```

### 3. Update Existing Files

#### Update Navigation Graph
In `ui/navigation/NavGraph.kt`, add or update routes:
```kotlin
composable(route = "home") {
    HomeScreen(
        onReviewClick = { reviewType -> ... },
        onStatisticsClick = { ... }
    )
}

composable(route = "review_type_selection") {
    ReviewTypeSelectionScreen(
        onReviewStart = { reviewType, answerMode -> ... }
    )
}

composable(route = "settings") {
    SettingsScreen(
        onBackupClick = { ... },
        onAISettingsClick = { ... }
    )
}
```

#### Update Module Dependencies (if needed)
Ensure your `build.gradle.kts` includes:
```gradle
dependencies {
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.1.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### 4. Update Theme Configuration
In `ui/theme/Theme.kt`, ensure colors are applied:
```kotlin
private val lightColorScheme = lightColorScheme(
    primary = LightPrimary,              // Blue #3B82F6
    secondary = LightSecondary,          // Teal #14B8A6
    tertiary = LightTertiary,            // Purple #7C3AED
    background = LightBackground,        // #FAFAFA
    surface = LightSurface,              // White #FFFFFF
    error = LightError,                  // Red #EF4444
    // ... other colors
)
```

## Design Changes

### Color Scheme
- **Primary**: Blue (#3B82F6) - replaced purple
- **Secondary**: Teal (#14B8A6)
- **Status Colors**:
  - Daily: Yellow (#FCD34D) ☀️
  - Weekly: Blue (#60A5FA) 📅
  - Monthly: Purple (#A78BFA) 🌙
  - Random: Cyan (#0EA5E9) 🎲

### Home Screen
- Greeting with emoji
- Language flags positioned on right
- Gradient "Today's Learning" card (Teal → Purple)
- Separated Streak (fire) and Mastered stats
- Colored review queue cards (Daily/Weekly/Monthly)

### Review Type Selection
- **Top section**: Answer mode (Multiple Choice / Typing)
- **Row 1**: Daily, Weekly, Monthly (3-column compact grid)
- **Row 2**: Random (full-width standalone with gradient)
- **Row 3**: Difficulty levels (Easy, Medium, Hard, Very Hard in 2×2 grid)
- **Row 4**: Learned (full-width standalone)

### Settings
- Compact theme buttons (Light/Dark/System) - 3 columns
- Language pair selector card
- Backup & Import/Export card
- AI Settings card
- Version info at bottom

## Testing

### Unit Tests
Update test files to match new ViewModel structure:
```kotlin
// In HomeViewModelTest.kt
@Test
fun testHomeDataLoading() {
    val viewModel = HomeViewModel(...)
    viewModel.uiState.collectAsState().value.run {
        assertEquals(expectedStreakValue, streak)
        assertEquals(expectedMasteredCount, masteredCount)
    }
}
```

### UI Tests
Test screen rendering:
```kotlin
// In HomeScreenTest.kt
@Composable
fun testHomeScreenRenders() {
    composeTestRule.setContent {
        HomeScreen(
            onReviewClick = {},
            onStatisticsClick = {}
        )
    }
    composeTestRule.onNodeWithText("Good evening").assertExists()
}
```

## Known Dependencies

The following use cases are required:
- `GetDailyReviewCardsUseCase`
- `GetWeeklyReviewCardsUseCase`
- `GetMonthlyReviewCardsUseCase`
- `GetLearnedCardsUseCase`
- `GetStatisticsUseCase`

Ensure these are implemented in your `domain/usecase/` package.

## Future Phases

**Phase 22**: Implement Flashcard Review Session UI
**Phase 23**: Add Vocabulary List with search/filter
**Phase 24**: Complete Add vocabulary workflow
**Phase 25**: Statistics and Progress screens

## Notes

- All screens use Material 3 theming
- RTL layout support included for Persian text
- Emoji-based icons (no icon library dependency)
- Responsive design for mobile screens
- No breaking changes to existing data layer

## Rollback

If needed to rollback:
```bash
git reset --hard HEAD~1
```

## Support

For questions or issues, refer to the main FlashLearn architecture documentation.
