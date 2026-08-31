# Plan: Escala honesta en las graficas de Datos (P4-8)

> **Para Codex:** lee la spec entera antes de empezar. **Crea la rama desde `develop`.** Commits
> separados por tarea, conventional commits, **sin Co-Authored-By**. Verifica `test` + `build`
> antes de cada commit; NO lances el emulador. No commits de WIP.

**Rama:** `fix/chart-scale-honesty` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-08-31-chart-scale-honesty.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

Ficheros que toca, todos: `core/design/components/LineChart.kt` y `feature/stats/StatsScreen.kt`.
Es un cambio pequeño y acotado; si te ves tocando algo mas, para y avisa.

---

## Tarea 0: Preparar rama

- [ ] `git checkout develop && git pull`
- [ ] `git checkout -b fix/chart-scale-honesty`
- [ ] `test` + `build` en verde antes de tocar nada, para partir de una base sana

---

## Tarea 1: Extraer el calculo de escala y hacerlo testeable

Hoy la escala se calcula dentro del `Canvas`, mezclada con el dibujo, asi que no se puede probar.

- [ ] Sacar el calculo de `minY`/`maxY`/`yRange` a una funcion pura `internal`, fuera del
      composable, que reciba los valores y el modo de linea base y devuelva el rango del eje.
- [ ] Definir el modo como un `enum` con nombre explicito en `core/design` (dos valores: ajustar
      al rango de los datos, y anclar el minimo a cero). **No un `Boolean`**: en la llamada tiene
      que leerse que hace.
- [ ] `LineChart` gana el parametro con **valor por defecto = comportamiento actual**.
- [ ] Test unitario nuevo: mismo conjunto de puntos en los dos modos produce rangos distintos, y
      en modo anclado el minimo del eje es 0. Incluye el caso de un solo valor repetido (hoy
      `yRange` cae a `1f`; que siga sin dividir por cero).
- [ ] `test` + `build`, commit.

## Tarea 2: Etiquetas de maximo y minimo del eje (R2)

- [ ] Dibujar el valor maximo y el minimo del eje en el borde izquierdo, con el mismo `Paint` y
      tamaño ya usado para las etiquetas (derivado de `labelSmall`, en `sp` — **no lo cambies a
      dp**, eso fue un arreglo previo).
- [ ] Ajustar el padding horizontal del area de dibujo para dejarles sitio. Comprueba leyendo el
      codigo que la linea y los puntos no se solapan con ellas ni se recortan por el borde.
- [ ] `test` + `build`, commit.

## Tarea 3: Dejar de etiquetar todos los puntos (R3)

- [ ] Sustituir el dibujado de etiqueta en cada punto por: primero, ultimo, maximo, minimo y
      seleccionado.
- [ ] Si varios de esos son el mismo punto, se dibuja una sola etiqueta.
- [ ] Funcion pura `internal` que decida que indices se etiquetan, con test: con 8 puntos
      devuelve como mucho 5 indices y no repite.
- [ ] `test` + `build`, commit.

## Tarea 4: Aplicar el modo anclado solo al volumen (R1)

- [ ] En `StatsScreen.kt`, la grafica de **tendencia de volumen** pasa a modo anclado a cero.
- [ ] La de **progreso del ejercicio** se queda en automatico. No la toques.
- [ ] Comentario de una linea en la llamada explicando por que solo una de las dos: el volumen es
      una cantidad absoluta y el cero significa algo; anclar el progreso aplastaria la variacion
      util entre 100 y 105 kg.
- [ ] `test` + `build`, commit.

## Tarea 5: Descripcion accesible con el rango (R4)

- [ ] La descripcion generada por defecto menciona entre que valores va el eje.
- [ ] Las descripciones que ya pasa `StatsScreen` siguen ganando; no las sobreescribas.
- [ ] `test` + `build`, commit.

---

## Verificacion final antes de avisar

- [ ] `test` + `build` en verde
- [ ] Grep de control: `LineChart.kt` no tiene ningun `textSize` en `dp` (regresion de la ronda
      anterior)
- [ ] `LineChart.kt` sigue teniendo `contentDescription`
- [ ] La seleccion de punto por toque sigue funcionando (leelo, no hace falta emulador)

## Si algo no cuadra

Si al implementar ves que el ajuste de padding para las etiquetas del eje deja la grafica
demasiado estrecha en un movil pequeño, **para y avisa** con lo que has medido en vez de reducir
el tamaño de fuente: bajarlo revertiria el arreglo de escalado de la ronda anterior.
