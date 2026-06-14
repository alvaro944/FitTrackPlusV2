# Plan: Health Connect — Integración de pasos

> **Para Codex:** Lee la spec completa antes de empezar. Crea la rama desde main.
> No lances el emulador — la verificación manual la hace el usuario.
> El emulador estándar de Android Studio no tiene Health Connect; el usuario
> prueba en dispositivo real.

**Rama:** `codex/health-connect-steps` (crear desde main)
**Spec:** `docs/superpowers/specs/2026-06-12-health-connect-steps.md`

---

## Tarea 1: Dependencia y permiso

**Archivos:**
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`

- [ ] Añadir `androidx.health.connect:connect-client` a dependencias
- [ ] Declarar `<uses-permission android:name="android.permission.health.READ_STEPS" />`
      en `AndroidManifest.xml`
- [ ] Declarar el `<queries>` necesario para detectar si Health Connect está instalado
- [ ] Verificar con `./gradlew build`

---

## Tarea 2: Repositorio y modelos de dominio

**Archivos:**
- Create: `data/health/HealthConnectRepository.kt`
- Create: `domain/model/StepsData.kt`
- Create: `domain/usecase/ReadDailyStepsUseCase.kt`
- Create: `domain/usecase/ReadWeeklyStepsUseCase.kt`

- [ ] `StepsData`: `data class StepsData(val todaySteps: Long, val weekDaySteps: Map<Int, Long>)`
      donde la clave del Map es el índice 0–6 (lunes=0)
- [ ] `HealthConnectRepository`: métodos `isAvailable()`, `hasPermission()`,
      `requestPermission()`, `readTodaySteps(): Long`, `readWeekSteps(): Map<Int, Long>`
- [ ] `ReadDailyStepsUseCase`: lee pasos de hoy y los días de la semana actual
- [ ] Los use cases devuelven `null` si no hay permiso o no hay datos — nunca lanzan
- [ ] Verificar con `./gradlew build`

---

## Tarea 3: Persistencia del objetivo y estado de conexión

**Archivos:**
- `data/preferences/UserPreferencesRepository.kt`
- `data/preferences/UserPreferences.kt` (o proto/DataStore equivalente)

- [ ] Añadir `dailyStepGoal: Int` (por defecto 10.000) a las preferencias de usuario
- [ ] Añadir `healthConnectConnected: Boolean` para recordar si el usuario ya conectó
- [ ] Exponer ambos como `Flow` desde `UserPreferencesRepository`
- [ ] Verificar con `./gradlew build`

---

## Tarea 4: Inyección de dependencias

**Archivos:**
- `di/DatabaseModule.kt` o crear `di/HealthModule.kt`

- [ ] Proveer `HealthConnectRepository` como singleton con Hilt
- [ ] Inyectar `HealthConnectClient` desde `HealthConnectClient.getOrCreate(context)`
- [ ] Verificar con `./gradlew build`

---

## Tarea 5: Ajustes — conexión y objetivo

**Archivos:**
- `feature/settings/SettingsScreen.kt`
- `feature/settings/SettingsViewModel.kt`

- [ ] Añadir sección "Salud y actividad" en SettingsScreen
- [ ] Botón "Conectar con Health Connect" (visible si no conectado)
- [ ] Si Health Connect no está instalado: texto + botón que abre Play Store
- [ ] Campo editable para el objetivo de pasos diarios (stepper o texto numérico)
- [ ] Botón "Desconectar" (visible si ya conectado)
- [ ] El ViewModel lanza el flow de permisos y actualiza el estado en DataStore
- [ ] Verificar con `./gradlew test` + `./gradlew build`

---

## Tarea 6: Inicio — widget de pasos y calendario actualizado

**Archivos:**
- `feature/home/HomeViewModel.kt`
- `feature/home/HomeScreen.kt`

- [ ] Añadir a `HomeUiState`:
  - `todaySteps: Long?` (null si sin datos)
  - `dailyStepGoal: Int`
  - `stepsDaysCompleted: Set<Int>` (días de la semana con objetivo alcanzado)
- [ ] El ViewModel combina los flows de pasos + preferencias + sesiones
- [ ] En `HomeScreen`, añadir la fila de progreso de pasos debajo del calendario
      (solo visible si `todaySteps != null`)
- [ ] Actualizar `WeekDayCell` para combinar `trainedDaysThisWeek` y `stepsDaysCompleted`:
  - Solo entreno: primary (verde actual)
  - Solo pasos: azul accent (`MaterialTheme.colorScheme.tertiary` o similar)
  - Ambos: dorado (`accentWarm`)
  - Ninguno: gris (actual)
- [ ] Verificar con `./gradlew test` + `./gradlew build`

---

## Tarea 7: Estadísticas — tarjeta de actividad semanal

**Archivos:**
- `feature/stats/StatsScreen.kt`
- `feature/stats/StatsViewModel.kt`

- [ ] Añadir `weeklySteps: Long?` y `stepGoalDaysCompleted: Int` a `StatsUiState`
- [ ] El ViewModel lee los datos del use case de pasos semanales
- [ ] En `StatsScreen`, añadir tarjeta "Actividad esta semana" (solo visible si hay datos):
  - Total pasos de la semana
  - "X de 7 días con objetivo completado"
- [ ] Verificar con `./gradlew test` + `./gradlew build`

---

## Tarea 8: Commit y push

- [ ] `./gradlew test --no-daemon --console=plain` — todos en verde
- [ ] `./gradlew build --no-daemon --console=plain` — build limpio
- [ ] Commit limpio en `codex/health-connect-steps`
- [ ] Push y avisar al usuario para prueba en dispositivo real
