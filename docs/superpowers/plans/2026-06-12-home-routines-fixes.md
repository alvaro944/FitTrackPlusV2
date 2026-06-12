# Plan: Home Calendar Fix + Routines Order

> **Para Codex:** Lee la spec antes de empezar. Crea la rama desde main. No lances el emulador — la verificación manual la hace el usuario.

**Rama:** `codex/home-routines-fixes` (crear desde main)
**Spec:** `docs/superpowers/specs/2026-06-12-home-calendar-and-routines-order.md`

---

## Tarea 1: Bug calendario — días reales de entreno

**Archivos:**
- `feature/home/HomeViewModel.kt`
- `feature/home/HomeScreen.kt`
- Test: `app/src/test/.../feature/home/HomeViewModelTest.kt` (crear si no existe)

- [ ] Añadir `trainedDaysThisWeek: Set<Int>` a `HomeUiState` (0=lunes … 6=domingo)
- [ ] En `HomeViewModel`, calcular los días reales: para cada sesión `>= weekStart`, obtener `Calendar.DAY_OF_WEEK`, convertir al índice 0–6 con lunes=0 y añadir al Set
- [ ] `sessionsThisWeek` puede derivarse de `trainedDaysThisWeek.size`
- [ ] En `HomeScreen`, cambiar `val isCompleted = index < completedSlots` por `val isCompleted = index in trainedDaysThisWeek`
- [ ] Escribir test: sesión con timestamp de un miércoles → `trainedDaysThisWeek` contiene 2
- [ ] Hacer pasar el test
- [ ] Verificar con `test` + `build`

---

## Tarea 2: Reordenar secciones en RoutinesScreen

**Archivos:**
- `feature/routines/RoutinesScreen.kt`

- [ ] En el LazyColumn del contenido de rutinas, mover el bloque de "Biblioteca" (~línea 391) para que aparezca antes del bloque de "Plantillas" (~línea 377)
- [ ] No cambiar la lógica, solo el orden de los composables
- [ ] Verificar con `test` + `build`

---

## Tarea 3: Commit

- [ ] `./gradlew test --no-daemon --console=plain` — todos en verde
- [ ] `./gradlew build --no-daemon --console=plain` — build limpio
- [ ] Commit limpio en `codex/home-routines-fixes`
- [ ] Push y avisar al usuario
