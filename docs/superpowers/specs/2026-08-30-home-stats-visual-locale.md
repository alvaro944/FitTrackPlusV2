# Spec: Home/Entrenar/Datos — visual, accesibilidad y locale (resto de P4 y P5, parcial)

**Fecha:** 2026-08-30
**Origen:** `docs/design/auditoria-ronda-3.md`, secciones P4 (items pendientes) y P5.
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-30-home-stats-visual-locale.md`
**Ejecutor:** Claude, excepcion explicita del dueño para ejecutar codigo directamente en esta rama (misma logica que `chore/dependency-upgrade` y `refactor/input-and-nav`), en worktree aislado del directorio de trabajo principal.
**Prerequisito:** `develop` al dia — ya trae P0-P4 mayormente fusionados.
**Rama hermana en paralelo:** `fix/shared-accessibility-and-copy` (ejecutada por Codex, misma base `develop`, sin ficheros compartidos con esta — ver "Aislamiento").

---

## Por que

De los 11 hallazgos de P4, quedan 3 sin resolver tras la fusion de la ronda anterior: unificacion de cabecera, esqueletos que no se parecen a lo que cargan, y la intro con paleta fija. De P5 (accesibilidad), la mayoria sigue pendiente salvo un contraste que se arreglo de rebote durante P4. Esta rama cierra la parte de esos dos frentes que vive en Home, Entrenar y Datos — sin tocar los componentes compartidos ni Ajustes, que los cierra la rama hermana en paralelo.

Verificado de forma independiente antes de escribir esta spec (leyendo el codigo actual en `develop`):
- **P4-4 (Home)**: confirmado, `HomeScreen.kt` sigue con su propia pila de texto (`displayLarge`) en vez de `FitTrackScreenHeader`.
- **P5-1 (Datos)**: confirmado, `.clickable(onClick = onSelect)` sin `Role` en `StatsScreen.kt` (~linea 1017).
- El resto de hallazgos (P4-5, P5-4, P5-5, P6-3, P6-4) se toman del audit original sin re-verificar linea por linea tras la fusion de P4; si al abrir una tarea el hallazgo ya no aplica, documentarlo en el commit en vez de forzar un cambio innecesario.

## Aislamiento entre las dos ramas

- **Esta rama NO toca**: `SettingsScreen.kt`, `LaunchIntroScreen.kt`, `AppShell.kt`, `ThemeModeSelector.kt`, `SegmentedSelector.kt`, `Stepper.kt`, `SetRow.kt`, `RoutinesScreen.kt`, `FitTrackPlusWidget.kt`.
- **La rama hermana NO toca**: `HomeScreen.kt`, `StatsScreen.kt`, `LineChart.kt`, `Skeletons.kt`, y de `WorkoutScreen.kt` solo puede tocar la linea ~1009 (una excepcion puntual, ver su spec).
- Si aparece la necesidad de tocar un fichero de la lista prohibida, parar y avisar en vez de improvisar.

## Requisitos

### R1 — Cabecera de Home unificada (P4-4 parcial)

- Sustituir la pila propia de texto de `HomeScreen.kt` por `FitTrackScreenHeader`.

### R2 — Esqueleto de lista compartido (P4-5)

- Añadir un skeleton de lista generico y reutilizable en `Skeletons.kt` que se parezca a una fila real (tarjeta + texto).
- Aplicarlo en Home (no tiene ninguno; su badge dice "SIN SESION" antes de que llegue el primer dato) y revisar los de Entrenar y Datos para que se parezcan a lo que cargan.

### R3 — Rol de accesibilidad en Datos (P5-1 parcial)

- Añadir `Role` al `.clickable` sin rol en `StatsScreen.kt` (~linea 1017).

### R4 — Descripcion de la tira semanal de Home (P5-4 parcial)

- Añadir `contentDescription` por dia en la tira semanal (dias entrenados) de `HomeScreen.kt`. El fix de contraste de `primarySoft` en oscuro ya se hizo en la ronda anterior, no repetir.

### R5 — Graficas accesibles (P5-5)

- Añadir al menos una `contentDescription` que resuma la serie en `LineChart.kt`. Evaluar nodos semanticos por punto segun tiempo disponible; si se recorta, documentarlo en el commit.

### R6 — Plurales duplicados en Home (P6-3 parcial)

- Eliminar la logica de plural incorrecta duplicada en `HomeScreen.kt`, dejando una sola fuente de verdad.

### R7 — Locale unificado en Datos y Home (P6-4 parcial)

- Decidir una politica unica de `Locale` para formato numerico/fecha visible (recomendado: `Locale("es", "ES")` fijo, ya que la UI esta en español fijo por diseño) y aplicarla de forma consistente en `StatsScreen.kt` y `HomeScreen.kt`.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. Home usa `FitTrackScreenHeader` igual que el resto de pantallas.
3. Existe un skeleton de lista compartido, aplicado en Home, y los de Entrenar/Datos se parecen a lo que cargan.
4. El chip/segmentado de Datos anuncia su rol en TalkBack.
5. La tira semanal de Home anuncia cada dia y si se entreno.
6. La grafica de progreso tiene una descripcion accesible.
7. No queda logica de plural duplicada en Home.
8. Los numeros con decimales se ven igual en Home y Datos, sin mezclar `Locale.getDefault()` y `Locale("es","ES")` en la misma pantalla.
9. Ningun fichero de la lista prohibida en "Aislamiento" fue tocado.
10. Detenerse y pedir autorizacion explicita antes de push, como en las ramas anteriores.
11. Pasada manual del dueño antes de mergear.
