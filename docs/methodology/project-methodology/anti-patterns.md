# Anti-Patterns

Este documento recoge errores que conviene detectar pronto.

## Ejecutar Backlog Como Decision

Sintoma:

- una nota externa se implementa sin validacion.

Correccion:

- contrastar con el repo y confirmar alcance antes de ejecutar.

## Editar Desde Memoria Conversacional

Sintoma:

- el agente trabaja segun lo que recuerda, sin abrir archivos ni revisar estado.

Correccion:

- partir de `git status`, docs vivas y area concreta.

## Mezclar Feature, Refactor Y Polish

Sintoma:

- una tarea pequena acaba tocando arquitectura, UI y deuda a la vez.

Correccion:

- separar iteraciones o justificar explicitamente por que deben ir juntas.

## Actualizar Metodologia Por Cualquier Detalle

Sintoma:

- cada fase modifica la metodologia general aunque el aprendizaje sea local.

Correccion:

- guardar aprendizajes especificos en docs del repo y promover solo reglas reutilizables.

## Dejar Trabajo Sin Handoff

Sintoma:

- otro agente necesita reconstruir contexto desde la conversacion.

Correccion:

- dejar handoff con objetivo, hecho, pendiente, verificacion y riesgos.

## Dos Ejecutores Sobre La Misma Zona

Sintoma:

- dos herramientas editan los mismos archivos o modulos sin coordinarse.

Correccion:

- parar, asignar ownership y decidir quien integra.

## Cambios Visuales Que Fuerzan Dominio

Sintoma:

- una decision estetica provoca cambios de datos, arquitectura o reglas no necesarios.

Correccion:

- adaptar el diseno al sistema existente o abrir una iteracion tecnica separada.

## Cierre Sin Verificacion

Sintoma:

- se declara terminado sin tests, build, revision manual o pendiente explicito.

Correccion:

- ejecutar verificacion proporcional o documentar claramente por que queda pendiente.
