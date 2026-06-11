# Spec: Pulido de pantalla de entreno activo

**Fecha:** 2026-06-11
**Rama:** `codex/workout-polish`
**Estado:** aprobada

---

## 1. Scroll con teclado abierto

**Problema:** Al tener el teclado visible, el contenido de la pantalla queda por debajo del teclado y no se puede desplazar. El usuario tiene que cerrar el teclado para ver el último ejercicio.

**Solución:**
- Añadir `Modifier.imePadding()` al contenedor de scroll principal de `WorkoutScreen` (el `LazyColumn` o `Column` que contiene los ejercicios)
- Esto hace que el scroll reserve espacio igual a la altura del teclado visible, permitiendo desplazarse hasta el último ejercicio con el teclado abierto
- Verificar también que el `Scaffold` tiene `contentWindowInsets = WindowInsets(0)` para que `imePadding` tenga efecto completo

**Criterio de aceptación:**
- [ ] Con 7+ ejercicios y teclado abierto, se puede hacer scroll hasta ver el último ejercicio sin cerrar el teclado

---

## 2. Teclado numérico en campos de peso y reps

**Problema:** Al tocar los campos de peso o repeticiones se abre el teclado de texto completo en lugar del numérico.

**Solución:**
- Campo de **peso**: `KeyboardType.Decimal`
- Campo de **reps**: `KeyboardType.Number`
- Aplicar en todos los `TextField` / `BasicTextField` de serie en `WorkoutScreen`

**Criterio de aceptación:**
- [ ] Tocar el campo de peso abre teclado decimal
- [ ] Tocar el campo de reps abre teclado numérico sin decimales

---

## 3. Separador decimal: coma, no punto

**Problema:** El usuario prefiere coma como separador decimal (estándar español). Si escribe un punto hay que convertirlo automáticamente, sin mostrar error.

**Solución:**
- En el `onValueChange` del campo de peso:
  - Reemplazar `.` por `,` en el texto mostrado al usuario
  - Para parsear el valor a `Double`: reemplazar `,` por `.` antes de llamar `toDoubleOrNull()`
- No mostrar ningún mensaje de error si el formato no es perfecto — ignorar silenciosamente los caracteres no válidos
- Máximo un separador decimal permitido (ignorar segundas comas)

**Criterio de aceptación:**
- [ ] Escribir `82.5` muestra `82,5` y se interpreta como 82.5 kg
- [ ] Escribir `82,5` funciona directamente
- [ ] Escribir texto no numérico no muestra error, simplemente no se acepta

---

## 4. Alineación horizontal de columnas en fila de serie

**Problema:** Las columnas de peso (kg) y repeticiones no están alineadas horizontalmente entre filas, lo que produce un aspecto visual desalineado.

**Solución:**
- Usar anchos fijos o `weight()` consistentes para cada columna en `SetRow`
- La cabecera de columnas (si existe) debe usar los mismos anchos que las filas de datos
- Estructura objetivo por fila:

```
[ # ] [ −  PESO kg  + ] [ −  REPS  + ] [ ✓ ]
```

Cada columna con ancho fijo definido como constante en el composable para que todas las filas queden alineadas.

**Criterio de aceptación:**
- [ ] Los valores de peso de todas las series quedan alineados verticalmente
- [ ] Los valores de reps de todas las series quedan alineados verticalmente
- [ ] La etiqueta "máximo anterior" queda alineada bajo su columna correspondiente

---

## 5. Ejercicios colapsables en entreno activo

**Problema:** Con 7-8 ejercicios todos abiertos la pantalla es muy larga. No hay forma de ver el estado global del entreno de un vistazo.

**Comportamiento objetivo:**

- Al entrar en la sesión, **todos los ejercicios están colapsados** excepto el primero (o el primero con series pendientes)
- Tocar la cabecera de un ejercicio lo **expande**; tocarla de nuevo lo **colapsa**
- Cuando el usuario completa **todas las series** de un ejercicio, ese ejercicio se **colapsa automáticamente** tras un breve delay (~600ms) para que el usuario vea el check antes de que se cierre
- La cabecera del ejercicio colapsado muestra:
  - Nombre del ejercicio
  - Indicador de progreso: `2/3 series` o checkmark si está completo
  - Color/tono diferente si está completado (igual que ya se hace con las series)
- Solo un ejercicio expandido a la vez (accordion) — al expandir uno se colapsa el anterior

**Criterio de aceptación:**
- [ ] Al iniciar sesión, solo el primer ejercicio está expandido
- [ ] Tocar cabecera expande/colapsa
- [ ] Completar todas las series colapsa el ejercicio automáticamente tras ~600ms
- [ ] La cabecera muestra progreso `X/Y series` cuando está colapsado
- [ ] La cabecera muestra checkmark cuando el ejercicio está completado
- [ ] Al expandir un ejercicio, el anterior se colapsa
