# Development Workflow

FitTrackPlus v2 se desarrolla como una secuencia de fases cerradas. El objetivo es aprender y construir una app movil seria sin meter complejidad antes de tiempo.

## Reglas Base

- Trabajar en cambios pequenos e incrementales.
- Usar una rama por fase o tarea cuando exista git.
- Mantener una responsabilidad clara por rama.
- Validar cada cambio con comandos.
- Probar manualmente los flujos visibles para el usuario.
- Actualizar documentacion cuando cambie arquitectura, comportamiento o proceso.
- Actualizar la guia de metodologia en `docs/work-methodology/` al cerrar cada fase.
- No llevar Firebase, estadisticas avanzadas o sync al presente si pertenecen a una fase futura.

## Ramas

Formato recomendado:

```text
codex/<scope>-<short-description>
```

Ejemplos:

```text
codex/phase-0-mobile-foundation
codex/phase-1-routines
codex/phase-2-workout-logging
```

Estado actual de Git:

- Repo local inicializado.
- Rama actual: `codex/phase-3-history`.
- No hay remoto configurado.
- No se ha subido nada a la nube.

## Definition Of Done

Una fase esta terminada solo si:

- El comportamiento previsto existe.
- La app compila.
- Los tests relevantes pasan.
- El flujo principal se ha probado manualmente si aplica.
- La documentacion esta alineada.
- No hay cambios fuera del alcance.

## Validacion Automatizada

Comandos base en Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Cuando haya pruebas instrumentadas:

```powershell
.\gradlew.bat connectedAndroidTest
```

## Validacion Manual Por Fase

Fase 0:

- Abrir la app.
- Confirmar que carga la shell Compose.
- Cambiar entre tabs principales.
- Confirmar que no aparece UI XML/Fragment legacy.

Fase 1:

- Crear rutina Push/Pull/Legs.
- Editar dias y ejercicios.
- Seleccionar rutina activa.
- Cerrar y abrir la app para comprobar persistencia.

Fase 2:

- Iniciar entrenamiento desde rutina activa.
- Registrar peso y reps por serie.
- Finalizar sesion.
- Completar mas de una vuelta y comprobar el ciclo de dias.

Fase 3:

- Ver sesiones pasadas.
- Revisar detalle de ejercicios y series.
- Editar una rutina y confirmar que el historial antiguo no cambia.

## Cierre De Fase

Antes de cerrar:

- Revisar archivos modificados.
- Ejecutar verificacion fresca.
- Actualizar README, `AGENTS.md` o docs si cambia el proceso.
- Actualizar `docs/project-progress.md`.
- Actualizar `docs/phase-log.md`.
- Actualizar `docs/work-methodology/` con procedimientos, tips y aprendizajes reutilizables.
- Guardar futuras ideas en `docs/future-improvements.md`.
- Comentar al usuario que documentacion se actualizo y que queda pendiente.
