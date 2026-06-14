# Spec: Integración de pasos con Health Connect

**Fecha:** 2026-06-12
**Rama:** `codex/health-connect-steps`
**Estado:** aprobada

---

## Objetivo

Leer el recuento de pasos diarios desde Health Connect (fuente de datos: Samsung Health,
Garmin, Google Fit, o cualquier app compatible) y mostrarlo en la app de forma sencilla:
progreso diario vs objetivo, y estado en el calendario semanal del inicio.

No se construye ningún podómetro propio. Solo se lee lo que ya existe en el dispositivo.

---

## Requisitos técnicos

- **Dependencia:** `androidx.health.connect:connect-client` (versión estable más reciente)
- **Permiso:** `android.permission.health.READ_STEPS` declarado en `AndroidManifest.xml`
- **Compatibilidad:** Health Connect requiere Android 9+. En Android 14+ está integrado
  en el sistema. En versiones anteriores el usuario necesita instalar la app de Health Connect.
- **Sin contador propio:** si el usuario no tiene ninguna app que escriba pasos en
  Health Connect, simplemente no se muestran datos (no hay fallback al sensor del móvil)

---

## Flujo de conexión

1. En Ajustes aparece una nueva sección "Salud y actividad"
2. Muestra un botón "Conectar con Health Connect"
3. Al pulsarlo, se lanza el flow de permisos de Health Connect
4. Si el permiso se concede: se guarda el estado en DataStore y se empiezan a leer pasos
5. Si Health Connect no está instalado: mostrar mensaje "Instala Health Connect desde
   Play Store para conectar tu app de actividad" con botón que abre Play Store
6. En Ajustes también aparece el objetivo de pasos diarios (editable, por defecto 10.000)
   y un botón "Desconectar" cuando ya está conectado

---

## Datos a leer

- **Pasos de hoy:** total acumulado del día actual
- **Pasos de cada día de la semana actual:** para el calendario del inicio

Se lee al arrancar la app y se refresca cada vez que la pantalla de inicio gana foco.
No hace falta lectura en tiempo real.

---

## Pantalla de inicio — widget de pasos

Debajo del calendario semanal, añadir una fila de progreso compacta (solo visible si
Health Connect está conectado):

```
👟  8.200 / 10.000 pasos   [====------]  82%
```

- Barra de progreso lineal
- Cuando se alcanza el objetivo: checkmark dorado + texto "Objetivo completado"
- Si no hay datos del día: "Sin datos de actividad hoy"

---

## Calendario semanal — indicadores combinados

Cada celda del calendario puede tener dos estados combinados:

| Entrenó | Pasos completados | Visual |
|---|---|---|
| No | No | Gris (actual) |
| Sí | No | Verde/primary (actual) |
| No | Sí | Azul accent |
| Sí | Sí | Dorado (`accentWarm`) |

El indicador dorado es el más especial — significa día completo: entreno + objetivo de pasos.

Para implementarlo, `HomeUiState` necesita `stepsDaysCompleted: Set<Int>` (mismos índices
0–6 que `trainedDaysThisWeek`) además de los datos de pasos del día actual.

---

## Pantalla de estadísticas — tarjeta de actividad

Añadir una tarjeta nueva "Actividad esta semana" (visible solo si conectado):

- Total de pasos de la semana
- Días en que se completó el objetivo (ej: "4 de 7 días")
- Nada más — simple y limpio

---

## Fuera de alcance

- No se muestran calorías, distancia ni ningún otro dato de Health Connect
- No se integra con el historial de entrenamientos
- No se implementa el sensor de pasos del móvil como fallback
- No hay gráficas históricas de pasos en esta iteración

---

## Criterios de aceptación

- [ ] En Ajustes aparece "Conectar con Health Connect" cuando no está conectado
- [ ] Al conectar se piden los permisos correctos y se guarda el estado
- [ ] En inicio se muestra la barra de progreso de pasos del día
- [ ] Al completar el objetivo aparece checkmark dorado
- [ ] Las celdas del calendario reflejan el estado combinado (entreno + pasos)
- [ ] La tarjeta de stats muestra el resumen semanal de pasos
- [ ] Si no hay conexión o datos, los elementos no aparecen (no hay estados rotos)
