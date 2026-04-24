# ADR 0002 — Ejercicios como strings libres sin catalogo global

**Estado:** Aceptado  
**Fecha:** 2026-04-24  
**Autores:** Equipo FitTrackPlus

---

## Contexto

Las apps de entrenamiento suelen mantener un catalogo de ejercicios predefinidos
(nombre, grupo muscular, instrucciones, imagen) vinculado mediante IDs a las rutinas
y al historial. Este catalogo facilita la busqueda por musculo, el autocompletado y
la deduplicacion.

FitTrackPlus v2 arranca como MVP local-first. El objetivo inicial es que el usuario
pueda crear rutinas, entrenar y ver su progreso con la menor friccion posible.

## Decision

Los ejercicios se representan como **texto libre** (`String`) tanto en las rutinas
como en los snapshots del historial. No existe una entidad `ExerciseCatalog` ni una
tabla de referencia global.

```kotlin
// Rutina
data class RoutineExerciseEntity(
    val exerciseNameSnapshot: String,   // "Press Banca", "Bench Press", etc.
    ...
)

// Historial
data class WorkoutExerciseEntity(
    val exerciseNameSnapshot: String,   // copia del nombre al iniciar sesion
    ...
)
```

La agrupacion en estadisticas se hace por **nombre normalizado**:

```kotlin
val exerciseKey = name.trim().lowercase()
```

Esto permite que " Bench Press " y "bench press" se traten como el mismo ejercicio
sin necesidad de un ID compartido.

## Consecuencias

**Positivo:**

- Cero friccion al crear una rutina: el usuario escribe el nombre que quiera.
- Sin pantallas de seleccion de ejercicio ni base de datos de catalogo que mantener.
- El MVP se entrega antes y con menos superficie de bugs.
- La normalizacion por string cubre el 95 % de los casos de uso sin infraestructura adicional.

**Negativo:**

- No hay busqueda por musculo trabajado.
- Si el usuario escribe "Press Banca" en una rutina y "Bench Press" en otra,
  las estadisticas los mostraran por separado (diferente `exerciseKey`).
- Autocompletado no disponible en esta version.

## Limitaciones conocidas y decision de diferimiento

La busqueda por musculo y el autocompletado son funcionalidades deseables a futuro.
Se difieren porque:

1. Requieren un catalogo curado (>200 ejercicios) o integracion con una API externa.
2. La pantalla de seleccion de ejercicio anade friccion que no se quiere en el MVP.
3. El patron de normalizacion por string es suficiente para el uso real de un usuario
   que entrena de forma consistente con nombres similares.

Cuando se implemente un catalogo, la migracion seria: añadir `exerciseTemplateId` como
FK opcional en `RoutineExerciseEntity` y en `WorkoutExerciseEntity`, manteniendo el
campo `exerciseNameSnapshot` para compatibilidad con el historial existente.

## Alternativas descartadas

| Alternativa | Motivo del descarte |
|-------------|---------------------|
| Catalogo predefinido en assets | Requiere curado, traduccion y mantenimiento |
| Catalogo editable por el usuario | Pantalla extra de gestion; complejidad innecesaria en MVP |
| API externa de ejercicios | Dependencia de red; rompe el modelo local-first |

## Referencias

- `data/local/entity/RoutineExerciseEntity.kt`
- `data/local/entity/WorkoutExerciseEntity.kt`
- `domain/usecase/ObserveWorkoutStatsUseCase.kt` (normalizacion `exerciseKey`)
- `docs/future-improvements.md` (busqueda por musculo como mejora futura)
