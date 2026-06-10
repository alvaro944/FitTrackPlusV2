# Spec: Recomendaciones de carga por ejercicio

**Fecha:** 2026-06-10
**Rama:** `codex/ux-improvements`
**Estado:** aprobada

---

## Problema

El usuario no tiene ninguna señal en la app sobre cuando subir o bajar peso en un ejercicio. Tiene que recordarlo manualmente o revisarlo en el historial. Una pista sutil al lado del ejercicio antes de empezar las series ahorraría esa decision.

---

## Comportamiento objetivo

### Algoritmo de evaluacion

Para cada ejercicio del entreno activo, analizar las ultimas **3 sesiones** en las que ese ejercicio aparece (por `variantKey`).

**Rango objetivo** extraido del campo `targetRepsText`. Ejemplos de parseo:

| targetRepsText | min | max |
|---|---|---|
| `8-12` | 8 | 12 |
| `10` | 10 | 10 |
| `Al fallo` | ignorar — no hay rango evaluable |
| Texto libre sin numeros | ignorar |

**Condicion de subir peso** — se muestra 🔺 cuando:
- En al menos 2 de las ultimas 3 sesiones, la media de reps por serie supera el limite **superior** del rango

**Condicion de bajar peso** — se muestra 🔻 cuando:
- En al menos 2 de las ultimas 3 sesiones, la media de reps por serie no llega al limite **inferior** del rango

Si no se cumplen ninguna de las dos condiciones, no se muestra ninguna pista.
Si hay menos de 2 sesiones con datos de ese ejercicio, no se muestra ninguna pista.

### Presentacion visual

- Un icono pequeno (flecha arriba / flecha abajo) junto al nombre del ejercicio en la pantalla de entreno activo
- Color verde para subir, color ambar para bajar
- Sin texto adicional — solo el icono
- Al pulsarlo, aparece un tooltip breve: "Has superado el rango las ultimas sesiones. Considera subir peso." / "No has alcanzado el rango las ultimas sesiones. Considera bajar peso."
- El icono desaparece una vez que el usuario completa la primera serie del ejercicio

### Donde se muestra

- En `WorkoutScreen`, dentro de `ExerciseBlock`, junto al nombre del ejercicio
- Solo durante sesion activa (no en preview ni en historial)

---

## Implementacion tecnica

- La logica de evaluacion va en un nuevo use case: `GetProgressionHintUseCase`
- Recibe: `variantKey`, `targetRepsText`, y el repositorio de historial
- Devuelve: `ProgressionHint` (UP, DOWN, NONE)
- El ViewModel expone el hint por ejercicio como parte del estado del entreno
- El parsing del `targetRepsText` es defensivo: si no se puede parsear, devuelve NONE

---

## Fuera de alcance

- No se muestran graficas ni historial detallado en esta iteracion
- No se modifica el peso automaticamente
- No se genera notificacion push ni alerta fuera de la sesion activa

---

## Criterios de aceptacion

- [ ] Si el ejercicio supero el rango alto en 2 de las 3 ultimas sesiones, se muestra flecha arriba
- [ ] Si el ejercicio no llego al rango bajo en 2 de las 3 ultimas sesiones, se muestra flecha abajo
- [ ] Si hay menos de 2 sesiones, no se muestra nada
- [ ] Si el `targetRepsText` no es parseable, no se muestra nada
- [ ] El icono desaparece al completar la primera serie del ejercicio
- [ ] Pulsar el icono muestra el tooltip explicativo
