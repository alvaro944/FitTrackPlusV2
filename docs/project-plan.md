# FitTrackPlus v2 Project Plan

Este documento es el plan maestro de FitTrackPlus v2. El objetivo es que el contexto importante no dependa de chats sueltos.

## Vision

FitTrackPlus v2 sera una app Android nativa para:

- crear rutinas de gimnasio por dias
- registrar entrenamientos reales desde una rutina activa
- conservar un historial consistente aunque la rutina cambie
- analizar progreso mas adelante

La prioridad es construir una app movil seria, entendible y mantenible mientras se aprende el proceso.

## Decisiones Base

- Plataforma: Android nativo.
- Lenguaje: Kotlin.
- UI: Jetpack Compose + Material 3.
- Arquitectura: Modern Android Architecture sencilla.
- Persistencia local: Room.
- Preferencias: DataStore.
- Inyeccion de dependencias: Hilt.
- Anotaciones: KSP.
- Sync/Firebase: fase futura, no MVP inicial.
- Modulos Gradle: un solo modulo al inicio.

## Reglas De Arquitectura

- Compose pinta estado y envia eventos.
- ViewModels expondran `UiState` con `StateFlow`.
- Room y DataStore quedan detras de repositorios.
- Casos de uso solo cuando protegen reglas importantes o reutilizables.
- El historial no depende de la rutina actual.
- Editar una rutina no modifica entrenamientos antiguos.

## Modelo Principal

Rutinas editables:

- `RoutineEntity`
- `RoutineDayEntity`
- `RoutineExerciseEntity`

Historico inmutable:

- `WorkoutSessionEntity`
- `WorkoutExerciseEntity`
- `WorkoutSetEntity`

Preferencias:

- rutina activa
- unidad de peso
- ajustes simples

## Roadmap

### Fase 0 - Mobile foundation

Estado: completada tecnicamente.

Objetivo:

- Crear base Compose moderna.
- Configurar Hilt, Room, DataStore, Navigation Compose y KSP.
- Crear estructura por features.
- Crear documentacion inicial.
- Preparar modelo historico snapshot.

### Fase 1 - Rutinas

Estado: completada tecnicamente.

Objetivo:

- Listar rutinas.
- Crear rutinas con dias y ejercicios.
- Editar rutinas reemplazando dias/ejercicios.
- Archivar rutinas.
- Seleccionar rutina activa en DataStore.

Fuera de alcance:

- Registrar entrenamientos.
- Estadisticas.
- Firebase.
- Sincronizacion.

### Fase 2 - Registro de entrenamiento

Estado: completada tecnicamente.

Objetivo:

- Detectar siguiente dia de rutina activa.
- Crear sesion desde snapshot de rutina.
- Registrar peso y repeticiones por serie.
- Finalizar sesion.
- Verificar ciclo de dias y semana.

### Fase 3 - Historial

Estado: siguiente fase.

Objetivo:

- Listar sesiones pasadas.
- Mostrar detalle de ejercicios y series.
- Confirmar que editar rutinas no altera historico.

### Fase 4 - Estadisticas MVP

Objetivo:

- Progreso por ejercicio.
- Volumen por sesion.
- Mejores marcas simples.

### Fase 5 - Pulido UX

Objetivo:

- Estados vacios.
- Loading y errores.
- Confirmaciones.
- Accesibilidad basica.
- Recorrido de usuario nuevo.

### Fase 6 - Sync futura

Objetivo:

- Evaluar Firebase/Auth/Firestore o alternativa.
- Disenar sync offline-first antes de implementar.

## Definition Of Done

Cada fase termina solo si:

- La app compila.
- Tests relevantes pasan.
- Flujo principal probado manualmente si aplica.
- Documentacion actualizada.
- Cambios limitados al alcance.
- Rama lista para commit/merge.
