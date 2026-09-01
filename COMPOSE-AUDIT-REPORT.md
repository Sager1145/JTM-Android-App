# Jetpack Compose Audit Report

Target: `/Users/sager/Documents/GitHub/JTM-Android-App`
Date: 2026-08-31
Scope: `app/src/main`, `app/build.gradle.kts`, `core/src/main`, and navigation/theme resources
Excluded from scoring: `**/test/**`, `**/androidTest/**`, preview-only code, generated `build/**`
Confidence: Medium — the complete production Compose surface was inspected, but this is a small first-version app
Overall Score: 87/100

## Scorecard

| Category | Score | Weight | Status | Notes |
|----------|-------|--------|--------|-------|
| Performance | 8/10 | 35% | solid | 100% of named composables are skippable; future list growth needs attention |
| State management | 8/10 | 25% | solid | Clear UDF and lifecycle collection; persistence is intentionally not implemented yet |
| Side effects | 10/10 | 20% | excellent | Composition is side-effect free and navigation calls are guarded event handlers |
| Composable API quality | 9/10 | 20% | excellent | Stateless screens/components follow modifier and callback conventions |

## Critical Findings

None after the audit fixes. The audit moved map point derivation out of composition, cached line geometry in `core`, deferred viewport reads to the Canvas draw phase, removed per-composition navigation item allocation, and removed direct string formatting from composition.

## Adjacent Findings

### Android Launch UX

- Android 12+ splash icon status: not configured; the app currently uses the platform default launch treatment and launcher icon.
- Evidence: `app/src/main/res/values/themes.xml` has no `windowSplashScreenAnimatedIcon` or `postSplashScreenTheme` item.
- Follow-up: add a dedicated splash theme and animated-vector wrapper when branded launch UX becomes part of the product scope.
- References: <https://developer.android.com/develop/ui/views/launch/splash-screen>

## Category Details

### Performance — 8/10

**Ceiling check**

- Strong Skipping: on (Kotlin 2.3.20; compiler flag confirmed in module metrics)
- Ceiling table applied: SSM-on
- Module-wide `skippable%`: 66/80 = 82.5% (includes compiler-generated composable lambdas)
- Named-only `skippable%`: 13/13 = 100%; this is the binding metric
- Unstable shared types from compiler: `JtmUiState`, `JtmViewModel`, and `MainActivity`; only `JtmUiState` crosses a reusable composable boundary
- SSM-on binding evidence: no repeated instance-recreation churn or broken equality observed in production composable bodies
- Qualitative score: 8/10
- Ceiling: none
- Applied score: 8/10

**What is working**

- Every moving lazy collection uses a stable domain ID and a `contentType`.
- Map pan/zoom values are read inside the Canvas draw block, so gesture updates invalidate drawing without recomposing the screen.
- Line geometry and selected journey geometry are calculated in the core/ViewModel layers rather than in composition.
- `enableEdgeToEdge()` is used and no Accompanist system UI dependency is present.

**What is hurting the score**

- `JtmUiState` contains three regular `List` values. Strong Skipping keeps the composable skippable, but equality may become more expensive as the journey collection grows; screen-specific state or persistent immutable collections would reduce that future cost.
- Release minification and a baseline profile are not configured yet, which is reasonable for this MVP but leaves startup/runtime optimization work for production.

**Animation performance signals**

- Status: clean; no custom value animations or infinite transitions are present.

**Paging list signals**

- Status: not present.

**Evidence**

- `app/src/main/java/com/sager/jtm/ui/JourneyListScreen.kt:100` and `PassportScreen.kt:127` — stable keys and content types are present. References: <https://developer.android.com/develop/ui/compose/lists>
- `app/src/main/java/com/sager/jtm/ui/RailMapScreen.kt:194` — viewport state is read in the draw phase. References: <https://developer.android.com/develop/ui/compose/performance/phases>
- `app/src/main/java/com/sager/jtm/JtmViewModel.kt:16` — raw lists make `JtmUiState` compiler-unstable; low risk at current scale, but worth revisiting as data grows. References: <https://developer.android.com/develop/ui/compose/performance/stability/strongskipping>
- `app/build/compose_audit/release/app-module.json` and `app/build/compose_audit/app-composables.csv` — compiler metrics confirm Strong Skipping and 100% named-only skippability. References: <https://developer.android.com/develop/ui/compose/performance/tooling>

### State Management — 8/10

**What is working**

- `JtmViewModel` owns a single immutable `JtmUiState` stream and exposes only `StateFlow`.
- `JtmApp` collects with `collectAsStateWithLifecycle()` and passes values/events to stateless content.
- Local transient form and viewport values use `rememberSaveable`.
- Navigation 3 uses top-level serializable keys; the UI layer owns the back stack.

**What is hurting the score**

- The ledger is in memory. Added journeys and completion changes intentionally reset after process/app restart; a repository plus Room/DataStore is required before calling the data durable.
- Search and selected-journey state live in the ViewModel without `SavedStateHandle`, so they also reset after process death.

**Paging load-state signals**

- Status: not present.

**Evidence**

- `app/src/main/java/com/sager/jtm/JtmViewModel.kt:29` and `JtmApp.kt:35` — one-way state flow with lifecycle-aware collection. References: <https://developer.android.com/develop/ui/compose/architecture>, <https://developer.android.com/develop/ui/compose/state>
- `app/src/main/java/com/sager/jtm/ui/JourneyListScreen.kt:58` and `RailMapScreen.kt:66` — recreation-worthy local UI state uses `rememberSaveable`. References: <https://developer.android.com/develop/ui/compose/state>
- `app/src/main/java/com/sager/jtm/NavigationKeys.kt:10` and `JtmApp.kt:56` — top-level serializable Navigation 3 keys and UI-owned back stack. References: <https://developer.android.com/guide/navigation/navigation-3>
- `app/src/main/java/com/sager/jtm/JtmViewModel.kt:29` — in-memory ledger and non-persisted selection are the remaining durability gap. References: <https://developer.android.com/develop/ui/compose/state-hoisting>

### Side Effects — 10/10

**What is working**

- No IO, coroutine launch, repository call, or navigation mutation occurs directly in a composable body.
- All forward navigation is triggered by click events and protected with `dropUnlessResumed`.
- Pointer gesture work is event-driven and stays inside `pointerInput`.

**What is hurting the score**

- No scored issue found in the current surface.

**Animation side-effect signals**

- Status: not present.

**Paging side-effect signals**

- Status: not present.

**Evidence**

- `app/src/main/java/com/sager/jtm/JtmApp.kt:59` — Navigation 3 mutations are guarded, event-driven callbacks. References: <https://developer.android.com/guide/navigation/navigation-3>, <https://developer.android.com/develop/ui/compose/side-effects>
- `app/src/main/java/com/sager/jtm/ui/RailMapScreen.kt:192` — transform handling is scoped to the pointer event lifecycle. References: <https://developer.android.com/develop/ui/compose/side-effects>

### Composable API Quality — 9/10

**What is working**

- Screens and `JourneyCard` accept values plus `onXxx` events, never `MutableState` or a ViewModel.
- Reusable functions expose `modifier: Modifier = Modifier` and apply it at the root.
- UI text is resource-backed, and previews cover phone/tablet layouts.

**What is hurting the score**

- Spacing values are locally consistent but still expressed as repeated `dp` literals. A formal spacing token layer would make a larger component library easier to evolve.

**Evidence**

- `app/src/main/java/com/sager/jtm/ui/JourneyCard.kt:32` — stateless, event-oriented reusable component with a root modifier. References: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- `app/src/main/java/com/sager/jtm/ui/PassportScreen.kt:43` and `JourneyListScreen.kt:48` — required data/events are explicit and screen state is hoisted. References: <https://developer.android.com/develop/ui/compose/api-guidelines>
- `app/src/main/java/com/sager/jtm/ui/JourneyCard.kt:52` and `PassportScreen.kt:76` — repeated spacing literals are the remaining design-token gap. References: <https://developer.android.com/develop/ui/compose/designsystems/material3>

## Prioritized Fixes

1. Add a repository backed by Room/DataStore and restore query/selection with `SavedStateHandle`. References: <https://developer.android.com/develop/ui/compose/state-hoisting>
2. Adopt persistent immutable UI collections or split `JtmUiState` into destination-specific state before journey counts become large. References: <https://developer.android.com/develop/ui/compose/performance/stability/fix>
3. Add a baseline-profile module and enable R8 for release builds before production distribution. References: <https://developer.android.com/develop/ui/compose/performance/baseline-profiles>
4. Expand visual regression coverage to dark mode, font scale, and tablet screenshots. References: <https://developer.android.com/develop/ui/compose/tooling/previews>

## Notes And Limits

- The full current production Compose surface was audited; generated code, previews, and tests were excluded from scoring.
- Confidence is Medium because the codebase is deliberately small and contains no async/paging/animation surface.
- Adjacent coverage notes: one device Compose navigation test, phone/tablet previews, no screenshot-golden suite, no focus/D-pad/KMP surface.
- Android Launch UX resources: not configured; `app/src/main/res/values/themes.xml` uses a basic no-action-bar theme.
- Strong Skipping mode: on by default and confirmed by compiler output.
- Weight choice: default 35/25/20/20.
- Renormalization: none.
- Compiler diagnostics used: yes; `app/build/compose_audit/release/app-module.json`, `app/build/compose_audit/app-composables.csv`, and `app/build/compose_audit/app-classes.txt` contributed. The SSM-on ceiling table used the named-only 13/13 metric.

## Suggested Follow-Up

- Run a deeper `material-3` audit when the visual language expands beyond these three screens.
- Run `compose-agent focus on testing` when screenshot tests or platform-service fakes are introduced.
