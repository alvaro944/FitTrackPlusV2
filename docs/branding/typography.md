# Tipografia

Este documento define la direccion tipografica de la marca.

## Estado

- Estado actual: direccion elegida, implementacion pendiente
- Ultima decision valida: 2026-04-24 — opcion C seleccionada

## Objetivo

La tipografia debe apoyar esta mezcla:

- claridad
- fuerza
- sobriedad
- ritmo editorial ligero

## Opcion Elegida: C — Sans principal + mono de apoyo para metricas

**Sans principal**: una sans con caracter pero alta legibilidad en movil.
Candidatas: DM Sans, Plus Jakarta Sans, Inter.
Sirve para: titulos, etiquetas, navegacion, textos de interfaz.

**Mono de apoyo**: para numeros de series, pesos, repeticiones y datos de entrenamiento.
Candidatas: JetBrains Mono, Fira Code, Roboto Mono.
Sirve para: `80 kg`, `3x10`, marcas, progreso numerico.

**Razon de la eleccion**:
- Los datos numericos del entrenamiento tienen mas personalidad y precision con mono.
- La sans principal da claridad y madurez sin ser enterprise generica.
- La combinacion sans + mono es coherente con el tono tecnico-editorial de la marca.

## Estado De Implementacion

La app usa actualmente Material3 typography con la fuente del sistema.
Esto es valido para el MVP — el sistema tipografico es correcto en escala y jerarquia.

Cuando se decida integrar una fuente custom:
- Sans principal: importar via `res/font/` + declarar en `Typeface` dentro de `Theme.kt`.
- Mono: importar via `res/font/` + aplicar en los composables de metricas y datos.
- Candidatos concretos a evaluar en ese momento: DM Sans + JetBrains Mono.

## Criterios Confirmados

- buena lectura en movil — prioritario
- buen rendimiento en titulos y cifras — cubierto por mono en datos
- no parecer una app enterprise generica — cubierto por sans con caracter
- no sacrificar claridad por personalidad — regla de oro al elegir la fuente final
