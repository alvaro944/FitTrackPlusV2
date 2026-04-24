# Trabajo Con Agentes Y Herramientas

Esta guia describe como colaborar con varias IAs, editores, herramientas de diseno y plataformas de trabajo sin perder control sobre el proyecto.

No esta pensada solo para este repo. La idea es fijar reglas que sigan siendo utiles aunque cambie el stack o el agente principal.

## Objetivo

Coordinar varias fuentes de ayuda sin caer en estos errores:

- mezclar propuesta y decision
- editar sobre contexto desactualizado
- duplicar trabajo entre herramientas
- perder trazabilidad de por que se hizo algo

## Roles Recomendados

Conviene distinguir cuatro roles:

- **usuario**: fija prioridades, valida direccion y autoriza ejecucion
- **agente ejecutor**: implementa en el repo y deja verificacion/documentacion
- **agentes auxiliares**: investigan, revisan, resumen o proponen
- **herramientas visuales/prototipos**: aportan referencia, no autoridad tecnica

La regla importante es esta:

- una sola herramienta ejecuta cambios sobre el repo compartido en cada iteracion

## Regla De Oro

Una propuesta externa no es una instruccion de ejecucion.

Antes de implementar algo propuesto por otra IA, prototipo o backlog:

1. releer el estado real del repo
2. contrastar la propuesta con la documentacion viva
3. confirmar que el usuario realmente quiere ese cambio ahora

## Modos De Trabajo

Conviene pensar cada iteracion en uno de estos modos:

- **leer**
- **analizar**
- **proponer**
- **implementar**
- **verificar**
- **cerrar**

No todas las herramientas deben tener permiso para todos los modos.

Ejemplo de reparto sano:

- una herramienta visual: analizar/proponer
- un agente tecnico: implementar/verificar
- el usuario: decidir y priorizar

## Fuente De Verdad

Para coordinar sin deriva:

- el repo dice lo que existe
- la documentacion viva dice en que estado esta
- la metodologia dice como conviene trabajar
- las notas externas solo aportan contexto

Si una conversacion recuerda algo que el repo no refleja, gana el repo.

## Relevo Entre Plataformas

Cuando se cambia de app, editor o agente:

- revisar `git status`
- releer docs de estado
- abrir de nuevo la zona a modificar
- identificar que cambios siguen sin verificar
- no continuar a ciegas por memoria conversacional

## Cuando Varias Herramientas Trabajan Sobre El Mismo Tema

Patron recomendado:

1. una herramienta explora o propone
2. otra contrasta con el repo
3. el usuario decide
4. un unico ejecutor implementa
5. ese mismo ejecutor verifica y actualiza docs

Esto evita solapes y respuestas inconsistentes.

## Gestion De Propuestas Externas

Cuando una herramienta deja backlog, comentarios o planes:

- tratarlos como entrada
- no tratarlos como verdad final
- extraer solo lo que encaja con el alcance real
- registrar la decision final en los docs del proyecto, no solo en la conversacion

## Permiso De Ejecucion

Regla portable y segura:

- leer, analizar y proponer suele requerir menos riesgo
- editar, ejecutar comandos pesados, crear ramas o cerrar fases requiere encargo claro

Cuando haya duda entre seguir o preguntar, conviene preguntar antes de tocar partes sensibles del repo.

## Cierre Correcto

Una iteracion multiagente esta bien cerrada si:

- queda claro quien ejecuto
- queda claro que se decidio realmente
- la verificacion esta hecha o explicitamente pendiente
- la documentacion refleja el nuevo estado
- el siguiente paso no depende de reconstruir el contexto desde cero
