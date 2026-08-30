# Project Kickoff

Este documento define como usar la metodologia reusable al arrancar un proyecto nuevo.

La idea central:

- la metodologia se usa para configurar el proyecto
- `AGENTS.md` se usa para operar el proyecto
- los docs vivos del repo guardan estado, direccion y bitacora

## Antes De Empezar Desarrollo Tecnico

Antes de escribir codigo o abrir ramas de trabajo, definir:

- objetivo del producto o sistema
- stack y tecnologia principal
- arquitectura o limites de capas
- restricciones importantes
- comandos de verificacion
- roadmap o primeras iteraciones
- reglas especiales del repo

Sin esos datos, la metodologia solo puede dar disciplina general, no reglas operativas reales.

## Pasada Inicial

Una vez definido lo tecnico, hacer una pasada por `docs/methodology/project-methodology/` para extraer reglas de trabajo.

Esa pasada debe producir documentos concretos del proyecto:

- `AGENTS.md`
- `README.md`
- plan o roadmap
- progreso actual
- bitacora de iteraciones
- fichero compartido de coordinacion entre plataformas
- arquitectura o notas tecnicas si hacen falta

## Despues Del Kickoff

Tras crear esos documentos, el trabajo diario no debe depender de releer toda la metodologia general.

El orden normal pasa a ser:

1. leer `AGENTS.md`
2. revisar README, progreso, plan y bitacora del proyecto
3. abrir el area concreta a tocar
4. trabajar en modo ligero o modo fase segun riesgo
5. verificar y cerrar con docs del proyecto

## Rol De `AGENTS.md`

`AGENTS.md` es un documento compilado.

Debe mezclar:

- reglas metodologicas reusables que afectan a la ejecucion diaria
- reglas concretas del repo: stack, carpetas, arquitectura, comandos y restricciones

No debe copiar toda la metodologia general.

Debe contener solo lo que un agente necesita obedecer mientras trabaja en ese repo.

## Cuando Volver A La Metodologia General

Volver a `docs/methodology/project-methodology/` solo cuando:

- se esta creando o reajustando el sistema operativo del proyecto
- aparece una regla clara, repetible y reusable entre proyectos
- hay conflicto entre agentes, handoff o documentacion que requiere mejorar la base reusable
- se quiere generar otro `AGENTS.md` para un proyecto nuevo

No volver por rutina durante cada iteracion.

## Resultado Esperado

Un buen kickoff deja al proyecto listo para trabajar con:

- normas de ejecucion claras
- comandos reales de verificacion
- ownership y handoff definidos
- ramas o iteraciones pequenas
- docs vivos preparados para progreso y bitacora
- metodologia general estable y fuera del ruido diario

## Checklist De Kickoff

Antes de empezar desarrollo tecnico:

- objetivo del proyecto definido
- stack definido
- arquitectura base o limites de capas definidos
- restricciones importantes escritas
- comandos de verificacion reales escritos
- roadmap o primeras iteraciones creadas
- `AGENTS.md` generado
- README inicial creado
- progreso inicializado
- bitacora inicializada
- fichero compartido de coordinacion creado o definido
- regla de ramas o iteraciones definida
- mapa de plataformas definido: quien ejecuta, quien revisa, quien planifica y quien integra
- handoff definido para cambios de agente, herramienta o sesion
