# Auditoria ronda 4 — lo que hay debajo de la UI

Fecha: 2026-08-31
Base: `develop` en `2b946f0` (18.649 lineas de produccion, 4.172 de test)
Rondas anteriores: `docs/design/auditoria-ronda-3.md`

Las rondas 1 y 2 unificaron componentes. La 3 audito comportamiento de UI. Esta mira lo que
ninguna de las tres podia ver: arquitectura, capa de datos, tests, rendimiento y deriva.

Metodo: cinco barridos en paralelo. Los hallazgos marcados **verificado** los he comprobado yo
leyendo el codigo, no solo el informe del agente.

---

## Lo primero: el invariante de snapshots aguanta

Verificado clausula por clausula contra `CLAUDE.md`. Despues de ~200 commits, **ninguna ruta de
lectura de Historial o Datos toca las tablas de rutinas vivas**.

| Clausula | Estado |
|---|---|
| Las rutinas son editables | OK |
| El historial guarda snapshots al iniciar sesion | OK, y completo (incluye min/max estructurados y la variante elegida) |
| El detalle lee snapshots, nunca rutinas vivas | OK, verificado en las tres rutas de lectura |
| Los stats salen de sesiones terminadas mas snapshots | OK, con doble filtro |
| Las sesiones abiertas no aparecen en Historial ni Datos | OK, con guardas redundantes en las cinco consultas |
| Los stats agrupan por nombre normalizado del snapshot | **NO** — ver R1-6 |

Lo que lo sostiene, y **no hay que "arreglar" nunca**: no existe ninguna foreign key entre el
arbol de rutinas y el de entrenamientos. Por eso el borrado en cascada de un dia no puede
alcanzar el historial. Es diseno deliberado, no casualidad.

Las migraciones tambien estan limpias: las cinco son aditivas, ninguna borra ni recrea tablas,
no hay `fallbackToDestructiveMigration`, y los seis esquemas estan exportados y cuadran con las
entidades.

---

## R0 — Corrupcion de datos y crashes

| # | Hallazgo | Referencias |
|---|---|---|
| R0-1 | **Las tres escrituras de serie no son atomicas.** `updateSet`, `updateSetCompletion` y `updateSetNotes` hacen `getSet` → `copy` → `updateSet` sin transaccion, y `@Update` escribe la fila entera. `persistSet` lanza una corrutina por tecla, `updateSetNotes` otra, y `completeSet` hace dos escrituras separadas. Escribe una nota y completa inmediatamente: la ultima escritura restaura `isCompleted = false` y el peso previo. Esa fila es la que va al historial. **Verificado** | `DefaultWorkoutRepository.kt:157-176` |
| R0-2 | **Un refresco de BD se lleva lo que estas escribiendo.** Escribes `82,5`, la corrutina de guardado va en camino, abres el dialogo de variantes → `refreshActiveSessionFromRepository` lee la fila anterior y reemplaza la sesion entera. El valor desaparece. **`observeActiveSession()` esta declarado en el repositorio y no lo usa ningun ViewModel**: el Flow observable que ordenaria las lecturas y eliminaria toda esta clase de fallo existe y esta sin usar. **Verificado** | `WorkoutViewModel.kt:788-804`, `WorkoutRepository.kt:14` |
| R0-3 | **`deleteSession` no tiene `runCatching`.** Operacion destructiva sin proteccion: una excepcion tumba la app. Hay ocho corrutinas mas igual en Routines, Stats y AppShell, incluidas cinco escrituras en `RoutinesViewModel` en una clase cuyo `saveEditor` si esta protegido | `HistoryViewModel.kt:238`, `RoutinesViewModel.kt:102,393,399,412,418` |
| R0-4 | **Siete `catch` terminan el upstream.** Un fallo transitorio y la pestana no vuelve a actualizarse en toda la vida del ViewModel. Dos de ellos son `catch { }` vacios: el heatmap y los pasos dejan de actualizarse sin error, sin log y sin reintento. **La solucion ya existe en el repo**: `keepHomeSourceAlive` en `HomeViewModel`, con nombre, comentario y test | `WorkoutViewModel.kt:98,132,145`, `RoutinesViewModel.kt:71`, `HistoryViewModel.kt:147`, `StatsViewModel.kt:119,150` |
| R0-5 | **Duplicar un ejercicio reutiliza su `variantKey`.** `duplicateExercise` copia todo salvo `draftId` y nombre. Si el ejercicio tiene alternativas, viola el indice unico global sobre `variantKey` y **falla el guardado de la rutina entera** dentro de la transaccion. Si no las tiene, guarda y dos ejercicios distintos comparten clave: Datos los funde en una sola serie de progreso. **Verificado** | `RoutineTemplates.kt:192-200`, `RoutineExerciseAlternativeEntity.kt:21` |
| R0-6 | **`confirmSaveChanges` sale del modo edicion antes de que las escrituras terminen.** Lanza una corrutina por serie editada y llama a `finalizeEditExit` inmediatamente, borrando el buffer de deshacer. Si falla la tercera de cinco, el usuario ya salio, el buffer no existe y el mensaje de error probablemente lo borre R1-2 | `HistoryViewModel.kt:265-275` |
| R0-7 | **Doble toque en "Empezar" puede crear dos sesiones abiertas**, violando el invariante de sesion unica del que dependen `getActiveSessionWithExercises` y `ReopenWorkoutSessionUseCase`. `isStarting` se pone dentro del `launch`, no antes. Los otros tres guardas del proyecto funcionan por accidente: dependen de que `viewModelScope` use `Dispatchers.Main.immediate` | `WorkoutViewModel.kt:330-381` |

---

## R1 — Trabajo perdido y numeros equivocados

| # | Hallazgo | Referencias |
|---|---|---|
| R1-1 | **Los PRs se borran al refrescar.** `prCount` y `prType` viven solo en memoria; `toUiState()` los reconstruye desde Room y nunca los rellena. Haces un record, cambias de variante, y todos los badges desaparecen y la pantalla final dice **0 records** | `WorkoutViewModel.kt:870,903,962-998` |
| R1-2 | **El contador de PRs se sigue inflando, por otra puerta.** P0-5 de la ronda 3 arreglo `completeSet`, que si decrementa. Pero `updateSetWeight` limpia `isCompleted` y `prType` y **nunca toca `prCount`**. Completas a 100×8 (PR de peso), editas reps a 9, vuelves a completar: el volumen 900 supera 800 → PR de volumen → contador 2. Una serie, dos records. **Verificado** | `WorkoutViewModel.kt:397-409,631-645` |
| R1-3 | **`message` se borra desde un colector de datos.** El colector del historial pone `message = null` en cada emision. Si `deleteSession` o `persistSetEdit` fallan, la escritura fallida dispara una emision que **borra el error antes de que lo veas** | `HistoryViewModel.kt:143` |
| R1-4 | **El snackbar deja el estado encallado, en siete pantallas.** `showSnackbar` suspende ~4s; si cambias de pestana antes, el efecto se cancela antes del `clearMessage()` y `message` se queda no-nulo para siempre. Vuelves y sale el mensaje viejo; uno identico posterior se traga. **Ninguna de las siete usa `try/finally`. Verificado** | las 7 pantallas con `SnackbarHost` |
| R1-5 | **Dos formateadores de peso dan numeros distintos.** Cambia a libras: `100 kg → 220.46226218`. Historial muestra `220,46226218`, Entrenar muestra `220,46`. Los dos formateadores conviven en la misma data class | `WorkoutViewModel.kt:1031` vs `HistoryViewModel.kt:764` |
| R1-6 | **La documentacion miente sobre el agrupado de Datos.** `CLAUDE.md` dice "nombre normalizado del snapshot"; el codigo agrupa por `performedVariantKey` con el nombre solo de respaldo. La regla del codigo es probablemente mejor, pero falla distinto: renombrar una **rutina o dia** parte la serie en dos, y la etiqueta mostrada es la del snapshot **mas antiguo**, asi que un ejercicio renombrado conserva su nombre viejo para siempre | `ObserveWorkoutStatsUseCase.kt:108-116,143,227-234` |
| R1-7 | **AMRAP y RPE se aceptan y no hacen nada.** El editor los valida como correctos; el parser de dominio los rechaza. Se guardan como texto con `targetRepsMin/Max = null`, asi que la sugerencia de repeticiones y el hint de progresion quedan mudos para siempre en ese ejercicio | `RoutinesViewModel.kt:635` vs `TargetRepsRange.kt:10` |
| R1-8 | **Renombrar la rutina activa deja el Historial aparentemente vacio.** El filtro por defecto usa el nombre **vivo**, y las sesiones guardan el nombre del snapshot. Cambias el nombre y el filtro apunta a algo que no existe en ninguna sesion | `HistoryViewModel.kt:99-104` |
| R1-9 | **El temporizador de descanso no esta ligado a la sesion.** Su estado en marcha vive en preferencias globales sin `sessionId`. Descarta una sesion con el timer corriendo y la siguiente hereda un temporizador con vencimiento en el pasado | `UserPreferencesRepository.kt:140-144` |
| R1-10 | **Snapshots de edicion de alternativas con clave posicional.** `alternativeEditSnapshots` se indexa por `(dayIndex, exerciseIndex, alternativeIndex)`. Reordenar o borrar invalida todas: cancelar restaura la alternativa equivocada en el sitio equivocado. Es el mismo fallo que P0-7 de la ronda 3, en otro sitio | `RoutinesViewModel.kt:34` |

---

## R2 — Arquitectura

`WorkoutViewModel` son 1.264 lineas con dos `@Suppress`. La prueba de que no es un falso positivo:
**cuatro de los seis colaboradores inyectados los usa una sola responsabilidad**.

Cinco responsabilidades con costuras concretas:

1. **Selector de alternativas** (~250 lineas, `:166-328`, `:817-835`, `:906-951`). Es la mas grande
   y la mas limpia de sacar: es un dialogo con su propio ciclo de vida y es la unica razon por la
   que `routineRepository` esta inyectado aqui.
2. **Temporizador de descanso** (~140 lineas). La logica **ya es pura y esta testeada**
   (`RestTimerState.kt`); solo falta sacar la orquestacion. Extraccion casi mecanica.
3. **Deteccion de PR y celebracion** — el estado esta en el sitio equivocado (R1-1).
4. **Edicion de series** — esto es lo que deberia quedar.
5. **Mapeo Room→UI** (`:962-998`) — deberia ser un caso de uso, como ya lo es en Historial.

Otros hallazgos estructurales:

| # | Hallazgo | Referencias |
|---|---|---|
| R2-1 | **`domain/` depende de `data/`.** Las interfaces de repositorio viven en `data/` y devuelven tipos de Room. `ObserveWorkoutStatsUseCase`, 260 lineas de la logica mas valiosa de la app, esta escrito contra formas de fila de Room: un cambio de esquema es un cambio de dominio. El primer paso barato es mover solo las interfaces | `WorkoutRepository.kt:1`, todos los use cases |
| R2-2 | **Cinco pantallas alcanzan `AppShellViewModel` casteando la Activity.** El mismo conjuro de tres lineas, cinco veces, y `requireNotNull(activity) as ViewModelStoreOwner` lanza en `@Preview` y en cualquier test de Compose: **las cinco pantallas son impreviewables** | `WorkoutScreen.kt:136`, `HistoryScreen.kt:103`, `RoutinesScreen.kt:112`, `StatsScreen.kt:99`, `HomeScreen.kt:87` |
| R2-3 | **`HistoryViewModel` importa cuatro funciones internas de `feature.workout`.** Historial no compila sin Entrenar. Son exactamente las funciones que ademas existen en dos versiones divergentes (R1-5) | `HistoryViewModel.kt:23-26` |
| R2-4 | **La regla de "finalizar sin series completadas descarta la sesion" vive en el ViewModel**, calculada desde estado de UI, y ademas se re-deriva en la pantalla para elegir el texto del dialogo. Tres sitios para la regla mas importante del flujo | `WorkoutViewModel.kt:520`, `WorkoutScreen.kt:216-223` |
| R2-5 | **Estados que expresan combinaciones imposibles.** `isLoading`/`isStarting`/`isFinishing` permiten 8 combinaciones con 3 validas. En Historial, `isEditMode = true` con `selectedDetail = null` es alcanzable, y en ese estado guardar no guarda nada sin avisar | `WorkoutUiState:839-841`, `HistoryUiState:474-475` |
| R2-6 | **Cinco contratos de error distintos** en cinco ViewModels. Solo Stats separa `error` (persistente) de `message` (transitorio), que es el modelo correcto y esta comentado explicando por que | `StatsUiState.kt:276-282` |

---

## R3 — Tests

4.172 lineas de test. El patron es nitido y muy util:

> **Todo bug cuya logica se extrajo a funcion pura tiene test. Todo bug que vive en la
> orquestacion del ViewModel, no.**

Los siete ViewModels — **3.851 lineas** — tienen cero tests. Ninguno se instancia en toda la suite.
Los seis defectos de la ronda 3 vivian todos en ViewModels; los arreglos que sacaron logica fuera
recibieron test, los que se quedaron dentro no. Por eso R1-2 sigue vivo.

De los seis bugs reales de la ronda 3, la suite habria cazado **dos**.

| # | Hallazgo | Referencias |
|---|---|---|
| R3-1 | **Tres tests leen ficheros `.kt` como texto y buscan subcadenas de firmas.** Prohiben refactorizar (renombra un parametro y fallan) y no cazan nada que el compilador no cace ya. Uno fija hasta el orden de los argumentos por defecto | `D4DesignComponentsApiTest.kt`, `ButtonsApiTest.kt`, `DialogsApiTest.kt` |
| R3-2 | **Diez fakes de `WorkoutRepository` copiados, ~350 lineas.** Escribir un test nuevo es caro, asi que solo se escriben para funciones puras que no necesitan fake. Es la causa directa del hueco de R3 | los 10 ficheros de test |
| R3-3 | **`= error("Not implemented")` en la interfaz deja que los fakes salten los metodos mas peligrosos.** Ningun fake implementa `workoutExerciseHasLoggedSets`, `replaceWorkoutExerciseVariant` ni `createExerciseAlternative` — que son exactamente el bug de las variantes | `WorkoutRepository.kt:29,39,49,50,52,53,58` |
| R3-4 | **Deriva fake/produccion en direccion permisiva.** El fake de `updateSet` no toca la completitud; produccion siempre la limpia. Por eso el test `leavesCompletionUntouchedByDefault` pasa afirmando una garantia que el sistema no da | `UpdateWorkoutSetUseCaseTest.kt:129` |
| R3-5 | **`ExportUserDataUseCase` sin tests.** Es el unico mecanismo de copia de seguridad, y no hay importacion. Un volcado truncado es irrecuperable e invisible hasta el dia que lo necesitas | `ExportUserDataUseCase.kt` |
| R3-6 | **Sin tests de migracion, y aqui si importan.** Un solo entorno de produccion — tu movil — con anos de historial irreemplazable y sin ruta de importacion. `MigrationTestHelper` ya esta declarado como dependencia y sin usar | `FitTrackPlusDatabase.kt`, `build.gradle.kts:146` |

**Sin tests de UI de Compose, y esta bien asi.** Los hallazgos visuales de la ronda 3 se vieron
mirando; ningun test los habria encontrado. P5-2 lo demuestra: se implemento, se probo a mano, se
veia peor y se revirtio por decision. Ningun test automatico produce ese resultado.

---

## R4 — Rendimiento

El compilador tiene **strong skipping** activo (Kotlin 2.1.20), asi que varias quejas de manual
son teoricas aqui. Lo que si se nota:

| # | Hallazgo | Referencias |
|---|---|---|
| R4-1 | **`StatsUiState` calcula 13 propiedades derivadas en el constructor**, incluidas tres que instancian un `Collator` y ordenan. Se recalculan en **cada `copy()`**, y `withValidFocusSelection` encadena 4-5 `copy` por llamada. Un cambio de rutina activa recalcula ~60 colecciones y ~15 ordenaciones con collator | `StatsViewModel.kt:303-361,527-548` |
| R4-2 | **Un `Collator` por cada `copy()` en Historial**, y ahi se dispara **en cada tecla** mientras editas series | `HistoryViewModel.kt:650` |
| R4-3 | **`SimpleDateFormat` por punto de grafica y por tarjeta de historial**, sin `remember`. En Historial se construye uno por item durante el scroll | `StatsScreen.kt:1218-1222`, `HistoryScreen.kt:907` |
| R4-4 | **El `Paint` y los offsets de la grafica se reconstruyen en cada frame de dibujo** | `LineChart.kt:89,96` |
| R4-5 | **El detalle de Historial compone todas las series de todos los ejercicios de golpe**, sin plegado. Una sesion de 24 series construye 24 filas con dos campos de texto cada una al entrar | `HistoryScreen.kt:869` |
| R4-6 | **El detalle de Historial carga TODO el historial terminado en memoria** para encontrar una sesion anterior, en cada toque | `GetWorkoutHistoryDetailUseCase.kt:22-24` |
| R4-7 | **N+1 en la carga de sesion: 2 consultas con join por serie.** Un dia de 6 ejercicios × 4 series son 48 consultas, sobre una columna **sin indice**, en cada carga y tras cada cambio de variante | `WorkoutViewModel.kt:707-725`, `WorkoutExerciseEntity.kt:18-22` |

**Lo que ya esta bien y no hay que tocar:** `updateWorkoutExercisesForSet` devuelve la misma
instancia para lo que no cambia, asi que una tecla recompone **una sola fila**, no la lista. Eso
es lo que de verdad se nota entrenando, y esta resuelto. Todas las listas lazy tienen `key`
estable. El confeti esta bien escrito: lee el progreso dentro del `draw`, no recompone.

---

## R5 — Deriva

| # | Hallazgo | Referencias |
|---|---|---|
| R5-1 | **Historial no registra bloqueador de navegacion** pese a tener estado de edicion sin guardar. Sales por la barra inferior y se descartan las ediciones sin avisar. **Rutinas si lo registra pero nunca lo limpia** al salir de composicion — y el KDoc que lo exige esta en el fichero de al lado | `HistoryScreen.kt:104-144`, `RoutinesScreen.kt:129-134` |
| R5-2 | **Cinco formateadores de fecha con tres locales distintos**, y cuatro formateadores de numero con tres convenciones decimales | ver informe del agente |
| R5-3 | **Tres implementaciones de select-all en `core/design`**, dos con comportamiento distinto: la de `FitTrackInputDialog` reselecciona en cada toque, asi que no puedes colocar el cursor | `SelectAllTextField.kt:76`, `Dialogs.kt:113-165`, `SetRow.kt:340-500` |
| R5-4 | **`fitTrackDeltaTone` dice ser "fuente unica de verdad" y Datos no la usa**: reimplementa el mapeo de color a mano, con un comentario que dice "alineado con Historial". Alineado por copia, no por codigo compartido | `DeltaTone.kt:21-24` vs `StatsScreen.kt:628-641` |
| R5-5 | **Codigo muerto**: `PlaceholderScaffold.kt` entero, `SkeletonListItem` (el unico esqueleto compartido, que no usa nadie), `DrawerItemKind.FutureAction` y `DrawerItem.isFuture` — cuyo test literalmente certifica que no se usa. Mas ~25 imports sin usar | ver informe |
| R5-6 | **`formatPreviousWeightLabel` esta muerto y ademas es incorrecto**: fija `"kg"` a fuego mientras la ruta viva si respeta la unidad. Un test verde afirma el comportamiento equivocado de codigo que nadie ejecuta | `WorkoutScreen.kt:1320` |
| R5-7 | **`exerciseTemplateId` apunta a una tabla que no existe.** No hay ninguna entidad `ExerciseTemplate`; apunta a `RoutineExerciseEntity.id` | `WorkoutExerciseEntity.kt:20` |
| R5-8 | **`CLAUDE.md` es falso en ~10 puntos**: dice Gradle 8.7 (es 9.7.1), dice que `gradlew` no tiene bit de ejecucion (lo tiene), describe `app/src/main/java/` y un `MainActivity.kt` en la raiz que no existen, lista mal los paquetes de `core/` y `feature/`, dice DB v2 (es v6) y "Fase 6 completa" cuando el propio HEAD dice "P6 parcial" | `CLAUDE.md`, `AGENTS.md:37`, `docs/architecture/overview.md` |
| R5-9 | **detekt no analiza los tests.** `source.setFrom("src/main/kotlin")`. Y los dos directorios de test no se ponen de acuerdo entre si: `src/test/java` y `src/androidTest/kotlin` | `build.gradle.kts:89` |

---

## Orden propuesto

| # | Rama | Contenido | Por que aqui |
|---|---|---|---|
| 1 | `fix/data-integrity` | R0 completo | Corrompe datos hoy. R0-1 y R0-2 se resuelven en gran parte usando `observeActiveSession()`, que ya existe |
| 2 | `fix/lost-work-and-numbers` | R1 completo | Trabajo perdido y numeros que mienten. R1-4 son siete `try/finally` |
| 3 | `test/viewmodel-coverage` | Un `FakeWorkoutRepository` compartido, y con el los tres tests que faltan | Sin el fake compartido, escribir tests de ViewModel es demasiado caro. Es el desbloqueo, no un extra |
| 4 | `refactor/workout-viewmodel` | Sacar el selector de alternativas y el temporizador | ~400 lineas y los dos `@Suppress` se borran, no se mueven |
| 5 | `chore/drift-cleanup` | R5 completo, empezando por `CLAUDE.md` | Barato y evita errores futuros. Un documento falso es peor que ninguno |
| — | Diferido | R2-1 (mover interfaces a `domain/`), R2-5 (estados sellados), R4 salvo R4-1/R4-2 | Estructural, una rama propia cada uno, sin prisa |

**Nota sobre R4:** solo R4-1 y R4-2 se notan de verdad (el collator por tecla en Historial y el
recalculo en cada `copy` de Datos). El resto es medible en un benchmark pero no en la mano.
