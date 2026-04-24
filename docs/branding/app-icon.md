# Icono De App

## Estado

- Estado actual: cerrado
- Ultima decision valida: 2026-04-24 — adaptive icon generado en Android Studio

## Decision

El icono de FitTrackPlus deriva directamente del simbolo principal de marca.

- Simbolo base: flechas/columnas ascendentes + diamante verde con detalle cobre
- Fondo del launcher: `#F4F4F1`
- Tipo de icono: adaptive icon de Android
- Uso principal: launcher, app switcher y recurso `ic_launcher-playstore.png`

## Relacion Entre Marca, Icono E Intro

- **Logo principal**: el simbolo abstracto sigue siendo la marca principal.
- **Icono de app**: usa ese simbolo en formato compacto y legible para launcher.
- **Pantalla de inicio**: puede acompanar el simbolo con wordmark o copy breve, pero eso no convierte el wordmark en logo principal.

La regla es clara: el icono y la intro parten del mismo ADN visual, pero no cumplen la misma funcion.

## Criterios Cerrados

- debe leerse bien a tamano pequeno
- debe mantener la silueta del simbolo sin sobrecarga
- debe usar fondo claro mineral para reforzar consistencia de marca
- debe poder convivir con la UI grafito + esmeralda sin parecer un elemento ajeno
- no necesita efectos atmosfericos ni detalles de presentacion

## Diferencia Entre Icono E Intro

El icono de app debe ser mas estricto que la intro:

- el icono prioriza legibilidad y recognoscibilidad
- la intro puede usar glow suave, wordmark y un poco mas de presencia visual
- los assets de intro no sustituyen ni contaminan el launcher

## Assets Reales

- Launcher foreground/background generados en Android Studio
- `app/src/main/ic_launcher-playstore.png` actualizado
- Fondo crema del launcher en `ic_launcher_background.xml`

## Evitar

- usar el logo con fondo completo como icono
- mezclar icono de launcher con piezas promocionales
- introducir mas detalle del que sobrevive en 48-192 px
- tratar la intro como si fuera el icono de app
