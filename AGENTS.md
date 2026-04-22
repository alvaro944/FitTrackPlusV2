# FitTrackPlus Agent Rules

Estas reglas mantienen el proyecto coherente en sesiones futuras con IA.

## Lectura Inicial

Antes de editar codigo, revisar:

1. `README.md`
2. `docs/development-workflow.md`
3. `docs/project-plan.md`
4. `docs/project-progress.md`
5. `docs/phase-log.md`
6. `docs/architecture.md`
7. `docs/work-methodology/README.md`
8. El area concreta que se va a tocar

## Modo De Trabajo

- Trabajar por fases pequenas.
- Usar una rama por fase cuando el workspace tenga git.
- No mezclar refactors con features.
- La v2 vive en `app/src/main/kotlin`.
- `app/src/main/java` es legacy local y no forma parte del nuevo repo.
- Mantener Firebase fuera del MVP hasta que el flujo local este cerrado.
- Verificar antes de afirmar que algo esta terminado.
- Al cerrar cada fase, actualizar tambien `docs/work-methodology/`.
- Al cerrar cada fase, comentar al usuario que avances, docs y aprendizajes se han anotado.

## Arquitectura

- Compose no debe contener logica de negocio.
- ViewModels deben exponer estado observable y recibir eventos.
- Repositorios ocultan Room/DataStore a la UI.
- Casos de uso solo cuando encapsulan reglas reutilizables o importantes.
- El historial se guarda como snapshot historico.

## Verificacion Minima

Para cambios de codigo:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Para cambios de UI, hacer tambien una pasada manual en emulador o dispositivo cuando sea posible.

## Cierre De Fase

Antes de cerrar una fase:

- Revisar cambios.
- Ejecutar verificacion minima.
- Actualizar `docs/project-progress.md`.
- Actualizar `docs/phase-log.md`.
- Actualizar `docs/work-methodology/` con procedimientos, tips, skills practicadas y aprendizajes.
- Informar al usuario que se hizo, que se verifico y que queda pendiente.
