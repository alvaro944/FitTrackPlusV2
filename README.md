# FitTrackPlus v2

FitTrackPlus es una aplicacion Android nativa para crear rutinas de gimnasio, registrar entrenamientos reales y conservar un historial consistente aunque las rutinas cambien con el tiempo.

## Estado Actual

Rama de trabajo actual: `codex/v2-mejoras`.
Fase 6 y su bloque de polish visual/accesibilidad estan cerrados tecnicamente:
- espaciado remapeado a `FitSpacing` en las pantallas principales
- `core/design` dividido por componentes (`Cards`, `Labels`, `States`, `Indicators`)
- transiciones `fadeIn/fadeOut` en navegacion y `AnimatedContent` en Historial
- semantics en barras de progreso y targets tactiles revisados en Rutinas
Validacion manual pendiente: prueba en movil/emulador para intro, transiciones, accesibilidad y dark mode.
Branding base cerrado: docs de marca, logo e icono adaptativo ya definidos.
La intro de arranque clara ya vive en Compose; Bloque 8 queda solo para validacion o ajuste real si aparece un problema.
Siguiente trabajo recomendado: validacion manual del polish visual, luego Bloque 7 (GitHub Actions CI). `strings.xml` sigue diferido hasta estabilizar copy.

La app esta arrancando una base nueva en Jetpack Compose. La v2 se compila desde `app/src/main/kotlin`.

Para no perder contexto entre sesiones:

- Plan maestro: `docs/project-plan.md`
- Progreso actual: `docs/project-progress.md`
- Bitacora por fases: `docs/phase-log.md`
- Arquitectura: `docs/architecture.md`
- Starter pack metodologico reusable: `docs/project-methodology/`
- Aprendizajes y notas del repo: `docs/work-methodology/`

## Decisiones Base

- Plataforma: Android nativo.
- Lenguaje: Kotlin.
- UI: Jetpack Compose + Material 3.
- Persistencia: Room para datos estructurados.
- Preferencias: DataStore.
- Inyeccion de dependencias: Hilt.
- Procesamiento de anotaciones: KSP.
- Sync/Firebase: fuera del MVP inicial.

## Arquitectura

La app sigue una arquitectura sencilla por features:

```text
com.alvarocervantes.fittrackplus
+-- core
+-- data
+-- domain
+-- feature
```

Reglas principales:

- Las pantallas Compose pintan estado y envian eventos.
- Los ViewModels expondran `UiState` con `StateFlow`.
- La logica de negocio vive en repositorios o casos de uso.
- Room es la fuente de verdad local.
- El historial guarda snapshots para no depender de la rutina actual.

## Comandos

En Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat test
```

Cuando existan pruebas instrumentadas:

```powershell
.\gradlew.bat connectedAndroidTest
```

## Roadmap

1. Fase 0 - Base movil seria: Compose, Hilt, Room, DataStore, Navigation y docs. Completada tecnicamente.
2. Fase 1 - Rutinas: crear, editar, archivar y seleccionar rutina activa. Completada tecnicamente.
3. Fase 2 - Registro: iniciar sesion desde rutina, registrar series y finalizar. Completada tecnicamente.
4. Fase 3 - Historial: listar sesiones y mostrar detalle historico. Completada tecnicamente.
5. Fase 4 - Estadisticas MVP: progreso por ejercicio, volumen y mejores marcas. Completada tecnicamente.
6. Fase 5 - Pulido UX funcional: estados, accesibilidad, errores y recorrido de usuario nuevo. Completada tecnicamente.
7. Fase 6 - UI visual / Front con herramienta: sistema visual, navegacion y pantallas Compose redisenadas sin cambiar reglas de negocio. Completada tecnicamente.
8. Bloque 3 UX + polish visual/accesibilidad de Fase 6: implementado en local y verificado con `test` + `build`, pendiente de validacion manual y push.
9. Bloque 7 - CI: crear workflow de GitHub Actions cuando la validacion manual del polish no detecte ajustes extra.
10. Bloque 4 - Funcionalidad nueva de valor: export de historial, referencia de peso anterior y mejora de stats, despues de cerrar visual/CI.
11. Fase 7 - Sync futura: evaluar Firebase/Auth/Firestore o alternativa cuando el nucleo local y visual este estable.

## Metodologia

Este proyecto usa entrega por fases:

- una rama por fase
- alcance pequeno y cerrado
- verificacion automatica
- verificacion manual
- documentacion actualizada
- guia de metodologia actualizada al cerrar fase

La metodologia completa esta en `docs/development-workflow.md`.
