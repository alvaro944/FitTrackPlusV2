# File Blueprints

Este documento da el contenido minimo que conviene definir en los ficheros operativos habituales de un proyecto.

No son templates cerrados. Son esqueletos para no olvidar piezas importantes.

## `AGENTS.md`

Es el documento operativo del repo.

Se deriva de la metodologia general, pero queda mezclado con reglas concretas del proyecto.

Debe dejar claro:

- que leer antes de editar
- agente principal por defecto
- agentes auxiliares permitidos y con que limites
- mapa de plataformas: Codex, Claude, herramientas visuales, revisores u otras
- reglas de alcance
- arquitectura o limites importantes
- verificacion minima
- protocolo de handoff
- fichero compartido de coordinacion
- cuando actualizar metodologia general
- reglas de cierre de iteracion

Estructura recomendada:

- lectura inicial
- modelo de agentes
- colaboracion entre plataformas
- reglas obligatorias
- modo ligero vs modo fase
- reglas de ownership
- contexto del proyecto
- arquitectura o limites del repo
- verificacion minima
- documentacion y cierre
- handoff
- coordination log

Minimo recomendado:

```markdown
## Agente Principal

- Codex actua como ejecutor principal por defecto.
- Otros agentes pueden asistir o ejecutar tareas acotadas si el usuario lo decide.

## Colaboracion

- Una sola herramienta ejecuta cambios sobre la misma zona del repo.
- Todo relevo debe dejar handoff.
- Codex, Claude y otras plataformas deben tener roles diferenciados.
- Las propuestas externas se validan contra repo, alcance y docs antes de ejecutarse.

## Plataformas

- Ejecutor principal: completar en el proyecto.
- Planners o reviewers permitidos: completar en el proyecto.
- Herramientas visuales o externas: completar en el proyecto.
- Integrador de cambios paralelos: completar en el proyecto.

## Reglas Obligatorias

- Verificar antes de declarar terminado.
- No pisar cambios ajenos.
- No mezclar refactors con features sin decision explicita.
- Dejar pendiente explicito si algo no se pudo verificar.

## Modos De Trabajo

- Usar modo ligero para cambios acotados y de bajo riesgo.
- Usar modo fase para cambios amplios, cross-cutting o de producto.
- Si entra otro ejecutor o crece el alcance, resincronizar antes de seguir.

## Contexto Del Proyecto

- Stack: completar en el proyecto.
- Carpetas relevantes: completar en el proyecto.
- Arquitectura: completar en el proyecto.
- Comandos de verificacion: completar en el proyecto.

## Metodologia

- La metodologia general cambia solo cuando aparece una regla reusable.
- Los aprendizajes especificos del repo viven en docs locales.

## Handoff

- Todo handoff debe incluir objetivo, hecho, archivos tocados, verificacion, pendiente, riesgos y siguiente paso.
- Si colaboran varias plataformas, el handoff debe quedar tambien en `docs/coordination-log.md` o equivalente.
```

## `README.md`

Debe dejar claro:

- que es el proyecto
- estado actual
- stack
- comandos base
- docs importantes

## `project-plan.md`

Debe dejar claro:

- fases o bloques
- prioridades
- que esta dentro y fuera de alcance

## `project-progress.md`

Debe dejar claro:

- donde estamos hoy
- que se ha hecho ya
- que esta pendiente
- cual es el siguiente paso real

## `phase-log.md`

Debe dejar claro:

- que se hizo en cada iteracion
- que problemas aparecieron
- que decisiones se tomaron
- que verificacion paso

## `coordination-log.md`

Debe dejar claro:

- integrador actual
- iteracion activa
- modo de trabajo
- zonas con ownership activo
- propuestas pendientes de decision
- handoffs entre plataformas
- verificaciones o pendientes relevantes para el relevo

No debe ser:

- una bitacora historica completa
- un duplicado del progreso
- un lugar para secretos
- una lista infinita de ideas sin decision

Minimo recomendado:

```markdown
# Coordination Log

## Estado Actual

- Integrador actual:
- Iteracion activa:
- Modo:
- Zonas con ownership activo:

## Pendiente Entre Plataformas

- [ ] ...

## Handoffs

### YYYY-MM-DD - Origen -> Destino

Rol/ownership:
- ...

Hecho:
- ...

Verificacion:
- ...

Pendiente:
- ...

No pisar:
- ...
```

## `architecture.md`

Debe dejar claro:

- capas o modulos
- reglas de dependencia
- fuente de verdad
- decisiones tecnicas relevantes

## Regla De Uso

La metodologia general define la estructura mental.

Estos ficheros aterrizan esa estructura al proyecto concreto.

Despues del kickoff, `AGENTS.md` es la referencia operativa diaria. La carpeta metodologica queda como base reusable y se consulta solo para reajustar el sistema de trabajo o promover reglas generales.
