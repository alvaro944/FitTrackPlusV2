# Spec: Integridad de datos — sesiones vacías y estadísticas

**Fecha:** 2026-06-11
**Rama:** `codex/workout-polish`
**Estado:** aprobada

---

## 1. Sesiones vacías no registradas

**Problema:** Si el usuario inicia un entrenamiento y lo finaliza sin completar ninguna serie, esa sesión aparece en el historial y puede afectar estadísticas, mostrando días con 0 volumen o 0 series.

**Comportamiento objetivo:**
- Al llamar a "Finalizar entrenamiento", comprobar si hay al menos **1 serie completada** en la sesión
- Si hay 0 series completadas: descartar la sesión silenciosamente (borrar el registro de Room) y volver al estado de preview
- Si hay al menos 1 serie completada: guardar normalmente como hasta ahora
- No mostrar pantalla de resumen si la sesión no tenía datos — simplemente volver al preview con un mensaje breve tipo "Sesión descartada (sin series registradas)"

**Dónde implementar:**
- `FinishWorkoutSessionUseCase` o en `WorkoutViewModel.finishWorkout()` antes de llamar al use case
- Añadir método en `WorkoutRepository` para eliminar una sesión activa sin guardar (o reutilizar el que exista)

**Criterio de aceptación:**
- [ ] Iniciar sesión → pulsar Finalizar sin marcar ninguna serie → no aparece en historial
- [ ] Iniciar sesión → completar 1 serie → finalizar → sí aparece en historial
- [ ] Tras descartar sesión vacía, la pantalla vuelve al estado de preview de la rutina

---

## 2. Gráficas sin huecos para días no entrenados

**Problema:** En las estadísticas, los días en que no se entrenó pueden generar puntos a cero o huecos visuales feos en la gráfica de progresión.

**Comportamiento objetivo:**
- Las gráficas de progresión (volumen, peso máximo, etc.) solo generan puntos para días con sesiones **finalizadas y con datos**
- No se generan puntos a cero para días sin entreno
- La línea conecta directamente los puntos de los días que sí tienen datos, saltando los días vacíos
- Si no hay ningún dato en el periodo seleccionado, mostrar el estado vacío actual (sin gráfica)

**Dónde implementar:**
- `ObserveWorkoutStatsUseCase` — asegurarse de que el mapeo de datos a puntos de gráfica no rellena días sin sesión
- Si hay lógica de relleno de huecos (fill zeros, continuous range), eliminarla o condicionarla

**Criterio de aceptación:**
- [ ] Un periodo con entrenos el lunes y el jueves muestra 2 puntos conectados, sin punto a cero el martes/miércoles
- [ ] Un periodo sin ningún entreno muestra el estado vacío, no una línea a cero
- [ ] Sesiones vacías (descartadas por la regla anterior) no generan puntos
