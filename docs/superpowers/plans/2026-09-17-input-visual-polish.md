# Plan: Pulido visual de la entrada de datos

> **Para Codex:** lee la spec entera antes de empezar, y lee tambien la entrada **2026-07-07**
> de `docs/progress/phase-log.md` y el **Bug C** de
> `docs/superpowers/specs/2026-07-05-workout-input-ux-bugs.md`. Documentan dos intentos que ya
> fallaron sobre el Bug B. **No los repitas.**
> Commits separados por tarea, conventional commits, **sin Co-Authored-By**. `test` + `build`
> antes de cada commit. NO lances el emulador: la pasada manual la hace el dueño.

**Rama:** `fix/input-visual-polish` (nueva, desde `develop`)
**Spec:** `docs/superpowers/specs/2026-09-17-input-visual-polish.md`
**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && export ANDROID_HOME=/Users/alvaro/Library/Android/sdk && sh gradlew test`

**Sin relacion con `feature/progression-engine-primary`.** No mezcles las dos ramas.

---

## Lo que hace especial a este plan

Los dos bugs son **visuales y no verificables por test automatico**. `test` + `build` en verde
NO significa que esten arreglados: solo que no has roto nada. La verificacion real es la pasada
manual del dueño.

Por eso aqui importa mas de lo normal **cambiar una sola cosa por commit**. Si un commit toca
dos variables y el sintoma cambia, no se sabra cual fue, y este bug ya ha consumido dos
intentos fallidos precisamente por eso.

---

## Tarea 0: Preparar rama

- [ ] `git checkout develop && git pull`
- [ ] `git checkout -b fix/input-visual-polish`
- [ ] `test` + `build` en verde antes de tocar nada
- [ ] **`design/` no se toca.** Prototipo de UI ajeno, sin seguimiento. Ni commit, ni borrado,
      ni `.gitignore`.

---

## Tarea 1: Unificar el tipo de teclado (A1, A2)

Ficheros: `core/design/SetRow.kt`.

- [ ] `KeyboardType.Number` -> `KeyboardType.Decimal` en los campos de **repeticiones**
      (lineas ~269 y ~470). Los de peso ya son `Decimal`: no los toques.
- [ ] **`ImeAction` se queda como esta** (`Next` en peso, `Done` en reps). Es deliberado: se
      mide el efecto de una sola variable. Ver A3 en la spec.
- [ ] Comprobar que el parseo de repeticiones sigue siendo entero y que escribir "8.5" no
      persiste un decimal ni rompe el guardado de la serie. Si ya se filtra hoy, no toques
      nada; si no, filtralo.
- [ ] Test del filtrado de reps si no existe ya.
- [ ] `test` + `build`, commit.

## Tarea 2: Declarar windowSoftInputMode (A4)

Ficheros: `app/src/main/AndroidManifest.xml`.

- [ ] `android:windowSoftInputMode="adjustResize"` en la `<activity>` de `MainActivity`.
      Hoy no se declara (verificado 2026-09-17).
- [ ] Commit **aparte** de la Tarea 1. Son dos variables distintas del mismo sintoma y el
      dueño tiene que poder saber cual hizo que.
- [ ] `test` + `build`, commit.

---

## Tarea 3: El experimento del recuadro blanco (B1)

Ficheros: `app/src/main/res/values/themes.xml`. **Solo ese.**

> Antes de tocar: la hipotesis es que
> `<item name="android:background">@color/base</item>` (`#F5F5F5`) es lo que pinta el
> rectangulo. `android:background` a nivel de tema es el atributo equivocado — el de fondo de
> ventana es `android:windowBackground`, que no esta declarado en ningun sitio. Al ser de tema
> lo hereda cualquier View nativa, y el handle de seleccion vive en un `Popup`, que por debajo
> es una View.
>
> **Esta hipotesis NO esta confirmada.** Es un experimento, no un arreglo.

- [ ] Quitar `<item name="android:background">@color/base</item>`.
- [ ] Si al hacerlo el fondo general de la app cambia, declarar
      `<item name="android:windowBackground">@color/base</item>` en su lugar — que es el
      atributo correcto — y nada mas.
- [ ] **El diff de este commit toca esa linea y solo esa.** Si toca mas, el experimento no
      vale y hay que rehacerlo. No aproveches para "ya que estoy" ordenar el tema.
- [ ] No toques `colorAccent`, `colorControlActivated`, `colorControlNormal`,
      `colorBackgroundFloating` ni los `textColorPrimaryInverse*`: esos son del arreglo de
      2026-07-05, funcionan, y son los que hacen que el handle sea verde de marca.
- [ ] `test` + `build`, commit con mensaje que deje claro que es un experimento pendiente de
      verificacion visual.

## Tarea 4: Cierre y registro

- [ ] `test` + `build` completos en verde.
- [ ] Entrada nueva en `docs/progress/phase-log.md`, fechada, **junto a la de 2026-07-07**:
      que se probo (quitar `android:background` del tema), por que se probo (el color coincide,
      el atributo es el equivocado, y explica que el recuadro salga tambien en otros sitios), y
      **pendiente de verificacion visual del dueño**.
- [ ] Actualizar `docs/progress/project-progress.md` con los dos bugs como pendientes de
      pasada manual.
- [ ] Push y avisar.

---

## Aviso obligatorio al dueño al terminar

Decir explicitamente estas tres cosas:

1. **Nada de esto esta verificado.** `test` y `build` en verde solo dicen que no se ha roto
   nada. Los dos bugs son visuales.

2. **Que mirar en el emulador:**
   - Saltar entre peso y reps sin que el teclado parpadee.
   - Que el campo de reps ahora muestra la tecla del punto decimal: **es esperado y
     aceptado**, no un fallo.
   - Que el handle de seleccion no tiene recuadro detras.
   - Que el fondo general de la app no ha cambiado.
   - Los "otros sitios" donde el dueño recordaba ver recuadros parecidos.

3. **Si el recuadro sigue ahi: se para.** No hay cuarto intento a ciegas. El siguiente paso
   es que el dueño abra **Layout Inspector** en Android Studio con el handle visible y capture
   que View pinta el rectangulo. Sin ese dato no se toca nada mas.
