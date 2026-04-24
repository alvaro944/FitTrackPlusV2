# ADR 0001 — Historial inmutable mediante snapshots

**Estado:** Aceptado  
**Fecha:** 2026-04-24  
**Autores:** Equipo FitTrackPlus

---

## Contexto

Al registrar un entrenamiento, la sesion queda vinculada a una rutina y un dia concretos.
Las rutinas son editables: el usuario puede renombrar ejercicios, cambiar series o eliminar dias
en cualquier momento despues de haber entrenado.

El problema: si el historial hiciera JOIN contra la rutina activa, cualquier edicion posterior
alteraria retroactivamente todos los entrenamientos anteriores. Un usuario que renombre
"Press Banca" a "Bench Press" veria su historial y sus estadisticas cambiados de golpe,
sin que hubiera entrenado de nuevo.

## Decision

Al iniciar cada sesion de entrenamiento, el sistema guarda **snapshots** de:

- Nombre de la rutina (`routineNameSnapshot`)
- Nombre del dia (`dayNameSnapshot`)
- Nombre de cada ejercicio (`exerciseNameSnapshot`)
- Repeticiones objetivo de cada ejercicio (`targetRepsSnapshot`)

El historial y las estadisticas leen **exclusivamente** de estos snapshots.
Nunca hacen JOIN contra las tablas de rutinas activas.

```
workout_sessions
  ├── routineNameSnapshot   ← copia en el momento del inicio
  └── dayNameSnapshot

workout_exercises
  ├── exerciseNameSnapshot  ← copia en el momento del inicio
  └── targetRepsSnapshot

workout_sets
  ├── weightKg
  └── reps
```

## Consecuencias

**Positivo:**

- El historial es completamente inmutable: editar o eliminar una rutina no altera ningun
  entrenamiento pasado.
- Las estadisticas son estables y auditables.
- La normalizacion por nombre (`exerciseKey = name.trim().lowercase()`) permite agrupar
  variantes del mismo ejercicio (espacios extra, mayusculas) sin necesidad de un catalogo global.

**Negativo:**

- Duplicacion de datos: cada sesion almacena cadenas de texto que ya existen en la rutina.
  El incremento en tamano de la base de datos es insignificante para el volumen tipico de un
  usuario de gimnasio (cientos de sesiones, no millones).
- No hay forma de "corregir" retroactivamente el nombre de un ejercicio en el historial.
  Se acepta: la inmutabilidad es la garantia mas valiosa.

## Alternativas descartadas

| Alternativa | Motivo del descarte |
|-------------|---------------------|
| JOIN contra rutina activa | Historial mutable: editar rompe el pasado |
| ID de ejercicio global + catalogo | Complejidad innecesaria en MVP; los strings libres son suficientes |
| Evento de cambio de nombre con migracion | Complejo, propenso a errores, fuera de alcance del MVP |

## Referencias

- `data/local/entity/WorkoutSessionEntity.kt`
- `data/local/entity/WorkoutExerciseEntity.kt`
- `domain/usecase/ObserveWorkoutStatsUseCase.kt`
