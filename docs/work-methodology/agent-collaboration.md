# Trabajo Con El Agente

Esta guia recoge como colaborar con la IA sin perder control del proyecto ni del aprendizaje.

## Trabajo Multiagente

Cuando intervienen varias herramientas o agentes, hace falta una estructura simple para no pisar trabajo:

- `Codex` actua como agente principal de ejecucion dentro del repo.
- `Claude` u otras herramientas pueden proponer diseno, revisar, detectar mejoras o dejar notas en sus propios ficheros de trabajo.
- Una propuesta detectada por otro agente no se considera decision tomada hasta que el usuario la valida.
- Antes de implementar, el agente principal debe revisar el estado real del repo y contrastar lo propuesto con el codigo y la documentacion viva.
- Si existe `CLAUDE.md` u otro fichero operativo equivalente, se trata como documento de coordinacion y se lee junto con la documentacion base.

La regla practica es sencilla: una sola fuente de ejecucion a la vez, varias fuentes de analisis si ayudan.

## Dos Editores O Plataformas

Si se trabaja con dos plataformas o editores distintos sobre el mismo repo, conviene fijar estas reglas:

- solo una plataforma edita codigo compartido en cada iteracion
- la otra plataforma puede analizar, proponer, disenar o escribir notas en sus propios archivos de trabajo
- antes de retomar desde otra plataforma, revisar `git status`, leer la documentacion viva y volver a abrir el area concreta tocada
- si una plataforma ha dejado backlog o notas, tratarlas como contexto de entrada, no como instrucciones de ejecucion automatica
- si hay cambios locales no revisados de otra plataforma, no continuar a ciegas: primero entenderlos o preguntar

Esto reduce el riesgo de solape, de reversiones accidentales y de tomar decisiones sobre un estado desactualizado.

## Regla Clave: No Implementar Sin Permiso Explicito

El proyecto se disena con varias herramientas en paralelo (visual por otro lado, IA para logica, etc.). Para evitar que una herramienta pise el trabajo de otra:

- El agente **no escribe ni modifica codigo** hasta que el usuario lo pida de forma explicita.
- Planificar, explorar, leer, proponer mejoras, revisar docs y discutir enfoques **si** esta permitido sin permiso adicional.
- Editar archivos de documentacion **solo** cuando el usuario lo pida.
- Crear ramas, hacer commits, ejecutar builds o tests **solo** cuando el usuario lo pida.
- Si hay duda, preguntar antes de actuar.

Esta regla tiene prioridad sobre cualquier instruccion implicita que sugiera avanzar rapido. Planificamos y revisamos juntos; la ejecucion se dispara solo con luz verde del usuario.

## Fuente De Verdad

Para coordinar varios agentes sin deriva:

- el estado actual de producto vive en `docs/project-progress.md` y `docs/phase-log.md`
- la forma de trabajar vive en `docs/work-methodology/`
- las propuestas externas pueden vivir en archivos como `docs/mejoras-claude.md`, pero siguen siendo backlog hasta validacion
- el agente principal no debe sobrescribir notas de otros agentes sin revisar antes que parte es descriptiva y que parte es normativa
- el relevo entre plataformas debe apoyarse en estado real del repo, no solo en memoria conversacional

## Antes De Tocar Codigo

El agente debe leer:

- documentacion base del proyecto
- documentos de coordinacion entre agentes cuando existan (`CLAUDE.md`, backlog externo, notas de diseno)
- estado de Git
- area concreta que se va a modificar
- y si hay relevo entre plataformas, tambien los archivos cambiados por la iteracion anterior

Esto evita cambios a ciegas y ayuda a respetar el estilo existente.

## Durante La Implementacion

La forma de trabajo deseada:

- cambios pequenos
- explicacion corta antes de editar
- verificar con compilador pronto
- no mezclar refactors con features
- dejar claro lo que se decide y por que
- separar propuesta, decision e implementacion en la conversacion y en la documentacion
- si otra herramienta ya dejo una propuesta de mejora, escoger solo las que encajan con el alcance real de la fase
- evitar que dos plataformas editen la misma zona del repo sin una nueva lectura intermedia del estado real

## Al Encontrar Problemas

Registrar problemas reales es parte del aprendizaje.

Ejemplos de Fase 1:

- permisos del sandbox al crear ramas
- Gradle necesitando acceso fuera del sandbox para usar `.gradle`
- build con daemon quedandose sin salida
- error de smart cast en Kotlin por estado delegado

Ejemplos de Fase 2:

- una ejecucion de `test` quedo atascada y dejo archivos de resultados bloqueados
- se paro Gradle con `.\gradlew.bat --stop` y se repitio la verificacion
- algunos tests fallaron por comparar `Int` con `Long`; las expectations deben usar el tipo real del modelo

Ejemplos de Fase 3:

- `.\gradlew.bat test` volvio a quedarse sin salida hasta timeout
- repetir con `--no-daemon --console=plain` dio una verificacion estable
- conviene registrar en docs cuando una prueba manual queda bloqueada por falta de `adb`

Lo importante no es solo arreglarlo, sino guardar el patron para reconocerlo despues.

## Al Cerrar Fase

El agente debe comentar:

- que se implemento
- que archivos principales cambiaron
- que comandos pasaron
- que quedo pendiente
- que documentacion se actualizo

Ademas, debe revisar esta carpeta y anadir aprendizajes nuevos.
