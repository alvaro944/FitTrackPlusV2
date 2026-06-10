# Spec: Mejoras UX de entrada de series en entreno activo

**Fecha:** 2026-06-10
**Rama:** `codex/ux-improvements`
**Estado:** aprobada

---

## Problema

Registrar una serie requiere abrir el teclado, escribir el numero de reps, y confirmar. En una sesion de 20+ series es repetitivo y lento. Ademas, la diferencia visual entre series completadas y pendientes es sutil.

---

## Comportamiento objetivo

### 1. Steppers +/- para reps sin teclado obligatorio

- Cada fila de serie tiene dos botones: `−` y `+` que decrementan/incrementan las reps en 1
- El campo de texto sigue disponible para quien quiera escribir directamente
- El valor inicial al empezar una serie nueva es el valor de la serie anterior del mismo ejercicio (o el minimo del rango objetivo si es la primera vez)
- Los botones `−` y `+` son suficientemente grandes para pulsarlos sin problema (minimo 40dp)
- El peso tambien tiene steppers: incremento de 2.5 kg por defecto

### 2. Feedback visual al completar una serie

- Al marcar una serie como completada, la fila cambia de estado visualmente:
  - Fondo ligeramente diferente (tono mas suave del verde de exito)
  - Checkmark visible en el lateral izquierdo
  - Tipografia del peso y reps en color mas apagado (ya registrado)
- El cambio de estado es inmediato, sin animacion larga que frene el ritmo
- Una vibracion haptica corta confirma el registro (igual que ya hace el PR)

### 3. Jerarquia visual mas clara entre ejercicios

- Misma logica que en el editor de rutinas: los ejercicios son cards diferenciados
- El nombre del ejercicio tiene mayor peso visual que los datos de series
- Las series completadas se agrupan visualmente separadas de las pendientes (o simplemente el estilo las diferencia)

### 4. Steppers para el peso

- Incremento por defecto: **2.5 kg**
- Mantener pulsado sube/baja de 5 en 5 (long-press acceleration)
- El campo de texto sigue editable para valores exactos

---

## Fuera de alcance

- No se cambia el modelo de datos de series
- No se anade registro de tiempo por serie en esta iteracion
- No se cambia el flujo del timer de descanso

---

## Criterios de aceptacion

- [ ] Pulsar `+` aumenta reps en 1 sin abrir teclado
- [ ] Pulsar `−` disminuye reps en 1 (minimo 0)
- [ ] Al completar una serie, la fila muestra checkmark y fondo diferenciado
- [ ] Hay vibracion haptica al completar
- [ ] El peso tiene steppers de 2.5 kg
- [ ] El campo de texto sigue funcionando para edicion manual
- [ ] Los botones miden al menos 40dp
