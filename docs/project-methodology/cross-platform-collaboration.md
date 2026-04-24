# Cross-Platform Collaboration

Este documento define como coordinar varias plataformas de IA o herramientas dentro de un mismo proyecto.

Ejemplos de plataformas:

- Codex
- Claude
- herramientas visuales
- asistentes de investigacion
- revisores automaticos o humanos
- editores o entornos distintos

## Regla Central

Las plataformas pueden colaborar, pero no deben competir por la verdad del repo.

La colaboracion sana requiere:

- una fuente operativa principal
- un fichero compartido de coordinacion
- ownership claro
- handoff explicito
- verificacion antes de cierre
- decisiones registradas en docs del proyecto

## Modelo Por Defecto

Por defecto:

- Codex actua como ejecutor principal e integrador del repo
- Claude puede actuar como planner, reviewer, assistant o bounded-executor
- herramientas visuales aportan referencias, assets o direccion visual
- el usuario decide prioridad, alcance y cierre

Este modelo puede cambiar si el usuario lo indica, pero el ownership debe quedar explicito.

## Fichero Compartido De Coordinacion

Cuando varias plataformas colaboran, debe existir un fichero comun dentro del repo.

Nombre recomendado:

- `docs/coordination-log.md`

Alternativas validas:

- `docs/agent-coordination.md`
- `docs/work-log.md`
- `docs/handoff.md`

La regla importante no es el nombre exacto. La regla importante es que todas las plataformas sepan donde escribir y leer el estado compartido.

Este fichero sirve para:

- dejar handoff entre plataformas
- registrar quien tiene ownership de que zona
- indicar que propuestas estan aceptadas, rechazadas o pendientes
- anotar verificaciones realizadas o pendientes
- evitar depender solo del chat de una plataforma

No debe sustituir a:

- `AGENTS.md`
- progreso del proyecto
- bitacora de iteraciones
- documentacion de arquitectura

Debe ser un tablero operativo corto, no una segunda metodologia.

## Que Puede Hacer Cada Plataforma

### Codex

- leer el repo real
- editar archivos
- ejecutar verificaciones
- integrar cambios de otras fuentes
- actualizar docs de estado
- cerrar iteraciones o dejar handoff

### Claude U Otro Agente Conversacional

- explorar ideas
- ordenar planes
- revisar propuestas
- detectar riesgos
- redactar alternativas
- ejecutar tareas acotadas si tiene ownership claro

### Herramientas Visuales

- crear referencias
- explorar identidad, layouts o interacciones
- entregar assets
- ayudar a comparar direcciones

No deben imponer arquitectura, datos o reglas de negocio sin decision posterior.

### Revisores

- senalar bugs, riesgos y contradicciones
- validar coherencia con alcance
- proponer cambios

No deben mezclar review con ejecucion salvo encargo explicito.

## Flujos Recomendados

### Claude Planifica, Codex Ejecuta

1. Claude propone plan, riesgos o alternativas.
2. La propuesta aceptada se anota en el fichero compartido.
3. Usuario decide que parte entra.
4. Codex relee repo, docs, fichero compartido y zona afectada.
5. Codex implementa solo lo aceptado.
6. Codex verifica y actualiza docs.
7. Codex actualiza el fichero compartido con resultado y pendientes.

### Codex Ejecuta, Claude Revisa

1. Codex implementa una iteracion.
2. Codex resume cambios y zonas tocadas en el fichero compartido.
3. Claude revisa riesgos, claridad o UX.
4. Claude anota findings o recomendaciones en el fichero compartido.
5. Usuario decide ajustes.
6. Codex integra ajustes aceptados.
7. Codex marca que queda resuelto o pendiente.

### Herramienta Visual Inspira, Codex Aterriza

1. La herramienta visual entrega referencia o asset.
2. Se separa obligatorio de exploratorio.
3. Codex adapta a la arquitectura real.
4. Codex verifica que no se han forzado reglas tecnicas innecesarias.

### Ejecutor Acotado En Paralelo

1. El integrador asigna archivos o zona.
2. La asignacion queda escrita en el fichero compartido.
3. El bounded-executor trabaja solo ahi.
4. Deja handoff con cambios, verificacion y pendientes.
5. El integrador revisa e integra.
6. El integrador marca ownership liberado o siguiente paso.

## Anti-Conflictos

Evitar:

- dos plataformas editando la misma zona
- implementar propuestas sin decision
- mezclar varias verdades: chat, prototipo y repo
- continuar desde memoria sin revisar estado real
- cerrar sin indicar que se verifico o que queda pendiente

## Handoff Entre Plataformas

Todo relevo debe responder:

- quien entrega
- quien continua
- que rol tuvo cada plataforma
- que se decidio realmente
- que se hizo
- que archivos o zonas se tocaron
- que queda pendiente
- que se verifico
- que no debe pisarse

Si falta esa informacion, la siguiente plataforma debe resincronizar antes de editar.

## Formato Recomendado Del Fichero Compartido

```markdown
# Coordination Log

## Estado Actual

- Integrador actual:
- Iteracion activa:
- Modo: ligero / fase
- Zonas con ownership activo:

## Entradas Pendientes

- [ ] Propuesta:
  - Origen:
  - Decision:
  - Siguiente paso:

## Handoffs

### YYYY-MM-DD - Origen -> Destino

Rol/ownership:
- ...

Hecho:
- ...

Archivos o zonas:
- ...

Verificacion:
- ...

Pendiente:
- ...

No pisar:
- ...

## Decisiones Recientes

- ...
```

Reglas de uso:

- escribir entradas cortas
- actualizar ownership antes de editar
- marcar resuelto o pendiente al terminar
- no duplicar toda la bitacora
- no guardar secretos ni credenciales

## Como Llevarlo A `AGENTS.md`

Cada proyecto debe traducir este modelo a reglas concretas:

- quien es ejecutor principal por defecto
- que plataformas auxiliares se permiten
- que puede hacer cada una
- como se asigna ownership
- que formato de handoff se exige
- quien integra cambios paralelos
- cual es el fichero compartido de coordinacion

`AGENTS.md` no necesita explicar toda la teoria. Debe dejar las reglas operativas suficientes para que varias plataformas trabajen juntas sin pisarse.
