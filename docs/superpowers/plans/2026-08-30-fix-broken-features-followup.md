# Plan: Cierre de fix/broken-features (P1) — 3 huecos encontrados en revision

> **Para Codex:** Rama existente `fix/broken-features`, ya tiene los 12 commits de R1-R12 verificados (test+build en verde). Añade estos 3 commits al final, mismo criterio: verifica local (`test` + `build`) antes de cada commit, conventional commits, sin Co-Authored-By, sin commits de WIP.

**Rama:** `fix/broken-features` (existente)

---

## Tarea 13: Import sin usar en HomeViewModel

**Archivo:** `feature/home/HomeViewModel.kt:18`

- [ ] Eliminar el import `kotlinx.coroutines.flow.catch` que quedo sin usar tras el fix de R7 (el `.catch` se movio a nivel de cada flow fuente, este import de nivel de archivo ya no hace falta)
- [ ] `test` + `build` en verde
- [ ] Commit: `chore: remove unused catch import in HomeViewModel`

---

## Tarea 14: Texto del cajon lateral desactualizado para Exportar

**Archivo:** `core/design/AppShell.kt:280` (entrada de drawer "Exportar datos")

- [ ] R10 ya implemento la exportacion real (JSON via SAF), pero el texto del cajon lateral sigue diciendo "Visible ahora, implementacion futura" — actualizar el copy para reflejar que la funcion ya esta disponible
- [ ] Revisar si el item de drawer sigue marcado como `isFuture = true` / `DrawerItemKind.FutureAction` en `NavigationShellConfig.kt` y corregirlo si aplica (deberia navegar/ejecutar la exportacion, no solo mostrar el texto)
- [ ] `test` + `build` en verde
- [ ] Commit: `fix: update export data drawer copy to reflect it is available`

---

## Tarea 15: Toggle de mostrar/ocultar nota de ejercicio en Entrenar

**Archivo:** `feature/workout/WorkoutScreen.kt:860-866` (y estado del ViewModel/UI que corresponda)

**Contexto:** R2 añadio las notas de ejercicio end-to-end, pero la nota se muestra siempre que existe, sin forma de ocultarla. El dueño la usa activamente durante el entrenamiento y quiere poder ocultarla y volver a mostrarla sin perderla.

- [ ] Añadir un boton/icono junto al nombre del ejercicio para alternar mostrar/ocultar su nota (patron similar a un icon toggle ya usado en el proyecto, ej. expand/collapse)
- [ ] El estado de mostrar/ocultar es puramente de UI (no se persiste en BD ni sobrevive a cerrar la app); puede resetear a "oculto" en cada apertura de la sesion, decision libre de Codex si difiere, documentarla en el commit
- [ ] La nota debe seguir siendo editable (via el dialogo existente) independientemente de si esta oculta o visible
- [ ] `test` + `build` en verde
- [ ] Commit: `feat: add show/hide toggle for exercise notes in workout screen`

---

## Verificacion final

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] Push y avisar

**Merge:** a `develop` cuando estas 3 tareas esten verificadas, junto con las 12 anteriores (arrastra `fix/data-loss` y las rondas 1+2). A `main` solo tras la pasada manual del dueño (checklist ya entregado, mas: revisar toggle de notas y confirmar que exportar ya no dice "implementacion futura").
