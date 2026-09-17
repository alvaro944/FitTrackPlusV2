# Spec: Pulido visual de la entrada de datos

**Fecha:** 2026-09-17
**Plan de ejecucion:** `docs/superpowers/plans/2026-09-17-input-visual-polish.md`
**Prerequisito:** ninguno. Parte de `develop`.

**Sin relacion con** `feature/progression-engine-primary`. Son dos bugs esteticos que el dueño
reporto durante la pasada manual del 2026-09-17 y que existen desde antes.

---

## Bug A — El teclado parpadea al saltar entre campos

### Sintoma

Al tocar un campo (peso, reps) aparece el teclado. Al tocar **otro campo sin cerrarlo antes**,
el teclado se oculta y se vuelve a mostrar: un parpadeo en cada transicion. Pasa siempre, en
todas las transiciones de la pantalla de entrenamiento.

### Causa

`core/design/SetRow.kt`:

```
linea 256   Peso  ->  KeyboardType.Decimal  +  ImeAction.Next
linea 269   Reps  ->  KeyboardType.Number   +  ImeAction.Done
linea 389   Peso  ->  KeyboardType.Decimal  +  ImeAction.Next
linea 470   Reps  ->  KeyboardType.Number   +  ImeAction.Done
```

Campos contiguos piden `inputType` **y** `imeOptions` distintos. Al mover el foco, Android
reconstruye la conexion de entrada porque el `EditorInfo` cambia, y la mayoria de IMEs
(Gboard incluido) responden a un `restartInput` **desmontando y re-inflando la vista del
teclado**. Eso es el parpadeo.

No es un bug de Compose ni de `FitTrackSelectAllTextField` (verificado: ese componente no
llama a `hide()` ni a `show()` en ningun sitio). Es comportamiento de plataforma.

Como **todas** las transiciones cambian el tipo, no hay ninguna que se libre.

### Decision del dueño

Unificar el `KeyboardType`. **Se acepta que el campo de repeticiones muestre la tecla del
punto decimal** a cambio de eliminar el parpadeo. Decidido explicitamente el 2026-09-17.

### Requisitos

**A1 — Mismo `KeyboardType` en los campos contiguos de serie.** `KeyboardType.Decimal` en
peso y en repeticiones. No al reves: `Number` en peso seria una regresion funcional, impide
cargas como 12,5 kg.

**A2 — El parseo de repeticiones sigue siendo entero.** La tecla decimal es visible pero el
valor se sigue interpretando como `Int`. Escribir "8.5" no puede persistir 8.5 reps ni
romper el guardado. Si hoy eso ya se filtra, no lo toques; si no, filtralo.

**A3 — `ImeAction` se deja como esta en este primer paso.** `Next` en peso, `Done` en reps.
El factor dominante del `restartInput` es el `inputType`; cambiar solo la accion hace que
muchos IMEs redibujen la tecla de accion sin desmontar la vista entera. Se mide primero con
un solo cambio.

**A4 — `windowSoftInputMode` declarado.** `app/src/main/AndroidManifest.xml` no lo declara
(verificado el 2026-09-17). Añadir `android:windowSoftInputMode="adjustResize"` a la
`<activity>` de `MainActivity`. **No arregla el `restartInput`**, pero es lo recomendado con
Compose edge-to-edge y suaviza el movimiento de la pantalla al entrar y salir el teclado.

### Criterios de aceptacion

1. `test` y `build` en verde.
2. Saltar de peso a reps y de reps al peso de la siguiente fila **sin que el teclado
   desaparezca y reaparezca**. Verificacion **manual en emulador**, no automatizable.
3. Escribir "8.5" en repeticiones no persiste un decimal ni rompe el guardado de la serie.
4. Sigue siendo posible introducir pesos decimales (12,5 kg).
5. Si tras A1 el parpadeo **persiste**, se anota en `docs/progress/phase-log.md` lo probado y
   **se para**. El siguiente paso seria unificar tambien `ImeAction`, pero no se aplica a
   ciegas en la misma pasada: se mide primero.

---

## Bug B — Cuadrado blanco detras del handle de seleccion

### Sintoma

Al tocar un campo de texto aparece el handle (la flecha/gota verde que marca el cursor) con
un **rectangulo blanco visible detras**. El dueño reporta que ese mismo tipo de recuadro
aparece **tambien en otros sitios de la app**, aunque no recuerda cuales.

### Historia: dos intentos fallidos

Documentado en `docs/progress/phase-log.md`, entrada **2026-07-07**:

> Intentos descartados: drawables nativos `android:textSelectHandle*` y
> `LocalTextSelectionColors.handleColor = Color.Transparent`; ninguno elimino el fondo blanco
> en dispositivo. Se revierten ambos intentos para no dejar cambios sin efecto.
> **No aplicar mas cambios sin una reproduccion/control visual claro.**

Un tercer cambio (spec `2026-07-05-workout-input-ux-bugs.md`, Bug C) si se aplico y **sigue
en el codigo**: alinear `Theme.FitTrackPlus` a la paleta v2 y añadir `colorAccent`,
`colorControlActivated`, `colorControlNormal`, `colorBackgroundFloating` y los
`textColorPrimaryInverse*`. Eso **arreglo el color** del handle (ahora es el verde de marca en
vez del teal generico de MaterialComponents) pero **no elimino el rectangulo**.

**Esta spec respeta esa instruccion: no propone un cuarto intento a ciegas.** Propone una
hipotesis concreta con protocolo de verificacion y un punto de parada.

### Hipotesis (SIN CONFIRMAR)

`app/src/main/res/values/themes.xml`:

```xml
<item name="android:background">@color/base</item>     <!-- #F5F5F5 -->
```

Tres cosas la sostienen:

1. **El color coincide.** `@color/base` es `#F5F5F5`, que a ojo es el "blanco" del recuadro.
2. **El atributo es el equivocado.** El fondo de ventana se declara con
   `android:windowBackground`, que **no esta declarado en ningun sitio** (verificado). Poner
   `android:background` a nivel de tema hace que lo resuelva cualquier View que consulte ese
   atributo. El handle de Compose no lo pinta Compose: vive en un `Popup`, que por debajo es
   una View añadida al WindowManager.
3. **Explica el dato nuevo.** El dueño dice que el recuadro aparece tambien en otros sitios.
   Una causa especifica del handle no explicaria eso; un `android:background` de tema afecta a
   todos los popups y vistas nativas de la app. Los dos intentos descartados eran ambos
   especificos del handle, y por eso ninguno podia funcionar si la causa es esta.

Huele a residuo de la v1, igual que los colores azules que ya se limpiaron.

### Protocolo, no fix

**B1 — Una sola variable.** Quitar `<item name="android:background">@color/base</item>`. Si
resulta que hace falta un fondo de ventana, declararlo como `android:windowBackground`, que
es el atributo correcto. **Un solo cambio.** No tocar nada mas del tema en la misma pasada: si
se cambian dos cosas y el bug desaparece, no se sabra cual fue.

**B2 — Comprobacion visual.** Tocar un campo de texto y mirar el handle. Mirar tambien el
panel de copiar/pegar y cualquier otro popup.

**B3 — Si NO se arregla, se PARA.** Nada de quinto intento. Con el handle visible en pantalla,
abrir **Layout Inspector** de Android Studio y localizar que View exacta pinta el rectangulo,
y con que fondo. Eso es la "reproduccion/control visual claro" que pedia la nota de
2026-07-07. Adjuntar la captura al phase-log.

**B4 — Se anota pase lo que pase.** Entrada nueva en `docs/progress/phase-log.md` junto a la
de 2026-07-07, diciendo que se probo y con que resultado. Si funciona, se cierra la deuda
explicitamente. Si no, se suma a la lista de descartados para que nadie lo repita.

### Criterios de aceptacion

1. `test` y `build` en verde.
2. Exactamente **un** cambio en `themes.xml`. Si el diff toca mas de esa linea (o su
   sustitucion por `android:windowBackground`), el experimento no vale.
3. Verificacion **manual en emulador**: el handle de seleccion no muestra recuadro detras.
   No hay criterio automatizable para esto — es visual y depende del dispositivo.
4. Comprobado tambien que el fondo general de la app no ha cambiado al quitar el atributo.
5. `phase-log.md` actualizado con el resultado, sea cual sea.

---

## Fuera de alcance

- Cualquier cosa del Progression Engine.
- Rediseñar `SetRow` o `FitTrackSelectAllTextField`.
- Tocar el comportamiento de select-all al enfocar (`f54541c`), que funciona.
- Unificar `ImeAction` (solo si A1 resulta insuficiente, y en otra pasada).
- Perseguir el recuadro en "otros sitios" antes de saber si B1 lo arregla en el handle.
