# Plan: RIR por ejercicio y metrica de calidad de esfuerzo

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `develop`** (no desde `main`). Commits separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador (eso lo hace el dueño). No commits de WIP.

**Rama:** `feature/effort-quality-rir` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-07-30-effort-quality-rir.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**PREREQUISITO:** `feature/estimated-1rm-confidence` mergeada a `develop`. Si no lo esta, **para y avisa**.

**Aviso critico:** hay una BD en uso diario. La migracion 5→6 es **aditiva y sin backfill**: `ALTER TABLE ADD COLUMN`, sin recrear tablas. Todo el historial queda con `rir = NULL` y eso es lo correcto.

**Regla de producto que gobierna toda la tarea:** la app **no premia el fallo**. El RIR 0 no recibe color de exito, ni medalla, ni felicitacion, en ningun punto de la UI. Si dudas en algun detalle visual, elige la opcion neutra.

---

## Tarea 0: Preparar rama

- [ ] Verificar el prerequisito
- [ ] `git checkout develop && git pull` si aplica
- [ ] `git checkout -b feature/effort-quality-rir`

---

## Tarea 1: Columna `rir` y migracion a DB v6

**Archivos:**
- `data/local/entity/WorkoutExerciseEntity.kt`
- `core/database/FitTrackPlusDatabase.kt`
- `di/DatabaseModule.kt`
- `app/schemas/` (generado)

- [ ] Añadir `rir: Int? = null` a `WorkoutExerciseEntity`
- [ ] Subir `version = 6` en `@Database` y añadir `MIGRATION_5_6` siguiendo el patron de las existentes. **Declararla en orden coherente con las demas en el fichero** (la 4→5 quedo colocada fuera de orden; no repitas eso)
- [ ] Un unico `ALTER TABLE workout_exercises ADD COLUMN rir INTEGER`. **Sin backfill**: no hay dato historico que recuperar
- [ ] Registrar `MIGRATION_5_6` en `DatabaseModule.kt`. **No** añadir `fallbackToDestructiveMigration`
- [ ] Confirmar que se genera `app/schemas/.../6.json` y commitearlo
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add per-exercise rir column with migration to db v6`

---

## Tarea 2: Dominio y repositorio

**Archivos:**
- `data/local/dao/WorkoutDao.kt`
- `data/repository/WorkoutRepository.kt` y `DefaultWorkoutRepository.kt`
- `domain/model/` (modelo de zonas)
- Tests en `app/src/test/.../domain/`

- [ ] Modelo de dominio para las zonas, p.ej. `enum class EffortZone { FAILURE, EFFECTIVE, SUBMAXIMAL }` con un mapeo desde `Int?`: `0 → FAILURE`, `1..3 → EFFECTIVE`, `>= 4 → SUBMAXIMAL`, `null →` sin zona
- [ ] Validacion de rango `0..10` en el punto de escritura. Fuera de rango se rechaza, no se recorta silenciosamente
- [ ] Metodo en el DAO y en el repositorio para actualizar el RIR de un ejercicio de sesion. Seguir el patron de `updateSetCompletion` (suspend, sin devolver el objeto entero)
- [ ] Permitir volver a `null` (borrar el valor), no solo cambiarlo
- [ ] Tests del mapeo de zonas incluyendo los bordes: 0, 1, 3, 4, 10, null
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add rir effort zones to domain and workout repository`

---

## Tarea 3: Entrada de RIR en la pantalla de entrenamiento

**Archivos:**
- `feature/workout/WorkoutViewModel.kt`
- `feature/workout/WorkoutScreen.kt`

- [ ] Exponer el `rir` del ejercicio en el UI state y una accion para fijarlo/borrarlo
- [ ] Fila de opciones de un toque: `0, 1, 2, 3, 4+`. **Reutilizar el componente de seleccion que ya exista en `core/design/`**; no crear uno nuevo si hay equivalente
- [ ] Volver a tocar la opcion ya seleccionada la deselecciona (vuelve a "no reportado")
- [ ] Texto corto que explique que se pregunta: cuantas repeticiones mas se podrian haber hecho. Español sin tildes, coherente con el resto de la app
- [ ] Señalar 1-3 como zona util **sin** marcar las otras como error. Informar, no juzgar
- [ ] El `0` se etiqueta como fallo con tratamiento **neutro**: mismo peso visual que el resto, sin color de exito ni de alarma
- [ ] **Verificar explicitamente que se puede terminar un ejercicio y una sesion completa sin tocar el RIR.** Ninguna validacion nueva puede bloquear el cierre
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: capture per-exercise rir during workout`

---

## Tarea 4: Metrica de calidad de esfuerzo en stats

**Archivos:**
- `domain/model/StatsModels.kt`
- `domain/usecase/ObserveWorkoutStatsUseCase.kt`
- `feature/stats/StatsViewModel.kt`
- `feature/stats/StatsScreen.kt`
- Tests en `app/src/test/.../domain/usecase/`

- [ ] Nuevo modelo de dominio con: total de ejercicios con RIR reportado, conteo por zona, y porcentaje de zona util
- [ ] Calcularlo en `ObserveWorkoutStatsUseCase` sobre las sesiones ya filtradas por periodo. **Reutilizar el filtrado existente** (`filterByPeriod`, `ObserveWorkoutStatsUseCase.kt:59-69`); no duplicar esa logica
- [ ] **Denominador: solo ejercicios con RIR reportado.** Los `null` no cuentan ni a favor ni en contra
- [ ] Si el total reportado es 0, el modelo debe permitir a la UI **no mostrar nada**. No devolver `0%`
- [ ] En la UI: porcentaje de zona util, reparto de las tres zonas, y **sobre cuantos ejercicios** se calcula
- [ ] Ninguna de las tres zonas se pinta como buena o mala. Describir el reparto, no puntuarlo
- [ ] Reutilizar la tarjeta/estado vacio que ya exista en `StatsScreen.kt`; no crear un patron visual nuevo
- [ ] Tests: 10 ejercicios reportados con 6 en zona util → 60% sobre 10; ejercicios con `null` mezclados no alteran el porcentaje; 0 reportados → metrica ausente
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add effort quality metric to stats`

---

## Verificacion final antes de avisar al dueño

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] `app/schemas/.../6.json` presente y commiteado
- [ ] Repaso manual del diff buscando cualquier sitio donde el RIR 0 reciba tratamiento de logro. **Si lo hay, quitalo**
- [ ] Confirmar que ninguna validacion nueva puede impedir terminar una sesion
- [ ] Push y avisar

**Pendiente de pasada manual (la hace el dueño):**
1. Instalar sobre la version previa con datos reales: rutinas e historial intactos, ejercicios antiguos con RIR vacio.
2. Entrenar sin tocar el RIR: todo debe funcionar como siempre.
3. Entrenar rellenando el RIR y comprobar la metrica en stats.

**Merge:** a `develop` cuando compile y pasen los tests. A `main` solo tras la pasada manual del dueño.
