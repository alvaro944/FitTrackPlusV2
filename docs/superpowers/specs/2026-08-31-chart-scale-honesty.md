# Spec: Escala honesta en las graficas de Datos (P4-8)

**Fecha:** 2026-08-31
**Origen:** `docs/design/auditoria-ronda-3.md`, seccion P4, item P4-8 (parcial).
**Plan de ejecucion:** `docs/superpowers/plans/2026-08-31-chart-scale-honesty.md`
**Rama:** `fix/chart-scale-honesty`, nueva desde `develop`.

---

## Por que

`LineChart` normaliza siempre a `minY..maxY` del conjunto de puntos:

```
val minY = minOf { it.second }
val maxY = maxOf { it.second }
val yRange = if (maxY == minY) 1f else maxY - minY
y = padTop + chartH - ((value - minY) / yRange) * chartH
```

Consecuencia: **el punto mas bajo siempre toca el borde inferior y el mas alto el superior**,
sin eje ni referencia numerica visible. Una progresion de 100 a 101 kg se dibuja exactamente
igual que una de 50 a 150. La grafica exagera cualquier variacion y no hay nada en pantalla que
lo desmienta.

Ya resuelto en la ronda anterior y **fuera del alcance de esta spec**: las etiquetas usan `sp` y
siguen el ajuste de fuente del sistema, y la grafica tiene `contentDescription`.

## Los dos usos no quieren lo mismo

Verificado en `feature/stats/StatsScreen.kt`:

| Grafica | Que pinta | Escala correcta |
|---|---|---|
| Tendencia de volumen (`:587`) | Volumen total por sesion | **Anclada a cero.** El volumen es una cantidad absoluta: el cero significa algo y la altura de la linea debe ser proporcional al valor |
| Progreso del ejercicio (`:812`) | Peso maximo, 1RM estimado, etc. | **Auto, pero con la escala visible.** Anclar a cero aplastaria la variacion util: entre 100 y 105 kg no se veria nada. Lo que falta no es el cero, es saber entre que valores se esta moviendo |

Esa diferencia es el nucleo de la spec: no es un unico arreglo, es un parametro.

## Requisitos

### R1 — Modo de linea base configurable

- `LineChart` gana un parametro de linea base con dos modos, con nombre explicito en el design
  system (no un `Boolean`): uno ancla el minimo del eje a cero, otro conserva el
  comportamiento actual de ajustar al rango de los datos.
- Por defecto se mantiene el comportamiento actual, para no cambiar en silencio ninguna grafica
  que se añada despues.
- La tendencia de volumen pasa a modo anclado a cero. El progreso del ejercicio se queda en
  automatico.

### R2 — Referencia numerica del eje Y

- En ambos modos, la grafica dibuja el valor **maximo y minimo del eje** en el borde izquierdo,
  con el mismo estilo tipografico que las etiquetas ya existentes (derivado de `labelSmall`, en
  `sp`).
- El area de dibujo se ajusta para dejar sitio a esas etiquetas: no deben solaparse con la linea
  ni recortarse.
- En modo anclado a cero, el minimo mostrado es `0`.

### R3 — Etiquetas de punto que no se pisan

- Hoy se dibuja una etiqueta encima de **cada** punto. Con 8 puntos en un movil se solapan.
- Pasar a etiquetar solo: el primero, el ultimo, el maximo, el minimo y el punto seleccionado.
- Si dos de esos coinciden en el mismo punto, se dibuja una sola vez.

### R4 — La descripcion accesible refleja la escala

- El `contentDescription` generado por defecto menciona el rango del eje, para que quien no ve la
  grafica reciba la misma informacion que R2 da visualmente.
- Las descripciones que ya pasan los llamadores desde `StatsScreen` siguen teniendo prioridad.

### R5 — Sin regresiones de lo ya cerrado

- Las etiquetas siguen en `sp` y respetando el ajuste de fuente del sistema.
- La grafica sigue teniendo `contentDescription`.
- La seleccion de punto por toque sigue funcionando igual.

## Fuera de alcance

- **Rejilla de fondo (gridlines)**: se menciono en la auditoria como opcion; no entra. Con las
  etiquetas de maximo y minimo la escala ya es legible, y una rejilla en un grafico de este
  tamaño añade ruido.
- **Semantica por punto para lector de pantalla**: el `contentDescription` de resumen cubre el
  caso principal. Exponer cada punto como nodo semantico con accion de clic es una pieza mayor y
  queda para P5 si se retoma.
- Cualquier cambio de color, tamaño o tipografia de la grafica.

## Criterios de aceptacion

1. `test` y `build` en verde.
2. La grafica de volumen arranca en cero: dos sesiones de 1000 y 1010 kg se ven casi planas, no
   como una subida a pantalla completa.
3. La grafica de progreso conserva su escala ajustada, pero muestra maximo y minimo del eje.
4. Con 8 o mas puntos, las etiquetas de valor no se solapan.
5. El texto de la grafica sigue escalando con el ajuste de fuente del sistema.
6. La descripcion accesible menciona el rango del eje.
7. Test unitario del calculo de la escala: mismo conjunto de puntos en los dos modos produce
   offsets distintos, y en modo anclado el minimo del eje es cero.
