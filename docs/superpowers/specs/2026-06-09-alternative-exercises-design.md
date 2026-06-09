# Alternative Exercises Design

**Goal:** Permitir variantes puntuales dentro de un ejercicio de rutina sin ensuciar la UI diaria y sin mezclar progresiones distintas en estadísticas.

**Scope**

- Mantener cada ejercicio visible como hoy, sin variantes expuestas por defecto.
- Añadir una utilidad discreta para consultar, crear y elegir alternativas de un ejercicio.
- Guardar alternativas dentro del ejercicio base de la rutina.
- Permitir usar una alternativa solo para la sesión actual.
- Permitir marcar una alternativa como nueva predeterminada desde la edición de rutina.
- Mantener estadísticas separadas por variante usada realmente en cada sesión.

**Out of scope**

- Mezclar variantes en una sola progresión estadística.
- Mostrar todas las variantes inline de forma permanente.
- Reestructurar por completo la arquitectura de rutinas.
- Cambiar automáticamente la rutina base al elegir una alternativa durante un entreno.
- Reconciliar sets ya empezados al cambiar de variante a mitad de un ejercicio con datos registrados.

**Product rules**

- La rutina sigue teniendo un único ejercicio principal por posición.
- Las alternativas viven asociadas a ese ejercicio principal.
- Elegir una alternativa durante un entreno afecta solo a esa sesión.
- Crear una alternativa nueva la deja disponible para sesiones futuras.
- Marcar una alternativa como predeterminada se hace solo desde editar rutina.
- La progresión de cada variante se calcula por la variante realmente ejecutada, no por la familia completa.

**UX**

**Entreno**

- Cada tarjeta de ejercicio mostrará un icono pequeño de intercambio/alternancia.
- Ese icono abre una `bottom sheet` compacta.
- La sheet muestra:
  - variante predeterminada actual
  - alternativas existentes
  - acción `Crear alternativa`
- Si no existen alternativas, la lista aparece vacía con una CTA clara para crear la primera.
- Seleccionar una alternativa sustituye el ejercicio de hoy en esa sesión.
- Para esta primera versión, el cambio de variante en el entreno se considera una acción previa a registrar series. Si el ejercicio ya tiene datos cargados, el cambio quedará bloqueado con feedback corto.

**Crear alternativa**

- La alternativa nueva nace precargada con:
  - nombre
  - series objetivo
  - reps objetivo
  - notas
- Todo queda editable antes de guardar.
- El caso de uso principal será cambiar el nombre y, si hace falta, ajustar series/reps/notas.

**Editar rutina**

- El editor de rutina tendrá acceso discreto a la misma utilidad de alternativas dentro de cada ejercicio.
- Desde ahí se podrá:
  - ver alternativas existentes
  - crear una nueva
  - marcar una como `Predeterminada`
- Marcar como predeterminada no cambia históricos previos; solo cambia la versión propuesta en futuras sesiones.

**Data model**

- El ejercicio base actual de la rutina seguirá representando la posición principal en el día.
- Las alternativas se almacenarán en una estructura separada ligada al ejercicio base.
- La sesión de entreno guardará la variante concreta utilizada mediante snapshot de nombre/reps y referencia a la alternativa elegida cuando exista.
- Las estadísticas y referencias de “último peso” deberán resolverse contra la variante real usada, no contra la familia completa.

**Architecture**

La solución más simple para esta fase es extender el modelo actual con una tabla específica de alternativas en vez de rehacer la jerarquía completa de ejercicios de rutina. El ejercicio base sigue siendo la referencia principal del slot y las alternativas actúan como extensiones opcionales.

En el entreno, la sustitución de variante se reflejará en la sesión activa mediante actualización del ejercicio snapshot y de sus metadatos asociados. En edición, la promoción de una alternativa a predeterminada modificará qué datos base usa la rutina para futuras sesiones sin tocar el histórico de sesiones ya cerradas.

**Files likely touched**

- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/database/FitTrackPlusDatabase.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/entity/RoutineExerciseEntity.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/entity/WorkoutExerciseEntity.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/entity/RoutineExerciseAlternativeEntity.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/dao/RoutineDao.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/local/dao/WorkoutDao.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/repository/DefaultRoutineRepository.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/data/repository/DefaultWorkoutRepository.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/domain/model/RoutineModels.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/routines/RoutinesViewModel.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/routines/RoutinesScreen.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/workout/WorkoutViewModel.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/workout/WorkoutScreen.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/domain/usecase/ObserveWorkoutStatsUseCase.kt`

**Verification**

- `./gradlew test`
- `./gradlew build`
- pasada manual en rutina y entreno:
  - crear alternativa
  - elegir alternativa para hoy
  - bloquear cambio cuando ya haya datos cargados
  - marcar alternativa como predeterminada
  - comprobar que stats no mezclan variantes
