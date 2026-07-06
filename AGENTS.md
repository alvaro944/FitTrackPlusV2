# FitTrackPlus Agent Rules

Reglas ligeras para trabajar cómodo en este proyecto. No conviertas cada tarea en un proceso SDD: SDD solo cuando se pida explícitamente o la feature sea grande.

## Idioma y tono

- Respuestas al usuario en español de España. No usar voseo ni giros rioplatenses.
- Artefactos técnicos, código, identificadores y comentarios de código en inglés salvo que el contexto pida otra cosa.
- Documentación del proyecto en español claro, manteniendo el estilo existente del repo.

## Ramas y commits

- No usar prefijos de agente o herramienta en nombres de rama.
- Usar prefijos semánticos: `feature/`, `bug/`, `fix/`, `refactor/`, `cherrypick/` o `docs/`.
- El nombre de la rama debe describir lo que se toca, no quién lo ejecuta.
- Commits con Conventional Commits, sin `Co-Authored-By` ni atribución IA.

## Lectura inicial

- No leer toda la documentación por defecto.
- Para tareas normales, revisar solo el área afectada y los docs necesarios.
- Para fases grandes, planificación o SDD, revisar los docs relevantes:
  - `README.md`
  - `docs/methodology/project-methodology/README.md`
  - `docs/planning/project-plan.md`
  - `docs/progress/project-progress.md`
  - `docs/progress/phase-log.md`
  - `docs/architecture/overview.md`
  - `docs/methodology/work-methodology/README.md`

## Modo de trabajo

- Trabajar por cambios pequeños y acotados.
- Usar una rama por fase cuando el workspace tenga git.
- No mezclar refactors con features.
- La v2 vive en `app/src/main/kotlin`.
- `app/src/main/java` es legacy local y no forma parte del nuevo repo.
- Mantener Firebase fuera del MVP hasta que el flujo local esté cerrado.
- Verificar antes de afirmar que algo está terminado.
- Subagentes solo para tareas grandes o independientes donde aporten valor real.
- No lanzar emulador salvo petición explícita del usuario.

## Arquitectura

- Compose no debe contener lógica de negocio.
- ViewModels deben exponer estado observable y recibir eventos.
- Repositorios ocultan Room/DataStore a la UI.
- Casos de uso solo cuando encapsulan reglas reutilizables o importantes.
- El historial se guarda como snapshot histórico.

## Verificación mínima

Para cambios de código, usar los comandos disponibles en este entorno, normalmente:

```bash
./gradlew test
./gradlew build
```

En Windows/PowerShell puede equivaler a:

```powershell
.\\gradlew.bat test
.\\gradlew.bat build
```

Para cambios de UI, hacer pasada manual en emulador o dispositivo solo cuando el usuario lo pida o cuando se acuerde explícitamente.

## Documentación de progreso

- Actualizar docs de progreso/metodología solo cuando la tarea lo requiera o deje un aprendizaje reusable.
- No mezclar cambios documentales ajenos con una feature o bugfix pequeño.
