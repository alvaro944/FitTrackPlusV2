# Plan: Home/Entrenar/Datos — visual, accesibilidad y locale (P4 restante)

**Ejecutor:** Claude, excepcion explicita concedida por el dueño (misma logica que
`chore/dependency-upgrade`, `refactor/input-and-nav`). Worktree aislado, no toca el directorio
de trabajo principal donde Codex ejecuta `fix/shared-accessibility-and-copy` en paralelo.

**Rama:** `refactor/home-stats-visual-locale` (desde `develop`)

**No tocar** (los edita la rama hermana de Codex sobre el mismo `develop`):
`SettingsScreen.kt`, `LaunchIntroScreen.kt`, `AppShell.kt`, `ThemeModeSelector.kt`,
`SegmentedSelector.kt`, `Stepper.kt`, `SetRow.kt`, `RoutinesScreen.kt`, `FitTrackPlusWidget.kt`
(salvo lo que ya haga Codex ahi).

---

## Tarea 1 (P4-4 parcial): Cabecera de Home unificada

**Archivo:** `feature/home/HomeScreen.kt`

- [ ] Sustituir la pila propia de texto (saludo + fecha a `displayLarge`) por `FitTrackScreenHeader`,
      igual que Historial/Rutinas/Entrenar
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: unify home header with the shared screen header pattern`

---

## Tarea 2 (P4-5): Esqueleto de lista compartido

**Archivos:** `core/design/components/Skeletons.kt`, `WorkoutScreen.kt`, `StatsScreen.kt`, `HomeScreen.kt`

- [ ] Añadir un skeleton de lista generico en `Skeletons.kt` que se parezca a filas reales
      (tarjeta + dos lineas de texto), reutilizable
- [ ] Aplicarlo donde falta: Home no tiene ninguno; su badge dice "SIN SESION" antes de que
      llegue el primer dato
- [ ] Revisar los skeletons existentes de Entrenar y Datos y ajustarlos para que se parezcan a
      lo que realmente cargan
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add shared list skeleton and use it consistently across home, workout and stats`

---

## Tarea 3 (P5-1 parcial): Rol de accesibilidad en Datos

**Archivo:** `feature/stats/StatsScreen.kt` (~linea 1017, chip de filtro/segmentado)

- [ ] Añadir `Role` adecuado al `.clickable` sin rol
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: add semantic role to stats filter clickable`

---

## Tarea 4 (P5-4 parcial): Descripcion de la tira semanal de Home

**Archivo:** `feature/home/HomeScreen.kt`

- [ ] La tira semanal (dias entrenados) no tiene `contentDescription` — añadirlo por dia,
      indicando si se entreno o no y que dia es
- [ ] (El fix de contraste de `primarySoft` en oscuro ya se hizo en la ronda anterior, no repetir)
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: add accessibility descriptions to home weekly streak strip`

---

## Tarea 5 (P5-5): Graficas accesibles

**Archivo:** `core/design/components/LineChart.kt`

- [ ] El `Canvas` no tiene `contentDescription` ni nodos por punto; la interaccion es
      `pointerInput` crudo sin soporte de accesibilidad
- [ ] Añadir como minimo una `contentDescription` que resuma la serie (ej. "Progreso de peso
      maximo, de X a Y kg en el periodo"), y evaluar si merece la pena añadir nodos semanticos
      por punto dado el tiempo disponible — si se recorta, documentarlo explicitamente en el commit
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: add accessible content description to progress line chart`

---

## Tarea 6 (P6-3 parcial): Deduplicar logica de plural en Home

**Archivo:** `feature/home/HomeScreen.kt`

- [ ] Existe una version correcta de la logica de plural y otra incorrecta duplicada ~250 lineas
      mas abajo en el mismo fichero — dejar una sola fuente de verdad
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: remove duplicated pluralization logic in home screen`

---

## Tarea 7 (P6-4 parcial): Locale unificado en Datos y Home

**Archivos:** `feature/stats/StatsScreen.kt` (~lineas 413, 1003, 1059),
`feature/home/HomeScreen.kt` (~linea 512)

- [ ] `Locale("es","ES")` a fuego conviviendo con `Locale.getDefault()` en la misma pantalla
      (una tarjeta produce "3.5k", otra "3,5") — decidir una politica unica (recomendado:
      `Locale("es", "ES")` fijo en todo punto de formato numerico/fecha visible, ya que la UI
      esta en español fijo por diseño del proyecto) y aplicarla de forma consistente
- [ ] `SimpleDateFormat` duplicado en dos ficheros y reasignado en cada composicion — extraer a
      una constante/util compartido si el esfuerzo lo justifica
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: unify locale policy for number and date formatting in home and stats`

---

## Verificacion final

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] Confirmar que no se toco ningun fichero de la lista prohibida
- [ ] Detenerse y pedir autorizacion explicita antes de push, como en las ramas anteriores

**Pendiente de pasada manual (la hace el dueño):**
1. Revisar visualmente la cabecera nueva de Home.
2. Comprobar los esqueletos de carga en Home/Entrenar/Datos con conexion lenta simulada.
3. TalkBack activado: escuchar la tira semanal de Home y la grafica de progreso.
4. Confirmar que los numeros con decimales se ven igual en Home y Datos.

**Merge:** a `develop` cuando compile, pasen los tests, y coordinando con la rama hermana de
Codex para no mergear las dos a la vez sin revisar conflictos de integracion.
