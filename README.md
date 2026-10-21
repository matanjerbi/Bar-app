# Bar

Personal Android cocktail app. Offline, no AI: every recipe and every generated riff comes from vetted specs and proven templates.

**Install:** [Bar.apk](https://github.com/matanjerbi/Bar-app/releases/latest/download/Bar.apk). Every push to `main` builds a new APK (GitHub Actions) and replaces it on the `latest` release; it installs as an update and keeps your data.

## What it does
- **My bar** – tick what you have. Bottles satisfy their categories (bourbon counts as "American whiskey" and "whiskey"); ice, sugar, salt and water are always assumed.
- **Make now** – what you can make, what is one ingredient away, and what to buy (or make) next ranked by how many drinks it unlocks.
- **Catalog** – ~75 classics and modern classics (sours with foam, equal-parts, stirred, tiki, highballs, spritzes) with filters by base, family and style.
- **Recipe** – exact specs, ml/oz, servings scaling, full technique (dry shake / reverse dry shake, stir times, double strain, rinses, floats), estimated ABV and standard drinks after dilution.
- **Homemade syrups** – recipes, what you need, shelf life and "made X days ago" tracking.
- **Builder** – new riffs only inside proven templates (sour 2 : ¾ : ¾, old fashioned, highball) and whitelisted pairings, with a pointer to the closest classic.
- **Taste** – ratings, favourites and liked styles reorder the recommendations.
- **My recipes** – save riffs or write your own; JSON backup/restore.

## Code
Kotlin, Jetpack Compose (Material 3), Room. Recipes: `logic/Cocktails.kt`, ingredients: `logic/Ingredients.kt`, templates: `logic/Builder.kt`.
Tests check that every recipe references real ingredients, matching, suggestions, ABV and the builder: `./gradlew testDebugUnitTest`.
