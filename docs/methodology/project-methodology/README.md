# Project Methodology Starter Pack

Esta carpeta es la base metodologica reusable para proyectos futuros.

No define el stack, el roadmap ni la arquitectura concreta de este repo. Define **como organizar el trabajo** para que luego sea facil aterrizarlo a un proyecto especifico con muy pocos datos extra.

## Para Que Sirve

Usa esta carpeta cuando quieras crear o ajustar:

- `AGENTS.md`
- workflow operativo
- reglas de colaboracion multiagente
- sistema de documentacion viva
- metodo de iteraciones
- criterios de verificacion

La idea es que para un proyecto nuevo solo tengas que aportar:

- stack
- arquitectura concreta
- restricciones
- roadmap o plan
- comandos de verificacion

## Que Contiene

- `operating-principles.md`: principios generales de trabajo
- `decision-system.md`: jerarquia de autoridad, artefactos y estados de una idea
- `multi-agent-model.md`: roles, relevo entre herramientas y trabajo multiagente
- `cross-platform-collaboration.md`: como coordinar Codex, Claude y otras plataformas
- `agent-operating-model.md`: reglas del agente ejecutor principal
- `collaboration-modes.md`: modos de colaboracion entre agentes y herramientas
- `handoff-protocol.md`: protocolo de relevo entre herramientas, agentes o sesiones
- `iteration-model.md`: ciclo completo de una iteracion
- `documentation-system.md`: como separar estado, direccion, metodologia y bitacora
- `quality-and-verification.md`: verificacion proporcional y reglas practicas
- `methodology-maintenance.md`: cuando actualizar la metodologia general y cuando no
- `anti-patterns.md`: errores recurrentes que conviene detectar pronto
- `project-kickoff.md`: como usar esta base antes de empezar el desarrollo tecnico
- `project-adaptation.md`: como aterrizar esta base a un proyecto real
- `file-blueprints.md`: esqueletos y contenidos minimos para los ficheros operativos habituales

## Como Leerlo

Orden recomendado:

1. `operating-principles.md`
2. `decision-system.md`
3. `multi-agent-model.md`
4. `cross-platform-collaboration.md`
5. `agent-operating-model.md`
6. `collaboration-modes.md`
7. `handoff-protocol.md`
8. `iteration-model.md`
9. `documentation-system.md`
10. `quality-and-verification.md`
11. `methodology-maintenance.md`
12. `anti-patterns.md`
13. `project-kickoff.md`
14. `project-adaptation.md`
15. `file-blueprints.md`

Los archivos numerados antiguos y `development-workflow.md` se mantienen solo como puentes de compatibilidad. Para trabajo nuevo, usar los documentos anteriores.

## Regla Principal

Esta carpeta es la **fuente canonica** de metodologia general.

Si el repo mantiene otras carpetas de metodologia o notas de trabajo:

- esta carpeta fija la base reusable
- las otras carpetas pueden guardar aprendizajes concretos, historicos o especificos del proyecto

## Regla De Mantenimiento

Esta carpeta debe cambiar poco.

No se actualiza en cada iteracion. Solo se modifica cuando aparece una regla clara, repetible y reusable entre proyectos. Los aprendizajes locales viven primero en la documentacion especifica del repo.

## Uso En Un Proyecto Nuevo

1. definir primero stack, arquitectura, restricciones, roadmap y verificaciones reales
2. leer esta carpeta como base metodologica de arranque
3. generar `AGENTS.md`, README, roadmap, progreso y bitacora desde `project-kickoff.md`, `project-adaptation.md` y `file-blueprints.md`
4. operar el proyecto desde `AGENTS.md` y los docs vivos del repo
5. mantener los aprendizajes locales fuera de esta carpeta salvo que se conviertan en reglas portables
