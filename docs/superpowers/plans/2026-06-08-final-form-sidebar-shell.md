# Final Form Sidebar Shell Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Aplicar el shell lateral del diseño final-form a la app actual sin mezclarlo con nuevas features reales.

**Architecture:** Se extraerá una configuración testeable del shell y luego se reconstruirá `FitTrackPlusNavHost` alrededor de una top app bar compartida, un drawer lateral y una bottom nav de cinco tabs. La lógica real de tema, unidad y acceso a ajustes se reutiliza desde los componentes ya presentes en esta rama.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Hilt, JUnit

---

### Task 1: Modelar la configuración del shell

**Files:**
- Create: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/navigation/NavigationShellConfig.kt`
- Test: `app/src/test/java/com/alvarocervantes/fittrackplus/core/navigation/NavigationShellConfigTest.kt`

- [ ] **Step 1: Write the failing test**
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Write minimal implementation**
- [ ] **Step 4: Run test to verify it passes**

### Task 2: Rehacer el shell principal

**Files:**
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/navigation/FitTrackPlusNavHost.kt`
- Create: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/AppShell.kt`

- [ ] **Step 1: Montar top app bar compartida y drawer**
- [ ] **Step 2: Eliminar Ajustes de la bottom nav**
- [ ] **Step 3: Mantener navegación real a tabs y ajustes**

### Task 3: Conectar preferencias reales y acciones futuras

**Files:**
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/AppShell.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/settings/SettingsScreen.kt`

- [ ] **Step 1: Reutilizar tema y unidad reales en el drawer**
- [ ] **Step 2: Añadir acciones futuras visibles con feedback controlado**

### Task 4: Ajustar integración visual y verificar

**Files:**
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/home/HomeScreen.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/routines/RoutinesScreen.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/workout/WorkoutScreen.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/history/HistoryScreen.kt`
- Modify: `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/stats/StatsScreen.kt`

- [ ] **Step 1: Ajustar espaciados/cabeceras para encajar con el shell**
- [ ] **Step 2: Run `./gradlew test`**
- [ ] **Step 3: Run `./gradlew build`**

