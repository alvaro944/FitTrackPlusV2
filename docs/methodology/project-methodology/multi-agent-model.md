# Multi-Agent Model

Este documento define un modelo portable para trabajar con varias IAs, herramientas de diseno, editores y plataformas sin perder control.

## Roles

- **usuario**: decide prioridades, alcance y cierres
- **primary-executor**: implementa la iteracion principal, verifica y actualiza docs
- **bounded-executor**: ejecuta una tarea acotada con ownership claro
- **assistant**: investiga, revisa, disena, propone o resume sin tocar el repo compartido
- **reviewer**: revisa riesgos, bugs y coherencia
- **herramientas visuales**: aportan referencia o material de diseno

## Regla Central

Solo debe haber un ejecutor principal sobre el repo compartido en cada iteracion.

Por defecto, Codex actua como `primary-executor`.

Claude u otros agentes pueden actuar como `assistant`, `reviewer` o `bounded-executor` si el usuario lo decide.

Para flujos concretos entre plataformas, ver `cross-platform-collaboration.md`.

## Ownership E Integracion

La regla no prohibe colaboracion paralela. Prohibe ownership ambiguo.

- una iteracion debe tener un unico integrador responsable del cierre
- cada `bounded-executor` debe tener archivos, modulo o zona asignada
- nadie debe modificar la zona asignada a otro ejecutor sin resincronizar
- el integrador revisa, adapta e integra los resultados antes de cerrar

## Reparto Sano De Trabajo

- el usuario prioriza y valida
- Codex ejecuta por defecto
- otras herramientas ayudan con exploracion, revision o piezas acotadas
- las herramientas visuales inspiran o entregan material, pero no fijan verdad tecnica

## Relevo Entre Herramientas

Cuando se cambia de editor, app o agente:

- revisar `git status`
- releer docs de estado
- abrir de nuevo la zona concreta a tocar
- identificar que cambios siguen sin verificar
- no continuar desde memoria conversacional sin contraste

## Relevo Codex / Claude

Cuando Codex y Claude colaboren, usar esta separacion por defecto:

- Codex: ejecutor principal e integrador del repo
- Claude: planner, reviewer, assistant o ejecutor acotado si el usuario lo pide
- si Claude propone, Codex contrasta con repo, alcance y docs antes de implementar
- si Claude ejecuta, debe dejar handoff con ownership, cambios, verificacion y pendientes
- si Codex toma trabajo iniciado por Claude, primero revisa estado real y cambios locales

## Cuando Si Tiene Sentido Delegar

- investigacion acotada
- revision de una zona concreta
- propuestas visuales
- tareas separadas con ownership claro
- implementaciones pequenas con archivos asignados

## Cuando No Conviene Delegar

- cuando dos herramientas tocarian la misma zona a la vez
- cuando el trabajo requiere contexto muy fino y continuo del repo
- cuando el siguiente paso critico depende del resultado inmediato
- cuando no hay handoff posible o no queda claro quien integra

## Gestion De Propuestas Externas

- tratarlas como entrada
- no tratarlas como decision
- extraer solo lo que encaja con el alcance real
- registrar la decision final en docs del proyecto, no solo en la conversacion
