# Jerarquia De Trabajo Y Decision

Este documento fija una jerarquia portable para proyectos donde intervienen varias fuentes de contexto: usuario, repo, backlog, agentes, herramientas de diseno y varias plataformas de trabajo.

La idea es evitar dos problemas comunes:

- ejecutar propuestas como si fueran decisiones
- editar sobre una lectura desactualizada del estado real

## 1. Jerarquia De Autoridad

Orden recomendado:

1. instruccion explicita del usuario
2. reglas operativas del repo (`AGENTS.md`, guias equivalentes)
3. estado real del repositorio
4. documentacion viva del proyecto
5. backlog validado
6. propuestas externas, notas o sugerencias de otras herramientas

### Regla Practica

- si dos fuentes se contradicen, gana la que esta mas arriba
- si una fuente de nivel bajo sugiere algo que no encaja con el estado real, primero se revisa, no se ejecuta

## 2. Jerarquia De Artefactos

No todos los archivos significan lo mismo.

### Operativos

- `AGENTS.md`
- guias de workflow/metodologia
- instrucciones activas del usuario

Sirven para decir **como** trabajar.

### Estado

- `README.md`
- `project-progress.md`
- `phase-log.md`

Sirven para decir **donde** esta el proyecto.

### Direccion

- roadmap
- plan activo
- backlog priorizado

Sirven para decir **hacia donde** ir.

### Apoyo

- notas de otras IAs
- carpetas de exploracion visual
- documentos externos

Sirven para **ayudar a pensar**, no para ejecutar automaticamente.

## 3. Jerarquia De Roles

En un flujo multiagente conviene distinguir roles:

- **usuario**: decide prioridades, alcance y cierres
- **agente ejecutor**: implementa, verifica y actualiza docs
- **agentes auxiliares**: proponen, revisan, investigan o exploran
- **herramientas visuales o prototipos**: inspiran o guian, pero no mandan sobre el repo

### Regla Practica

Solo debe haber un ejecutor principal por iteracion sobre el repo compartido.

## 4. Estados De Una Idea

Conviene etiquetar mentalmente cada cosa en uno de estos estados:

1. observacion
2. propuesta
3. decision
4. implementacion
5. verificacion
6. cierre

Muchos errores de coordinacion vienen de saltar de `observacion` a `implementacion` sin pasar por `decision`.

## 5. Relevo Entre Herramientas O Plataformas

Cuando se cambia de editor, app o agente:

- partir siempre del repo, no de la memoria de la conversacion
- revisar `git status`
- releer docs de estado y metodologia
- abrir de nuevo el area concreta a tocar
- identificar que cambios son propios, cuales ajenos y cuales siguen sin verificar

## 6. Regla De Sincronizacion

Antes de ejecutar un cambio, confirmar estas tres cosas:

- **que se quiere hacer**
- **por que se quiere hacer**
- **donde vive la verdad actual de esa decision**

Si una de las tres no esta clara, primero toca releer o preguntar.
