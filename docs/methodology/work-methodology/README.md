# Work Methodology

Esta carpeta deja de ser la fuente canonica de metodologia general.

Ahora el starter pack reusable vive en:

- `docs/methodology/project-methodology/README.md`

## Para Que Queda Esta Carpeta

Como apoyo del repo actual:

- aprendizajes historicos
- notas de arquitectura nacidas en este proyecto
- patrones tecnicos observados durante la ejecucion real

## Regla Practica

- si buscas metodologia portable entre proyectos -> ir a `docs/methodology/project-methodology/`
- si buscas aprendizajes concretos de este repo -> usar `docs/methodology/work-methodology/`

## Aprendizajes Del Repo

- En Windows, KSP/Hilt puede dejar archivos de `app/build/generated/ksp/` bloqueados por procesos Java/Kotlin tras builds largos o fallidos. Si aparece `NoSuchFileException`, `FileAlreadyExistsException` o `FileNotFoundException` sobre salidas generadas, parar daemons/procesos Java bloqueantes y repetir con `.\gradlew.bat clean test --no-daemon --console=plain` antes de relanzar `build`.

### Aprendizajes de la auditoria ronda 3 (2026-08-31)

**Todo hallazgo accionable va en la tabla numerada, no en prosa destacada.** El bug que
origino la auditoria (variantes duplicadas) estaba explicado en una seccion destacada del
documento, pero **no** en la tabla P0. La spec para Codex se escribio desde las tablas, y el
hallazgo se quedo fuera: se implementaron los 9 puntos de P0 y el bug reportado por el usuario
seguia intacto. La prosa es para explicar el porque; lo que hay que hacer va numerado.

**La revision cruzada encuentra lo que una sola pasada no ve.** Tres capas, cada una encontro
algo que la anterior habia dado por bueno:

- Los agentes de auditoria encontraron ~250 hallazgos leyendo codigo.
- Codex, revisando la implementacion de P4, vio que el nuevo estado de error de Datos compartia
  campo con el snackbar y desaparecia con el. Al tirar de ese hilo aparecio algo mayor: el flujo
  usaba `catch`, que **termina el upstream**, asi que la pestaña se congelaba tras un solo fallo.
- La pasada manual del usuario encontro que bloquear el cambio de variante cuando ya habia
  series registradas era la regla equivocada, aunque estuviera bien implementada.

**Un bloqueo correctamente implementado sigue siendo un bloqueo equivocado si impide el caso en
el que mas falta hace.** Al arreglar el bug de variantes se hizo honesto el pre-chequeo, pero no
se cuestiono la regla que protegia. El caso real (dos maquinas del mismo movimiento con placas
distintas, registras en la equivocada) es justo cuando necesitas cambiar de variante.

**Verificar antes de dar por cerrado, incluido lo propio.** Al revisar la integracion de P0/P1
aparecio que el "leak" del bloqueador de navegacion descrito en la auditoria no era observable:
`requestNavigation` ya se protegia. Y la propuesta de P3-2 (mover el boton del menu a la
izquierda) se probo y se revirtio, porque ahi va el titulo de todas las pantallas. Una auditoria
tambien se equivoca.

**`catch` en Flow termina el upstream.** Aparecio tres veces en el proyecto (Home, Datos y los
flujos de heatmap/pasos). Si lo que se quiere es "no morir ante un fallo transitorio", es
`retryWhen`, no `catch`.

**Detekt como señal, no como obstaculo.** Durante esta ronda salto cinco veces por complejidad
ciclomatica y listas de parametros. En todos los casos la solucion fue extraer una funcion, y en
todos el codigo quedo mejor. Solo una vez se penso en `@Suppress`; se descarto y se inlineo el
mapeo, que era el arreglo real.

**Antes de borrar una rama, mirar QUE tiene.** Durante la limpieza, `feature/structured-target-reps`
daba "no mergeada" y parecia basura vieja de julio. Tenia un `fix` de trim que nunca llego a
`develop` y seguia siendo relevante. Un cherry-pick cambia el SHA, asi que
`merge-base --is-ancestor` dira "no mergeada" aunque el contenido este: `git cherry` lo confirma.

**Orden de los logs de progreso.** `docs/progress/phase-log.md` es cronologico **ascendente**
(lo nuevo al final). `docs/progress/project-progress.md` es al reves: lo nuevo arriba, justo tras
el encabezado.
