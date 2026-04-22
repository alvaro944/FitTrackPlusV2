# Notas De Arquitectura Aprendidas

Estas notas explican patrones que estamos usando en la app y por que ayudan.

## Capas

La app mantiene una arquitectura sencilla:

- `feature`: pantallas Compose, estado de UI y ViewModels.
- `domain`: modelos y reglas importantes.
- `data`: repositorios, Room, DataStore y mapeos.
- `core`: infraestructura compartida.

La idea es evitar una Clean Architecture pesada al inicio, pero conservar separacion suficiente para que la app no se vuelva dificil de tocar.

## Compose

Compose debe recibir estado y enviar eventos.

Buen patron:

- `Screen` observa `uiState`.
- La UI llama a funciones del ViewModel.
- La pantalla no decide como guardar, cargar o transformar datos persistentes.

Evitar:

- consultas a Room desde Compose
- logica de negocio dentro de composables
- estados duplicados sin motivo

## ViewModel

El ViewModel es el punto de coordinacion de la feature.

Patron usado en Fase 1:

- `RoutinesUiState` agrupa lo que pinta la pantalla.
- `StateFlow` expone estado observable.
- funciones como `saveEditor`, `archiveRoutine` o `setActiveRoutine` representan eventos de usuario.
- repositorios quedan inyectados con Hilt.

## Repositorios

Los repositorios ocultan detalles de persistencia:

- Room queda detras de `RoutineRepository`.
- DataStore queda detras de `UserPreferencesRepository`.
- La UI no sabe si los datos vienen de tabla, preferencia o mapeo.

## Persistencia Local

Room es fuente de verdad para datos estructurados.

DataStore se usa para preferencias pequenas:

- rutina activa
- unidad de peso
- ajustes simples

## Snapshots Historicos

Regla importante del dominio:

Editar una rutina no debe modificar entrenamientos antiguos.

Por eso el historico guarda snapshots de nombres, ejercicios, reps objetivo y otros datos relevantes en el momento del entrenamiento.
