# Spec: Fixes de entrada de series en entreno activo

**Fecha:** 2026-06-12
**Rama:** `codex/ux-improvements`
**Estado:** aprobada

---

## 1. Campo de reps demasiado estrecho en pantalla vertical

**Problema:** El campo de repeticiones es tan estrecho que solo muestra el primer dígito (ej: "12" aparece como "1"). Los botones +/− ocupan demasiado espacio.

**Solución:**
- Reducir el tamaño de los `SetStepperButton` de reps: icon size a 16dp, botón a 28dp de ancho mínimo
- Dar más espacio al campo de texto reduciendo el `weight` de la columna de peso o ajustando la proporción entre columnas
- El campo de reps debe mostrar al menos 2 dígitos sin truncarse
- Verificar en emulador con valores de 1, 10 y 100 reps que se ven completos

**Criterio de aceptación:**
- [ ] "12" reps se ve completo en pantalla vertical
- [ ] "100" reps se ve completo
- [ ] Los botones +/− siguen siendo pulsables cómodamente

---

## 2. Eliminar auto-colapso al completar ejercicio

**Problema:** Al completar todas las series de un ejercicio, este se colapsa automáticamente. El usuario no quiere este comportamiento. Solo quiere accordion puro: al abrir un ejercicio, el anterior se cierra.

**Solución:**
- Eliminar completamente el `exerciseAdvanceJob` y la lógica de `EXERCISE_AUTO_COLLAPSE_DELAY_MILLIS`
- Eliminar la llamada a `delay` + auto-colapso que se dispara al completar sets
- El único momento en que un ejercicio se colapsa es cuando el usuario toca la cabecera de otro ejercicio
- Un ejercicio completado (todas sus series marcadas) muestra checkmark en la cabecera cuando está colapsado, pero no se cierra solo

**Criterio de aceptación:**
- [ ] Completar todas las series de un ejercicio NO lo cierra automáticamente
- [ ] Tocar la cabecera de otro ejercicio SÍ cierra el anterior
- [ ] El ejercicio completado muestra checkmark en cabecera cuando está colapsado

---

## 3. Reps compartidas entre ejercicios (bug)

**Problema:** Al pulsar +/− en las repeticiones del ejercicio 1, también cambia el valor en el ejercicio 2. No ocurre con el peso, solo con reps. Bug de estado compartido o de ID incorrecto.

**Solución:**
- Investigar `stepSetReps` en `WorkoutViewModel` — verificar que usa el `setId` único del set, no un índice
- Verificar que el valor por defecto de reps se inicializa de forma independiente por cada set (no desde un estado compartido)
- Añadir test unitario: `stepSetReps(setId = A, delta = 1)` no modifica el set con `setId = B`
- Si el bug está en cómo Compose comparte el `remember` del campo de texto, mover el estado del texto al ViewModel en lugar de tenerlo local en el composable

**Criterio de aceptación:**
- [ ] Pulsar + en reps del ejercicio 1 no modifica reps del ejercicio 2
- [ ] Test unitario que verifica independencia de sets
- [ ] No ocurre con ninguna combinación de ejercicios

---

## 4. Seleccionar todo al tocar un campo (peso y reps)

**Problema:** Al tocar el campo de peso o reps para editar, el cursor aparece junto al número existente. El usuario tiene que borrar manualmente antes de escribir. Quiere que al tocar el campo se seleccione todo el contenido, de modo que al escribir directamente lo reemplaza.

**Solución:**
- En los `OutlinedTextField` / `BasicTextField` de peso y reps, usar `onFocusChanged` para seleccionar todo el texto al recibir foco
- Implementación con `TextFieldValue` + `selection = TextRange(0, text.length)` al detectar `hasFocus = true`
- Aplicar tanto al campo de peso (`WeightFieldColumn`) como al campo de reps
- El comportamiento de los botones +/− no cambia

**Criterio de aceptación:**
- [ ] Tocar el campo de peso selecciona todo el texto existente
- [ ] Tocar el campo de reps selecciona todo el texto existente
- [ ] Escribir un número directamente reemplaza el valor anterior sin necesidad de borrar
- [ ] Los botones +/− siguen funcionando correctamente
