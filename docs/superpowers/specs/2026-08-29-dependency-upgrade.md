# Spec: Actualizacion de dependencias (Gradle, AGP, SDK, Kotlin, Compose)

**Fecha:** 2026-08-29
**Origen:** `docs/design/auditoria-ronda-3.md`, seccion "Actualizacion de dependencias", mas verificacion en vivo de las cifras contra las release notes reales de Android Developers.
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-29-dependency-upgrade.md`
**Prerequisito:** ninguno tecnico. Se recomienda que `fix/data-loss` este mergeada primero (toca solo ViewModels/logica, no build), para no arrastrar conflictos de rebase sobre una rama de infraestructura.

---

## Por que

`fix/broken-features` (P1) y las rondas de design system que vienen despues van a construir UI nueva (formularios de notas, exportar datos, componentes visuales de la ronda 3). Construir esa UI nueva sobre Compose BOM 2024.04.01 para luego migrarla a 2026.08.00 es hacer el trabajo dos veces. Por eso esta rama va **antes** de `fix/broken-features`, no despues como proponia el orden original de la auditoria.

## Estado actual verificado

Confirmado leyendo `gradle/libs.versions.toml`, `app/build.gradle.kts` y `gradle/wrapper/gradle-wrapper.properties`, y contrastado con busqueda en vivo contra `developer.android.com`:

**Todas las versiones de destino confirmadas contra maven-metadata.xml real (dl.google.com, repo1.maven.org, plugins.gradle.org, services.gradle.org), no solo contra paginas de release notes.**

| Pieza | Ahora (repo) | Destino | Verificado |
|---|---|---|---|
| Compose BOM | 2024.04.01 (material3 1.2.1) | 2026.08.00 (material3 1.4.0, compose 1.12.0) | si — maven-metadata de dl.google.com |
| AGP | 8.5.1 | 9.3.2 (ultimo patch de la 9.3, la 9.3.0 original ya tiene 2 patches) | si — maven-metadata de dl.google.com |
| Gradle | 8.7 | 9.7.1 (ultimo estable; supera el minimo real de AGP 9.3.x, que es 9.5.0) | si — services.gradle.org/versions/current |
| compileSdk / targetSdk | 35 / 35 | 37 / 36 (compileSdk 37 lo exige Compose 1.12.0 explicitamente, no solo AGP) | si — release notes de Compose UI 1.12.0 |
| minSdk | 23 | **26** (decision del dueño, no viene de la auditoria) | — |
| JDK | 17 | 17 (sin cambio) | AGP 9 exige minimo 17, ya lo cumplimos |
| Kotlin | 2.1.0 | **2.1.20** — bump conservador desde la version ya probada en este proyecto, no la ultima absoluta (2.4.10), precisamente porque detekt no certifica oficialmente mas alla de Kotlin 2.0.21 en su ultima version estable | si — maven-metadata de plugins.gradle.org |
| KSP | 2.1.0-1.0.28 | **2.1.20-2.0.1** (pareja exacta publicada para Kotlin 2.1.20) | si — maven-metadata de plugins.gradle.org |
| detekt | 1.23.6 | **1.23.8** (ultimo estable; no existe detekt 2.x estable, solo alpha, y esa alpha exige Kotlin 2.4.x — no se adopta) | si — maven-metadata + detekt.dev/docs/introduction/compatibility |
| Hilt | 2.57.2 | **2.60.1** (ultimo estable — pieza de mayor riesgo de esta migracion, conviene la version con mas fixes de KSP2) | si — maven-metadata de repo1.maven.org |
| Glance / Health Connect | 1.1.0 / 1.1.0-alpha11 | **sin cambio** — no existe release estable mas reciente de ninguna de las dos (Glance solo tiene 1.3.0-alpha02, Health Connect 1.2.0-alpha06) | si — maven-metadata de dl.google.com |

### Matiz importante sobre Material 3 Expressive

La suite completa de Material 3 Expressive (`ButtonGroup`, `SplitButton`, `WavyProgressIndicator`, top bars flexibles, `ToggleButton`, FAB Menu, `SearchBar` por slots, expressive list items) se estabiliza en **material3 1.5.0**, que en la fecha de esta spec sigue en alpha (~alpha26-27). En **1.4.0** (lo que trae esta migracion) esos APIs existen pero marcados `@ExperimentalMaterial3ExpressiveApi`.

El proyecto ya usa opt-in de APIs experimentales (`ExperimentalFoundationApi`), asi que adoptarlos con opt-in es viable — pero es una decision consciente para cuando llegue `refactor/design-system-round-3`, no algo que esta rama resuelva por si sola.

Lo que si llega **estable** con este salto (sin opt-in) y es relevante para trabajo futuro ya especificado:
- **Predictive back** con soporte de framework — resuelve parte de P3 (auditoria ronda 3).
- **Shape morphing** y `MaterialShapes` — util para la jerarquia dia/ejercicio de Rutinas (P4).
- `LoadingIndicator` / `ContainedLoadingIndicator` — cubre el hueco de indicadores de progreso en botones (`isSaving` que hoy solo cambia la etiqueta).
- Mejoras de accesibilidad y semantica (ayuda a P5).
- Mejoras de rendimiento en `LazyColumn` y recomposicion.

### Fecha de Google Play (no aplica a este proyecto por ahora)

Google Play exige targetSdk 36 para apps nuevas y actualizaciones desde finales de agosto de 2026, con prorroga solicitable hasta noviembre. **FitTrackPlus se instala por sideload, no se publica en Play** — esto no es un plazo real para el proyecto hoy. Se sube igualmente porque desbloquea trabajo futuro ya planificado, no por presion de fecha.

### Riesgo real identificado

El salto AGP 8.5→9.x y Gradle 8.7→9.5 cruza dos majors con cambios de ruptura. Puntos concretos de este repo a vigilar:

- `sourceSets { getByName("main") { java.setSrcDirs(...) } }` (`app/build.gradle.kts:69`) — la fuente `src/main/kotlin` en vez de `src/main/java` es una configuracion no estandar que puede necesitar ajuste con el nuevo DSL de AGP 9.
- `providers.exec` para leer git en build time (`app/build.gradle.kts:16`, usado para la version dinamica de la app) — verificar compatibilidad con configuration cache, que AGP 9 endurece.
- detekt 1.23.6 — verificar compatibilidad con el Kotlin destino antes de subir Kotlin en la Etapa B.
- KSP 2.1.0-1.0.28 va atado a la version de Kotlin — deben subir juntos.
- Hilt 2.57.2 con KSP: **este es el punto de mayor riesgo practico**, mas que Compose en si. La codegen de Hilt via KSP es sensible a saltos de Kotlin. Verificar en la Etapa B, de forma aislada (build limpio + `sh gradlew build` completo, no solo `test`), antes de pasar a la Etapa C — un problema de KSP/Hilt que se arrastra sin detectar hasta la Etapa C se confunde facilmente con un problema de Compose.
- Glance 1.1.0 y Health Connect 1.1.0-alpha11 — revisar si tienen version mas reciente compatible antes de fijar la Etapa C, para no dejar el widget o Health Connect en una version desalineada con el resto.

## Ejecucion real (2026-08-29): las etapas A y B se fusionaron por necesidad, no por eleccion

Al implementar Etapa A aislada (solo Gradle/AGP/SDK, Kotlin sin tocar) aparecieron 3 roturas encadenadas, todas causadas por AGP 9 y no por Compose:

1. **AGP 9 trae "Kotlin integrado" activado por defecto** y colisiona con el plugin clasico `org.jetbrains.kotlin.android` ("Cannot add extension with name 'kotlin'"). Fix real (no el flag de escape `android.builtInKotlin=false`, que se elimina en AGP 10): quitar el plugin `org.jetbrains.kotlin.android` de raiz y de `app/build.gradle.kts`, mover `kotlinOptions{}` a un bloque `kotlin { compilerOptions {} }` de nivel superior, y cambiar `java.setSrcDirs(...)` por `kotlin.srcDir(...)` dentro de `android.sourceSets` — exactamente el punto que esta spec ya señalaba como riesgo.
2. **Hilt 2.57.2 no arranca con AGP 9** ("Android BaseExtension not found") — busca una API que el nuevo DSL de AGP 9 ya no expone. Esto es un problema de AGP, no de Kotlin, asi que Hilt tuvo que subir (a 2.60.1) dentro de la Etapa A, no se pudo esperar a la B.
3. **KSP 2.1.0-1.0.28 registra sus directorios generados via la API clasica `kotlin.sourceSets`**, que el Kotlin integrado de AGP 9 prohibe por defecto. Esto es un limite conocido de KSP (`google/ksp#2729`, cerrado con workaround, aun sin fix nativo en la version usada). Subir KSP solo (a `2.1.20-2.0.1`) no lo resuelve — el workaround documentado por Google es `android.disallowKotlinSourceSets=false` en `gradle.properties`. **No confundir con `android.builtInKotlin=false`**: este flag no desactiva el Kotlin integrado, solo permite el registro de source sets que KSP sigue haciendo a la antigua.

Consecuencia: Kotlin (2.1.20), KSP (2.1.20-2.0.1), Hilt (2.60.1) y detekt (1.23.8, para no quedarse en una version sin ningun soporte oficial del Kotlin destino) subieron todos junto con AGP/Gradle/SDK, en un solo commit. La spec original asumia que Etapa A podia quedar aislada de Kotlin — no era cierto para este proyecto.

### R1 — Etapa A+B fusionadas: Gradle, AGP, SDK, Kotlin, KSP, Hilt, detekt

Todas las versiones verificadas contra maven-metadata.xml real, no contra paginas de release notes:

- Gradle wrapper a **9.7.1** (`gradle/wrapper/gradle-wrapper.properties`).
- AGP a **9.3.2** (`gradle/libs.versions.toml`).
- `compileSdk` a 37 (exigido por Compose 1.12.0), `targetSdk` a 36, `minSdk` a 26 (`app/build.gradle.kts`).
- Kotlin a **2.1.20** — bump conservador, no la ultima absoluta, por la limitacion de detekt explicada arriba.
- KSP a **2.1.20-2.0.1** (pareja exacta).
- Hilt a **2.60.1** (forzado por la rotura #2 de arriba).
- detekt a **1.23.8** (ultimo estable).
- Quitar `org.jetbrains.kotlin.android` de `build.gradle.kts` (raiz) y `app/build.gradle.kts`, y de `gradle/libs.versions.toml` (`jetbrains-kotlin-android`). Mantener `kotlin-compose` (`org.jetbrains.kotlin.plugin.compose`), que no colisiona.
- Mover `kotlinOptions{}` a `kotlin { compilerOptions {} }` de nivel superior en `app/build.gradle.kts`, usando el tipo `JvmTarget.JVM_17` en vez del string `"17"`.
- Cambiar `java.setSrcDirs(listOf(...))` por `kotlin.srcDir(...)` dentro de `android.sourceSets` (afecta tanto a `main` como a `androidTest`).
- Añadir `android.disallowKotlinSourceSets=false` en `gradle.properties`, documentado con el link al issue de KSP y una nota explicita de que NO es lo mismo que `android.builtInKotlin=false`.
- Sin tocar codigo de UI/produccion (`feature/`, `core/design/`, `domain/`) en esta etapa — todos los cambios son de build.
- `test` + `build` (completo, incluyendo `detekt`/`lint`, no solo `test`) en verde: **confirmado**.

### R2 — Etapa B: absorbida por R1 (ver seccion de ejecucion real arriba)

### R3 — Etapa C: Compose BOM

- Compose BOM a 2026.08.00 (material3 1.4.0, compose 1.12.0).
- Revisar Glance (widget) y Health Connect: actualizar a la version mas reciente compatible si existe, para no dejar esas dos piezas desalineadas del resto del stack.
- **No adoptar APIs de Material 3 Expressive en esta rama.** Existen marcadas `@ExperimentalMaterial3ExpressiveApi` en 1.4.0, pero su adopcion es decision de `refactor/design-system-round-3`, no de esta migracion.
- Revisar warnings de deprecacion que introduzca el nuevo BOM (M3 1.2.1→1.4.0 es un salto grande, es esperable que haya APIs renombradas o deprecadas) y resolverlos, no silenciarlos.
- `test` + `build` en verde.

## Fuera de alcance

- Adoptar Material 3 Expressive (`ButtonGroup`, `SplitButton`, `WavyProgressIndicator`, etc.) — para `refactor/design-system-round-3`, con opt-in consciente.
- Cualquier cambio de UI, componente o comportamiento — esta rama es infraestructura de build pura.
- Predictive back, shape morphing, `LoadingIndicator` — quedan disponibles tras esta migracion pero su adopcion es de otra rama.
- Exportar/importar datos, notas, widget — `fix/broken-features`, no aqui.

## Criterios de aceptacion

1. `test` y `build` en verde al final de cada una de las 3 etapas, no solo al final de la rama.
2. `minSdk` = 26, `compileSdk` = 37, `targetSdk` = 36.
3. La app instala y arranca sin cambios de comportamiento visible — esta rama no cambia UI.
4. La generacion de Hilt (`build` completo, no solo `test`) no muestra errores ni warnings nuevos de KSP.
5. La version dinamica de la app (leida de git en build time) sigue funcionando tras la Etapa A.
6. Ningun API de Material 3 Expressive queda usado en el codigo al cerrar esta rama.
7. Pasada manual del dueño en emulador/dispositivo — arrancar la app, navegar las 5 pestañas, confirmar que no hay regresiones visuales ni de comportamiento antes de mergear a `main`.
