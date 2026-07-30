# Plan: Rango de repeticiones estructurado (DB v5)

> **Para Codex:** Lee la spec entera antes de empezar. **Crea rama nueva desde `develop`** (no desde `main`). Commits separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica local (`test` + `build`) antes de cada commit; NO lances el emulador (eso lo hace el dueño). No commits de WIP.

**Rama:** `feature/structured-target-reps` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-07-27-structured-target-reps.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

**Aviso critico:** hay una base de datos en uso diario. La migracion es aditiva y no debe recrear tablas ni tocar `targetRepsText`. Ante cualquier ambiguedad de parseo, dejar `NULL`.

---

## Tarea 0: Preparar rama

- [ ] Confirmar que partes de `develop` actualizada (`git checkout develop && git pull` si aplica)
- [ ] `git checkout -b feature/structured-target-reps`

---

## Tarea 1: Parser compartido en dominio

**Archivos:**
- Nuevo: `domain/model/TargetRepsRange.kt`
- `domain/usecase/GetProgressionHintUseCase.kt`
- Test nuevo: `app/src/test/.../domain/model/TargetRepsRangeTest.kt`

- [ ] Crear `TargetRepsRange(val min: Int, val max: Int)` con un `companion object` que exponga `parse(text: String?): TargetRepsRange?`
- [ ] Portar **exactamente** la logica de `GetProgressionHintUseCase.kt:41-65`, sin cambiar ni un umbral: entero en `1..99` → `min = max = n`; rango via regex `^(\d{1,2})\s*-\s*(\d{1,2})$` con ambos en `1..99` y `min <= max`; cualquier otra cosa → `null`
- [ ] Sustituir en `GetProgressionHintUseCase` el parseo inline por una llamada a `TargetRepsRange.parse(...)`, manteniendo el mismo comportamiento (si devuelve `null` → `ProgressionHint.NONE`)
- [ ] Tests del parser: `"10"`, `"8-12"`, `"8 - 12"`, `"12-8"` (null), `"0"` (null), `"100"` (null), `"AMRAP"` (null), `""` (null), `null` (null)
- [ ] Confirmar que los tests existentes de `GetProgressionHintUseCase` siguen pasando sin tocarlos
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract target reps range parsing to domain model`

---

## Tarea 2: Columnas estructuradas y migracion 4→5

**Archivos:**
- `data/local/entity/RoutineExerciseEntity.kt`
- `data/local/entity/RoutineExerciseAlternativeEntity.kt`
- `data/local/entity/WorkoutExerciseEntity.kt`
- `core/database/FitTrackPlusDatabase.kt`
- `di/DatabaseModule.kt`
- `app/schemas/` (generado)

- [ ] Añadir `targetRepsMin: Int? = null` y `targetRepsMax: Int? = null` a `RoutineExerciseEntity` y `RoutineExerciseAlternativeEntity`
- [ ] Añadir `targetRepsMinSnapshot: Int? = null` y `targetRepsMaxSnapshot: Int? = null` a `WorkoutExerciseEntity`, junto al `targetRepsSnapshot` existente
- [ ] Subir `version = 5` en `@Database` y añadir `MIGRATION_4_5` siguiendo el patron de `MIGRATION_3_4` (`FitTrackPlusDatabase.kt:35-41`)
- [ ] Registrar `MIGRATION_4_5` en `DatabaseModule.kt:26-30`. **No** añadir `fallbackToDestructiveMigration`
- [ ] Seis `ALTER TABLE ... ADD COLUMN ... INTEGER` (dos por tabla, nullable, sin default)
- [ ] Backfill por tabla, en tres pasadas. **La SQL de abajo es propuesta, no verificada — validala antes de darla por buena:**

```sql
-- 1) valor exacto: solo digitos, 1..99
UPDATE routine_exercises
SET targetRepsMin = CAST(trim(targetRepsText) AS INTEGER),
    targetRepsMax = CAST(trim(targetRepsText) AS INTEGER)
WHERE trim(targetRepsText) GLOB '[0-9]'
   OR trim(targetRepsText) GLOB '[0-9][0-9]';

-- 2) rango "N-M" (tolerando espacios)
UPDATE routine_exercises
SET targetRepsMin = CAST(trim(substr(targetRepsText, 1, instr(targetRepsText, '-') - 1)) AS INTEGER),
    targetRepsMax = CAST(trim(substr(targetRepsText, instr(targetRepsText, '-') + 1)) AS INTEGER)
WHERE targetRepsMin IS NULL
  AND instr(targetRepsText, '-') > 1
  AND replace(trim(targetRepsText), ' ', '') GLOB '[0-9]*-[0-9]*';

-- 3) limpieza: cualquier cosa fuera de rango o invertida vuelve a NULL
UPDATE routine_exercises
SET targetRepsMin = NULL, targetRepsMax = NULL
WHERE targetRepsMin IS NOT NULL
  AND (targetRepsMin < 1 OR targetRepsMax > 99 OR targetRepsMin > targetRepsMax);
```

- [ ] Repetir las tres pasadas para `routine_exercise_alternatives` (misma columna `targetRepsText`)
- [ ] Repetir para `workout_exercises`, parseando desde `targetRepsSnapshot` hacia `targetRepsMinSnapshot`/`targetRepsMaxSnapshot`
- [ ] Verificar que el resultado del backfill SQL coincide con lo que devolveria `TargetRepsRange.parse` para los mismos textos. Si diverge en algun caso, **manda el caso al dueño en vez de decidir por tu cuenta**
- [ ] Confirmar que se genera `app/schemas/com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase/5.json` y commitearlo
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add structured target reps columns with migration to db v5`

---

## Tarea 3: Propagar en dominio y repositorios

**Archivos:**
- `domain/model/RoutineModels.kt`
- `data/repository/DefaultRoutineRepository.kt`
- `data/repository/DefaultWorkoutRepository.kt`
- Tests en `app/src/test/.../domain/usecase/`

- [ ] Añadir `targetRepsMin: Int?` / `targetRepsMax: Int?` a `RoutineExerciseDraft`, `RoutineExerciseSnapshot`, `RoutineExerciseAlternativeDraft`, `RoutineExerciseAlternativeSnapshot` (con default `null` para no romper llamadores)
- [ ] En `DefaultRoutineRepository.createRoutine` / `replaceRoutine` (`DefaultRoutineRepository.kt:74-88`), rellenar los campos estructurados al insertar usando `TargetRepsRange.parse(draft.targetRepsText)`. El texto se guarda tal cual, sin normalizar
- [ ] Propagar los campos en los mappers de lectura de `getRoutineSnapshot`
- [ ] En `DefaultWorkoutRepository.createSessionFromRoutineDay` (`DefaultWorkoutRepository.kt:77-100`) copiar `targetRepsMinSnapshot`/`targetRepsMaxSnapshot` desde `activeVariant()` en el mismo insert que ya hace `targetRepsSnapshot` (linea 85)
- [ ] Revisar `activeVariant()` y `toActiveVariant()` (`DefaultWorkoutRepository.kt:213-234`) para que arrastren los campos nuevos
- [ ] Revisar `replaceWorkoutExerciseVariant` (`DefaultWorkoutRepository.kt:106-141`): al cambiar de variante dentro de una sesion debe actualizar tambien los campos estructurados
- [ ] Test: al crear sesion desde una rutina con `"8-12"`, el `WorkoutExerciseEntity` resultante tiene `targetRepsMinSnapshot = 8` y `targetRepsMaxSnapshot = 12`. Seguir el patron de fakes de `StartWorkoutSessionUseCaseTest.kt`
- [ ] Test: con `"AMRAP"`, los campos quedan `null` y `targetRepsSnapshot` conserva el texto
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: propagate structured target reps through routines and session snapshot`

---

## Verificacion final antes de avisar al dueño

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] `app/schemas/.../5.json` presente y commiteado
- [ ] `git diff develop --stat` revisado: no debe haber cambios en `feature/routines/` ni en ninguna pantalla Compose. Si los hay, algo se ha salido del alcance
- [ ] Push de la rama y avisar

**Pendiente de pasada manual (la hace el dueño, no Codex):** instalar sobre una version previa con datos reales, confirmar que rutinas e historial siguen intactos, y que la pista de progresion se comporta igual que antes. Esta fase no debe cambiar nada visible.

**Merge:** a `develop` cuando compile y pasen los tests. A `main` solo tras la pasada manual del dueño.
