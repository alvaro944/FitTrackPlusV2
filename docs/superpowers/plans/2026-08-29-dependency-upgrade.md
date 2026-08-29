# Plan: Actualizacion de dependencias (Gradle, AGP, SDK, Kotlin, Compose)

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `develop`** (verifica primero que `fix/data-loss` ya este mergeada ahi; si no lo esta, avisa y decide con el dueño si esperar o partir igualmente — esta rama no depende tecnicamente de esa, pero mezclar despues sera mas facil si ya esta). Commits separados por **etapa**, conventional commits, **sin Co-Authored-By**. `test` + `build` en verde al final de CADA etapa, no solo al final de la rama. No lances el emulador. No commits de WIP.

**Rama:** `chore/dependency-upgrade` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-08-29-dependency-upgrade.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**Esto es infraestructura de build, no UI.** Ningun cambio de comportamiento visible es esperado. Si en cualquier etapa algo de UI se rompe, es una señal de que algo en esa etapa no esta bien resuelto — no lo "arregles" tocando la pantalla afectada, para y averigua la causa real en la config de build.

**Regla de oro:** cada etapa es su propio commit (o mas de uno si hace falta, pero nunca mezclar etapas en un commit), y cada etapa debe dejar `test` + `build` en verde antes de pasar a la siguiente. Si una etapa no compila, no avances a la siguiente para "ver si se arregla solo" — resuelve ahi.

---

## Tarea 0: Preparar rama

- [ ] Verificar si `fix/data-loss` ya esta mergeada en `develop`. Si no lo esta, avisar y preguntar si se espera o se parte igual
- [ ] `git checkout develop && git pull` si aplica
- [ ] `git checkout -b chore/dependency-upgrade`

---

## Ejecutado (2026-08-29) — Etapas A y B se fusionaron

Al aislar Etapa A aparecieron 3 roturas causadas por AGP 9 (no por Kotlin), documentadas en
la spec: colision del "Kotlin integrado" de AGP 9 con el plugin `org.jetbrains.kotlin.android`,
Hilt 2.57.2 incompatible con el nuevo DSL de AGP, y un limite conocido de KSP
(`google/ksp#2729`) con el registro de source sets. Las tres forzaron subir Kotlin/KSP/Hilt/detekt
junto con AGP, en un commit unico.

### Commit 1 — `4fc7c08`: `chore: upgrade gradle, agp, sdk, kotlin, ksp, hilt, and detekt`

- [x] Gradle wrapper a **9.7.1**
- [x] AGP a **9.3.2**
- [x] `compileSdk` = 37, `targetSdk` = 36
- [x] `minSdk` = 26
- [x] Kotlin **2.1.20**, KSP **2.1.20-2.0.1**, Hilt **2.60.1**, detekt **1.23.8**
- [x] `org.jetbrains.kotlin.android` eliminado de `build.gradle.kts` (raiz), `app/build.gradle.kts` y `gradle/libs.versions.toml`
- [x] `kotlinOptions{}` movido a `kotlin { compilerOptions {} }` de nivel superior (`JvmTarget.JVM_17`)
- [x] `sourceSets { getByName("main") { java.setSrcDirs(...) } }` → `kotlin.srcDir(...)` dentro de `android.sourceSets` (main y androidTest)
- [x] `android.disallowKotlinSourceSets=false` en `gradle.properties`, documentado (workaround de KSP, no de `android.builtInKotlin`)
- [x] `providers.exec` de la version dinamica de git sigue funcionando — verificado en `BuildConfig` generado
- [x] `test` + `build` completo (incluye `detekt`, `lint`, `assembleDebug`, `assembleRelease`) en verde

### Commit 2 — `72fb904`: `chore: upgrade compose bom to 2026.08.00`

- [x] Compose BOM a **2026.08.00** (material3 1.4.0, compose 1.12.0)
- [x] Glance y Health Connect: **sin cambio** — no existe release estable mas reciente que la actual (solo alphas)
- [x] Unico warning de codigo propio (`Dropdown.kt`, `menuAnchor()` deprecado) resuelto con `ExposedDropdownMenuAnchorType.PrimaryNotEditable`
- [x] Warning externo de `ReportingExtension.file()` (dentro del plugin de detekt, no de nuestro codigo) documentado en la spec, no silenciado
- [x] Confirmado por grep: cero uso de APIs de Material 3 Expressive
- [x] `test` + `build` en verde

---

## Verificacion final

- [x] `sh gradlew test` en verde
- [x] `sh gradlew build` en verde (con `detekt`, `lint`, `assembleDebug`, `assembleRelease`)
- [x] `minSdk` = 26, `compileSdk` = 37, `targetSdk` = 36 confirmados en `app/build.gradle.kts`
- [x] Cero uso de APIs de Material 3 Expressive (confirmado por grep)
- [x] Version dinamica de la app confirmada en el `BuildConfig` generado (`GIT_BRANCH`/`GIT_SHA` correctos)
- [ ] Push — pendiente de autorizacion explicita del dueño
- [ ] Pasada manual del dueño en dispositivo/emulador

**Pendiente de pasada manual (la hace el dueño):**
1. Instalar la app y arrancarla — sin crashes de arranque.
2. Recorrer las 5 pestañas comprobando que no hay regresiones visuales.
3. Comprobar que la version de la app (visible en Ajustes o donde se muestre) sigue reflejando la rama/commit correctos.
4. Si el dispositivo de prueba tiene Android por debajo de API 26 (minSdk nuevo), confirmar que se ha decidido conscientemente dejarlo fuera.

**Merge:** a `develop` cuando compile y pasen los tests en las 3 etapas. A `main` solo tras la pasada manual del dueño. Recomendado mergear antes de empezar `fix/broken-features`, para que esa rama se construya ya sobre el stack actualizado.
