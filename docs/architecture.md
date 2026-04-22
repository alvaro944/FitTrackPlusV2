# Architecture

FitTrackPlus v2 usa Modern Android Architecture de forma practica, evitando una Clean Architecture pesada para el tamano actual del proyecto.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- DataStore
- Hilt
- KSP

## Estructura

```text
com.alvarocervantes.fittrackplus
+-- core
|   +-- database
|   +-- datastore
|   +-- design
|   +-- navigation
|   +-- util
+-- data
|   +-- local
|   +-- preferences
|   +-- repository
+-- domain
|   +-- model
|   +-- usecase
+-- feature
    +-- home
    +-- routines
    +-- workout
    +-- history
    +-- stats
    +-- settings
```

## Reglas

- `feature`: pantallas, estado de UI y ViewModels por flujo.
- `domain`: modelos y reglas importantes del negocio.
- `data`: repositorios, entidades Room, DAOs y preferencias.
- `core`: infraestructura compartida.

## Persistencia

Room es la fuente de verdad local. Las rutinas son editables, pero el historial debe ser inmutable a nivel practico.

Rutinas:

- `RoutineEntity`
- `RoutineDayEntity`
- `RoutineExerciseEntity`

Historico:

- `WorkoutSessionEntity`
- `WorkoutExerciseEntity`
- `WorkoutSetEntity`

La regla principal es que una sesion guarda snapshots:

- nombre de rutina
- nombre de dia
- nombre de ejercicio
- objetivo de reps

Asi, editar una rutina nunca reescribe el pasado.

## Preferencias

DataStore guarda datos pequenos:

- rutina activa
- unidad de peso
- ajustes simples

## Firebase

Firebase queda fuera del MVP inicial. Cuando se retome, debe disenar una estrategia offline-first antes de escribir codigo de sync.
