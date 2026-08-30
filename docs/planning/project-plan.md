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

Estado: completada tecnicamente.

Objetivo:

- Listar sesiones pasadas.
- Mostrar detalle de ejercicios y series.
- Confirmar que editar rutinas no altera historico.
- Anadir datos demo automaticos solo en builds debug cuando la base esta vacia.

### Fase 4 - Estadisticas MVP

Estado: completada tecnicamente.

Objetivo:

- Progreso por ejercicio.
- Volumen por sesion.
- Mejores marcas simples y 1RM estimado.

### Fase 5 - Pulido UX

Estado: completada tecnicamente.

Objetivo:

- Estados vacios.
- Loading y errores.
- Confirmaciones.
- Accesibilidad basica.
- Recorrido de usuario nuevo.

### Fase 6 - UI visual / Front con herramienta

Estado: completada tecnicamente.

Objetivo:

- Definir diseno visual con una herramienta externa.
- Aplicar el diseno a Compose.
- Revisar tema, navegacion, jerarquia visual, espaciados y componentes.
- Mejorar pantallas principales sin cambiar reglas de negocio.

Fuera de alcance:

- Firebase.
- Sync.
- Cambios en snapshots historicos.

### Cierre tecnico v1

Estado: cerrado tecnicamente.

Objetivo:

- Consolidar la primera version funcional completa antes de abrir mejoras nuevas.
- Mantener la validacion manual como gate pendiente, sin documentarla como completada hasta ejecutarla.
- Usar `docs/planning/roadmap-2.1.md` como direccion oficial post-v1.

Incluye:

- nucleo local-first completo
- rutinas, entrenamiento, historial, estadisticas y ajustes
- sistema visual y branding base
- intro clara en Compose
- CI con `test`, `build` y `detekt`
- documentacion viva actualizada

Pendiente:

- validacion manual en dispositivo/emulador de intro, navegacion, dark mode, accesibilidad y flujo completo

### Roadmap 2.1 - Mejoras post-v1

Documento canonico:

- `docs/planning/roadmap-2.1.md`

Orden recomendado:

1. Gate 0 - Validacion manual de la v1 tecnica.
2. Fase 2.1A - Estabilidad y fricciones criticas.
3. Fase 2.1B - Features de valor.
4. Fase 2.1C - Portfolio WOW.

Fuera del roadmap inmediato:

- sync cloud
- ExerciseCatalog global
- supersets/cardio/RPE
- i18n completa
- import/export avanzado salvo fase dedicada

### Sync futura

Objetivo:

- Evaluar Firebase/Auth/Firestore o alternativa.
- Disenar sync offline-first antes de implementar.
- Ejecutar solo despues de cerrar el nucleo local, la fase visual y las mejoras V2.1 prioritarias.

## Linea Paralela - Branding

No se trata como fase funcional del producto.

Objetivo:

- definir identidad visual y de marca de FitTrackPlus
- fijar personalidad, color, tipografia, logo e icono
- dar coherencia a futuras iteraciones visuales sin mezclarlo con sync o backend

## Definition Of Done

Cada fase termina solo si:

- La app compila.
- Tests relevantes pasan.
- Flujo principal probado manualmente si aplica.
- Documentacion actualizada.
- Cambios limitados al alcance.
- Rama lista para commit/merge.
