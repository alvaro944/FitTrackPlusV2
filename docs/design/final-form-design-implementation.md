# Final Form Design - Implementacion

Este documento conecta la referencia visual `docs/design/final-form-v1/` con su
implementacion nativa en Jetpack Compose.

## Fuente Visual

La carpeta de referencia contiene un prototipo navegable y datos simulados:

- `FitTrackPlus.html`: tokens visuales, paleta clara/oscura y estructura base.
- `app-shell.jsx`: composicion y estados propuestos para las pantallas.
- `mock-data.js`: datos de ejemplo usados por el prototipo.
- `tweaks-panel.jsx`: controles auxiliares del prototipo.

El prototipo es una referencia visual. No se integra como runtime ni sustituye
la arquitectura Android existente.

## Cambios Aterrizados

### Sistema visual

- Paleta clara y oscura alineada con los tokens Final Form.
- Bordes, superficies y radios mas contenidos.
- Cards sin elevacion decorativa y con jerarquia basada en superficie/borde.
- Numeros y metricas mantienen tratamiento monoespaciado.

Archivos principales:

- `core/design/Theme.kt`
- `core/design/Cards.kt`
- `core/design/Indicators.kt`

### Inicio

- Tira semanal compacta.
- Estado de actividad semanal visible antes del hero principal.
- Jerarquia de cards alineada con el nuevo sistema visual.

### Entrenar

- Timer de descanso circular mediante `FitTrackRadialTimer`.
- Acciones de pausar, reanudar, reiniciar y cancelar agrupadas.
- Estados visuales diferenciados para timer listo, activo, pausado y terminado.

### Ajustes

- Selector segmentado para `kg/lb`.
- Selector visual exclusivo para tema `Sistema`, `Claro` y `Oscuro`.
- La persistencia y comportamiento existentes no cambian.

## Restricciones Respetadas

- Sin cambios de Firebase o sync.
- Sin cambios de Room schema.
- Sin cambios de repositorios o snapshots historicos.
- Compose pinta estado y envia eventos; no contiene reglas de negocio nuevas.
- La referencia web permanece separada de la app Android.

## Verificacion

Ejecutado correctamente:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

## Validacion Manual Pendiente

- Revisar Home en tema claro, oscuro y sistema.
- Comprobar que la tira semanal cabe en pantallas estrechas.
- Probar el timer radial en estados listo, corriendo, pausado y terminado.
- Confirmar ergonomia de los controles del timer durante una sesion real.
- Verificar selector de unidad y tema en telefono real.
- Revisar contraste y legibilidad en ambos temas.

## Siguientes Iteraciones Visuales

Las ideas no implementadas siguen en `docs/design/visual-improvements.md`. Deben
aterrizarse en fases pequenas y verificables, sin convertir el prototipo web en
una fuente de logica de negocio.
