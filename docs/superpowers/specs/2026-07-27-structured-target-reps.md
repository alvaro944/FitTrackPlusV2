# Spec: Rango de repeticiones estructurado (cimiento de periodizacion)

**Fecha:** 2026-07-27
**Fase:** 1 del arco de periodizacion (ver `docs/design/mejoras-claude.md`, entrada 19)
**Plan de ejecucion:** `docs/superpowers/plans/2026-07-27-structured-target-reps.md`

---

## Por que

El usuario quiere que la app dirija el entrenamiento: mesociclos, descargas, cambios de rango entre bloques, y objetivos del tipo "quiero 100 kg en banca, hoy hago 80x5". Eso son tres capas: estructura (rutina, ya existe), prescripcion (rangos e intensidad por bloque) y objetivo (motor de proyeccion).

Las capas 2 y 3 son imposibles hoy por un motivo concreto: **el rango de repeticiones se guarda como texto libre**.

`RoutineExerciseEntity.targetRepsText: String` (`RoutineExerciseEntity.kt:30`) y el mismo campo en `RoutineExerciseAlternativeEntity`. Se parsea en caliente, con un regex, cada vez que hace falta (`GetProgressionHintUseCase.kt:41-65`). Sobre un `String` no se puede calcular una prescripcion, ni comparar rangos entre bloques, ni proyectar contra un objetivo.

Esta fase convierte ese dato en ciudadano de primera clase. No añade ninguna funcionalidad visible: es cimiento.

Cruza con la entrada 9 del backlog (`Migrar targetRepsText a targetRepsMin/Max`), que ya estaba anotada como condicional. La condicion se ha cumplido.

## Estado actual verificado

- `targetRepsText: String` en `RoutineExerciseEntity` y `RoutineExerciseAlternativeEntity`. Texto libre: `"10"`, `"8-12"`, y potencialmente cualquier cosa (`"AMRAP"`).
- El parseo vive inline en `GetProgressionHintUseCase.kt:41-65`: acepta entero en `1..99`, o rango via regex `^(\d{1,2})\s*-\s*(\d{1,2})$` validando `min <= max`. Todo lo demas → `ProgressionHint.NONE`.
- El snapshot se materializa en un unico punto: `DefaultWorkoutRepository.createSessionFromRoutineDay` (`DefaultWorkoutRepository.kt:59-104`), que copia `activeVariant().targetRepsText` a `WorkoutExerciseEntity.targetRepsSnapshot` (linea 85) e inserta `targetSets` filas vacias de `WorkoutSetEntity`.
- DB en version 4. Migraciones 1→2, 2→3, 3→4 en `core/database/FitTrackPlusDatabase.kt`, registradas en `di/DatabaseModule.kt:26-30`. Sin `fallbackToDestructiveMigration`. Esquemas exportados en `app/schemas/`.

## Requisitos

### R1 — El rango estructurado se persiste junto al texto, no en su lugar

Se **añaden** `targetRepsMin: Int?` y `targetRepsMax: Int?`. `targetRepsText` **se conserva** como fuente de verdad de la UI y como respaldo de lo no parseable.

`NULL` en los campos estructurados tiene significado explicito: "este texto no representa un rango estructurado, usa el texto". No es un error ni un dato pendiente.

Un valor exacto (`"10"`) se guarda como `min = max = 10`.

### R2 — La migracion no destruye ni inventa datos

Hay una base de datos en uso diario. La migracion 4→5 debe:

- Añadir columnas nullable (`ALTER TABLE ADD COLUMN`), sin recrear tablas.
- Rellenar por parseo best-effort lo que sea inequivocamente parseable.
- Dejar `NULL` cualquier texto ambiguo. Ante la duda, `NULL`.
- No modificar ni una fila de `targetRepsText`.

### R3 — El parseo deja de estar duplicado

La logica de parseo pasa a un modelo de dominio reutilizable. `GetProgressionHintUseCase` lo consume en vez de tener el regex inline.

El comportamiento observable de la pista de progresion **no cambia en ningun caso**. Mismos umbrales, mismas entradas, mismas salidas.

### R4 — El snapshot congela tambien el rango estructurado

`WorkoutExerciseEntity` gana `targetRepsMinSnapshot: Int?` / `targetRepsMaxSnapshot: Int?`, rellenados en el mismo punto y momento que `targetRepsSnapshot`.

**La invariante de snapshot no cambia**: se sigue congelando al iniciar sesion, se sigue leyendo del snapshot en historial, y editar la rutina despues sigue sin afectar a lo ya registrado. Solo se congela un campo mas.

### R5 — La UI no cambia

El editor de rutinas sigue editando texto. El usuario no percibe nada distinto. El repositorio parsea al guardar y rellena los campos estructurados.

Esto es deliberado: mantener el texto como fuente de verdad de la UI evita tocar `RoutineEditorUiState` y toda la pantalla de edicion en esta fase. La UI estructurada, si llega, sera parte de la fase de bloques.

## Fuera de alcance

- Cualquier UI nueva.
- Mesociclos, bloques, prescripcion, objetivos.
- Tocar el calculo de e1RM (ya existe y funciona: Epley en `ObserveWorkoutStatsUseCase.kt:240-246`).
- Unificar las dos identidades de ejercicio (`scopeKey` en stats vs `performedVariantKey` en la pista de progresion). Queda anotado para la fase 2.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. `app/schemas/.../5.json` generado y commiteado.
3. Actualizar de una instalacion con datos reales no pierde rutinas ni historial.
4. Una rutina con `"8-12"` queda con `min=8, max=12`; con `"10"` queda `min=max=10`; con `"AMRAP"` queda `NULL, NULL` y el texto intacto.
5. La pista de progresion (UP/DOWN/NONE) se comporta exactamente igual que antes del cambio.
