# Handoff Protocol

Este protocolo se usa cuando una herramienta, agente o sesion deja trabajo preparado para otra.

## Cuando Hace Falta Handoff

- al cambiar de Codex a Claude o viceversa
- al pausar una iteracion a medias
- al dejar una tarea para una sesion futura
- cuando un agente auxiliar ha ejecutado una parte acotada
- cuando hay cambios locales que aun no estan verificados o publicados

## Contenido Minimo

Un buen handoff debe decir:

- agente o herramienta que entrega
- agente o herramienta esperada para continuar
- rol usado: planner, assistant, reviewer, bounded-executor o primary-executor
- objetivo de la iteracion
- que se hizo
- que archivos o zonas se tocaron
- que queda pendiente
- que comandos se ejecutaron y resultado
- riesgos o puntos delicados
- que no debe pisarse
- siguiente paso recomendado

## Formato Recomendado

```markdown
## Handoff

Origen:
- ...

Destino recomendado:
- ...

Rol/ownership:
- ...

Objetivo:
- ...

Hecho:
- ...

Archivos tocados:
- ...

Verificacion:
- ...

Pendiente:
- ...

Riesgos:
- ...

No pisar:
- ...

Siguiente paso:
- ...
```

## Reglas Practicas

- escribir el handoff pensando en alguien que no ha leido la conversacion
- usar rutas concretas cuando haya riesgo de solape
- separar hechos de recomendaciones
- no esconder verificaciones pendientes dentro de texto largo

## Handoff De Diseno A Implementacion

Si el relevo viene de una herramienta visual:

- indicar que asset o prototipo es referencia
- separar elementos obligatorios de elementos exploratorios
- aclarar si el runtime debe copiar assets o solo inspirarse en ellos
- indicar restricciones tecnicas conocidas

## Handoff Entre Ejecutores

Si otro agente ha tocado codigo:

- listar archivos modificados
- indicar si hay tests o build pasados
- indicar si quedan cambios sin commit
- advertir de zonas que no deben tocarse sin releer

## Handoff Codex / Claude

Para relevos entre Codex y Claude, anadir:

- quien fue integrador hasta ahora
- si Claude solo propuso o tambien edito
- si Codex debe implementar, revisar o solo continuar
- que propuesta queda aceptada, rechazada o pendiente de decision
- que archivos no deben tocarse sin mirar cambios locales

Regla:

- una conversacion no es handoff suficiente si no deja objetivo, ownership, verificacion y pendiente

## Fichero Compartido

Cuando varias plataformas colaboran, el handoff debe quedar tambien en el fichero compartido de coordinacion del repo.

Nombre recomendado:

- `docs/coordination-log.md`

Regla:

- el chat ayuda a trabajar, pero el fichero compartido permite que otra plataforma continue sin reconstruir contexto desde conversaciones separadas
