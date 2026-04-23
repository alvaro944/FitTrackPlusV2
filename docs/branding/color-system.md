# Sistema De Color

Este documento recoge la direccion cromatica de la marca y como baja a UI.

## Estado

- Estado actual: cerrado
- Ultima decision valida: 2026-04-24 — paleta confirmada, roles definidos

## Direccion

El logo confirma la paleta: verde esmeralda como senal principal, grafito como base,
cobre/dorado como acento calido puntual. La paleta ya implementada en Fase 6 es correcta.

## Preguntas Resueltas

- El verde es la seña principal — es el color de accion primaria y el color del diamante del logo.
- El acento calido (#C47A49 cobre) es real — aparece en el contorno del diamante. Se usa de forma muy puntual.
- La marca es mineral y editorial, no deportiva ni tecnica-fria.
- Paleta estable y atemporal — sin tendencias cromaticas de temporada.

## Paleta Base

- Fondo: `#F4F4F1`
- Superficie: `#FCFBF7`
- Superficie alternativa: `#E8E5DD`
- Texto principal: `#161816`
- Texto secundario: `#5E655F`
- Primario: `#1F6B57`
- Primario oscuro: `#174D40`
- Primario suave: `#D9E8E1`
- Acento calido: `#C47A49`
- Acento suave: `#F1E2D6`
- Error: `#B15249`

## Reglas De Uso

- contraste alto para uso real en movil
- nada de neones
- nada de morados genericos
- nada de degradados por defecto
- el color debe ayudar a orientarse, no a decorar

## Aterrizaje A UI

Roles definitivos del sistema de color:

- **Color de marca principal**: `#1F6B57`
- **Color de accion primaria**: `#1F6B57` (light) / `#7FCDB7` (dark)
- **Color de exito**: `#1F6B57` (mismo que primario)
- **Color de error**: `#B15249`
- **Color de informacion**: pendiente de definir si se necesita
- **Fondos**: `#F4F4F1` (light) / `#161816` (dark aprox)
- **Superficies**: `#FCFBF7` (light) / superficie oscura equivalente
- **Bordes**: `#E8E5DD` (light) / suavizado oscuro
- **Acento calido**: `#C47A49` — uso muy puntual (detalles del logo, iconos decorativos, acentos de calidad)

Esta paleta ya esta implementada en `core/design/Theme.kt` con extensiones semanticas
(primarySoft, primaryDark, accentWarm, surfaceCard, borderLight, textTertiary, errorSoft).
