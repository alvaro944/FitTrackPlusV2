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

Patron usado en Fase 2:

- `WorkoutViewModel` combina preferencia de rutina activa, preview del siguiente dia y sesion abierta.
- Una sesion abierta se trata como estado principal y se reanuda antes de permitir crear otra.
- Los inputs de series viven como texto en UI, pero se normalizan en un caso de uso antes de persistir.

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

En el registro de entrenamiento, Room guarda la sesion inmediatamente al iniciarla y cada serie se actualiza durante la edicion. Finalizar solo marca `finishedAt`, por lo que una sesion abierta puede reanudarse.

## Snapshots Historicos

Regla importante del dominio:

Editar una rutina no debe modificar entrenamientos antiguos.

Por eso el historico guarda snapshots de nombres, ejercicios, reps objetivo y otros datos relevantes en el momento del entrenamiento.

En Fase 2 la sesion se crea desde `RoutineSnapshot`: se copian nombre de rutina, nombre de dia, nombre de ejercicio y objetivo de reps antes de registrar pesos/reps reales.

En Fase 3 el historial lee esos snapshots directamente:

- el listado usa solo sesiones finalizadas
- el detalle se carga desde la relacion historica de sesion, ejercicios y series
- el mapeo ordena ejercicios por `position` y series por `setNumber`
- no se consulta la rutina editable para mostrar entrenamientos antiguos

## Datos Demo De Desarrollo

Los datos demo sirven para probar flujos sin meter dependencias externas.

Regla usada en Fase 3:

- sembrar solo si la app es debuggable
- sembrar solo si la base esta vacia
- crear snapshots historicos completos
- dejar release sin seed automatico

## Estadisticas Locales

Las estadisticas de Fase 4 se calculan en memoria desde el historico, sin nuevas tablas.

Reglas usadas:

- observar solo sesiones finalizadas con ejercicios y series
- mantener Room como fuente de verdad
- calcular agregados en un caso de uso de dominio
- agrupar progreso por nombre snapshot normalizado cuando los IDs editables pueden cambiar
- dejar Compose como capa de lectura del `UiState`, sin formulas de negocio

Para marcas simples:

- volumen = `peso * reps`
- 1RM estimado = `peso * (1 + reps / 30)`
- marcas con peso ignoran sets con peso o reps en cero
- marca de reps puede usar peso cero para ejercicios de peso corporal registrados asi
