# Collaboration Modes

Este documento define modos de colaboracion para agentes y herramientas.

## Como Elegir Modo

- usa `planner` cuando aun falta decidir alcance, orden o riesgos
- usa `assistant` cuando hace falta apoyo sin tocar el repo compartido
- usa `reviewer` cuando ya hay cambios, propuesta o plan que evaluar
- usa `bounded-executor` cuando una tarea puede aislarse por archivos o zona
- usa `primary-executor` cuando hay que integrar, verificar y cerrar la iteracion

Regla practica:

- si alguien edita, debe tener ownership claro
- si alguien no tiene ownership claro, debe actuar como planner, assistant o reviewer

## `planner`

Analiza, estructura y propone.

Puede:

- leer contexto
- comparar opciones
- crear planes
- detectar riesgos

No debe:

- editar repo
- ejecutar tareas de cierre
- tratar su plan como decision final

## `assistant`

Ayuda en una parte del trabajo sin ser ejecutor.

Puede:

- investigar
- resumir
- revisar docs
- proponer copy o diseno
- preparar alternativas

No debe:

- tocar zonas compartidas del repo
- cerrar la iteracion
- asumir ownership general

## `primary-executor`

Ejecuta la iteracion principal.

Puede:

- editar repo
- verificar
- actualizar docs
- cerrar o dejar handoff

Debe:

- revisar estado real
- respetar alcance
- mantener trazabilidad
- coordinar con cambios ajenos

## `bounded-executor`

Ejecuta una tarea acotada.

Debe tener:

- objetivo cerrado
- archivos o modulo asignado
- limites claros
- handoff final

No debe:

- ampliar alcance
- tocar archivos fuera de ownership
- cerrar la fase completa salvo encargo explicito

## `reviewer`

Revisa riesgos, bugs, coherencia y calidad.

Puede:

- leer cambios
- senalar problemas
- recomendar ajustes

No debe:

- reescribir sin encargo
- mezclar review con feature nueva

## Regla De Concurrencia

Dos ejecutores no deben tocar la misma zona del repo al mismo tiempo.

Si hace falta trabajo paralelo:

- dividir ownership
- definir archivos o modulos
- exigir handoff de cada parte

## Cambio De Modo

Un agente puede cambiar de modo solo si queda explicito.

Ejemplos:

- `planner` -> `primary-executor`: el usuario acepta el plan y encarga ejecucion
- `assistant` -> `bounded-executor`: se asignan archivos y limites concretos
- `bounded-executor` -> `reviewer`: termina su parte y pasa a revisar sin editar
- `reviewer` -> `primary-executor`: solo si el usuario encarga aplicar los cambios

Si el modo no esta claro, asumir el modo menos invasivo.
