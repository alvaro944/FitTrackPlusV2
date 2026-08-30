# Methodology Maintenance

Este documento define cuando se actualiza la metodologia general.

## Regla Principal

`docs/methodology/project-methodology/` cambia poco.

No se actualiza en cada iteracion por defecto.

## Cuando Si Actualizarla

Actualizar la metodologia general solo si aparece una regla que sea:

- clara
- repetible
- reusable entre proyectos
- independiente del stack
- util para evitar errores futuros

Ejemplos:

- un nuevo protocolo de handoff
- una regla de colaboracion multiagente
- una forma mejor de separar decision e implementacion
- un criterio general de verificacion

## Cuando No Actualizarla

No actualizarla por:

- comandos especificos de un stack
- bugs concretos de una herramienta local
- decisiones de producto
- detalles de arquitectura de un repo
- preferencias visuales de una app concreta

Eso debe vivir en docs especificas del proyecto.

## Relacion Con `work-methodology/`

`docs/methodology/work-methodology/` guarda aprendizajes especificos del repo.

Si un aprendizaje se repite y sirve para otros proyectos, puede promoverse a `docs/methodology/project-methodology/`.

## Promocion De Una Regla

Antes de promover un aprendizaje a metodologia general, comprobar:

- ha aparecido mas de una vez o evita un riesgo claro
- no depende del stack
- se puede explicar en una regla corta
- ayuda a futuros agentes o sesiones

## Prueba Rapida De Promocion

Antes de editar esta carpeta, responder:

- serviria igual en otro proyecto con otro stack?
- reduce una decision repetida, un riesgo de handoff o un fallo de verificacion?
- puede vivir como regla breve sin contar la historia local?
- si se borra el nombre del proyecto, sigue teniendo sentido?

Si alguna respuesta es no, guardarlo en docs especificas del repo.

## Coste De La Metodologia

Una metodologia demasiado pesada deja de usarse.

Por eso debe:

- ser corta
- tener reglas accionables
- distinguir modo ligero y modo fase
- evitar documentacion ceremonial
- preferir checklist pequeno antes que protocolo largo
- eliminar reglas que no cambien la conducta real
