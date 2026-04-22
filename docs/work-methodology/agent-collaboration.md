# Trabajo Con El Agente

Esta guia recoge como colaborar con la IA sin perder control del proyecto ni del aprendizaje.

## Antes De Tocar Codigo

El agente debe leer:

- documentacion base del proyecto
- estado de Git
- area concreta que se va a modificar

Esto evita cambios a ciegas y ayuda a respetar el estilo existente.

## Durante La Implementacion

La forma de trabajo deseada:

- cambios pequenos
- explicacion corta antes de editar
- verificar con compilador pronto
- no mezclar refactors con features
- dejar claro lo que se decide y por que

## Al Encontrar Problemas

Registrar problemas reales es parte del aprendizaje.

Ejemplos de Fase 1:

- permisos del sandbox al crear ramas
- Gradle necesitando acceso fuera del sandbox para usar `.gradle`
- build con daemon quedandose sin salida
- error de smart cast en Kotlin por estado delegado

Lo importante no es solo arreglarlo, sino guardar el patron para reconocerlo despues.

## Al Cerrar Fase

El agente debe comentar:

- que se implemento
- que archivos principales cambiaron
- que comandos pasaron
- que quedo pendiente
- que documentacion se actualizo

Ademas, debe revisar esta carpeta y anadir aprendizajes nuevos.
