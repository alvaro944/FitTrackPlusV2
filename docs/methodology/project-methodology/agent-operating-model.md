# Agent Operating Model

Este documento define como debe trabajar el agente principal dentro de un proyecto.

## Regla Base

Codex es el ejecutor principal por defecto.

Esto no significa que otros agentes no puedan trabajar. Significa que, si no se indica otra cosa, Codex es quien:

- contrasta el estado real del repo
- implementa cambios
- verifica
- actualiza documentacion
- deja cierre o handoff

## Responsabilidades Del Ejecutor Principal

Antes de editar:

- leer las reglas operativas del repo
- revisar el estado real
- abrir el area concreta que se va a tocar
- distinguir propuesta, decision y tarea encargada

Durante la ejecucion:

- tocar solo el alcance definido
- respetar cambios ajenos
- no mezclar refactors con features
- verificar pronto cuando el cambio pueda romper compilacion o flujo

Al cerrar:

- resumir que se hizo
- indicar que se verifico
- anotar que queda pendiente
- actualizar docs de estado si corresponde
- dejar handoff si otra herramienta o sesion va a continuar

## Propuestas Externas

Una propuesta de Claude, otra IA, una herramienta visual o un backlog no se ejecuta automaticamente.

Antes de implementarla:

- contrastar con el repo
- confirmar que encaja con el alcance actual
- comprobar si ya hay cambios locales relacionados
- registrar la decision final si se acepta

## Limites Del Ejecutor

El ejecutor principal no debe:

- reescribir trabajo ajeno sin permiso
- tocar zonas no relacionadas por conveniencia
- asumir que una nota externa es verdad final
- cerrar una iteracion sin verificacion o pendiente explicito

## Cuando Otro Agente Ejecuta

Otro agente puede ejecutar si el usuario lo decide.

En ese caso debe trabajar como `bounded-executor`:

- alcance cerrado
- archivos o modulo asignado
- reglas de no pisar trabajo ajeno
- resumen final obligatorio
- verificacion indicada o pendiente explicito
