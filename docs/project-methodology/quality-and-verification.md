# Quality And Verification

La verificacion debe ser proporcional al riesgo, no identica para todo tipo de cambio.

## Niveles De Verificacion

### Cambio de documentacion

- revisar consistencia
- comprobar que no contradice el estado real

### Cambio de codigo

- tests relevantes
- build o compilacion

### Cambio visible o de flujo

- verificacion manual cuando sea posible
- comprobar que lo disenado sigue siendo usable y legible

### Cambio de proceso

- verificar que la documentacion operativa y el starter pack siguen alineados
- comprobar que no se han introducido detalles especificos del proyecto en la metodologia general

## Reglas Practicas

- usar el comando estable, no el mas rapido si el tooling es fragil
- no paralelizar checks pesados por sistema
- documentar patrones de recuperacion cuando una toolchain falle de forma intermitente
- distinguir fallo del codigo y fallo del entorno

## Pendientes Aceptables

Si una verificacion no puede hacerse:

- dejarlo escrito
- explicar por que
- dejar claro quien o que la desbloquea

## Objetivo

La verificacion no solo sirve para atrapar errores. Tambien sirve para que el cierre de una iteracion sea defendible.
