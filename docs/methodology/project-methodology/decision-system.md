# Decision System

Este documento fija una estructura portable para decidir bien cuando hay varias fuentes de contexto.

## 1. Jerarquia De Autoridad

Orden recomendado:

1. instrucciones explicitas del usuario
2. reglas operativas del repo
3. estado real del repositorio
4. documentacion viva del proyecto
5. backlog validado
6. propuestas externas o notas auxiliares

## 2. Jerarquia De Artefactos

### Operativos

Responden a: **como se trabaja**

- `AGENTS.md`
- workflow operativo
- metodologia reusable

### Estado

Responden a: **donde estamos**

- `README.md`
- `project-progress.md`
- `phase-log.md`

### Direccion

Responden a: **hacia donde vamos**

- roadmap
- plan activo
- backlog priorizado

### Apoyo

Responden a: **que ayuda a pensar**

- notas de otras IAs
- exploraciones visuales
- documentos externos

## 3. Estados De Una Idea

Conviene clasificar cualquier tema en uno de estos estados:

1. observacion
2. propuesta
3. decision
4. implementacion
5. verificacion
6. cierre

Error comun:

- saltar de observacion o propuesta a implementacion sin una decision clara

## 4. Regla De Sincronizacion

Antes de ejecutar un cambio, confirmar:

- que se quiere hacer
- por que se quiere hacer ahora
- donde vive la verdad actual de esa decision

Si una de esas tres cosas no esta clara, primero toca releer o preguntar.
