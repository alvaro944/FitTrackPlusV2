# Tips Y Skills Practicadas

Este documento guarda aprendizajes concretos, comandos utiles y habilidades practicadas durante el desarrollo.

## Skills Practicadas

### Fase 0 - Mobile foundation

- Crear base Android moderna desde un proyecto existente.
- Separar codigo nuevo en `app/src/main/kotlin`.
- Configurar Compose, Hilt, Room, DataStore y KSP.
- Preparar documentacion viva del proyecto.
- Definir un flujo por fases.

### Fase 1 - Rutinas

- Crear una pantalla Compose conectada a datos reales.
- Modelar `UiState` para una feature.
- Usar `StateFlow` con `collectAsStateWithLifecycle`.
- Combinar datos de Room y DataStore en el ViewModel.
- Separar eventos de UI en funciones del ViewModel.
- Crear un editor simple de listas anidadas:
  - rutina
  - dias
  - ejercicios
- Validar formularios sin meter logica en Compose.
- Archivar datos sin borrarlos fisicamente.
- Mantener una preferencia activa coherente al archivar.

### Fase 2 - Registro de entrenamiento

- Crear una feature Compose con estados de preview, vacio, loading y edicion activa.
- Reanudar una sesion abierta antes de crear una nueva.
- Persistir ediciones de formularios en Room desde eventos de ViewModel.
- Separar el texto editable de UI de los valores normalizados que se guardan.
- Usar snapshots historicos para que los entrenamientos no dependan de la rutina actual.
- Probar reglas de dominio con repositorios fake y `runBlocking`.

### Fase 3 - Historial

- Crear lectura de historial sin depender de rutinas editables.
- Mapear relaciones Room a modelos de dominio de solo lectura.
- Ordenar datos historicos en el mapeo cuando Room no garantiza orden en relaciones.
- Crear una UI Compose minima para verificar flujo sin pulido visual.
- Sembrar datos demo solo en debug y solo si la base esta vacia.
- Probar snapshots historicos con repositorios fake.

## Tips Tecnicos

### Gradle En Windows

Comando estable para build de cierre:

```powershell
.\gradlew.bat build --no-daemon --console=plain
```

Si queda algun daemon vivo:

```powershell
.\gradlew.bat --stop
```

Si `test` falla porque no puede borrar `app/build/test-results`, suele haber un daemon o proceso Gradle anterior reteniendo archivos. Parar daemons y repetir el comando suele liberar el bloqueo.

Si `test` queda sin salida hasta timeout, repetir con:

```powershell
.\gradlew.bat test --no-daemon --console=plain
```

### Kotlin Y Compose State

Si un valor viene de `val state by collectAsStateWithLifecycle()`, Kotlin puede no hacer smart cast sobre propiedades nullable.

Solucion simple:

```kotlin
val editor = state.editor
if (editor == null) {
    // lista
} else {
    // editor ya no es nullable aqui
}
```

### Inputs Numericos

Para campos como peso y repeticiones conviene mantener el texto que el usuario escribe en el `UiState` y normalizar solo al persistir:

- texto invalido o vacio -> `0`
- negativos -> `0`
- valores validos -> se guardan tal cual

### Historial Y Snapshots

Para mostrar entrenamientos antiguos, leer siempre desde tablas historicas:

- `WorkoutSessionEntity`
- `WorkoutExerciseEntity`
- `WorkoutSetEntity`

No consultar la rutina editable para pintar el historial, porque el usuario puede cambiar nombres, dias y ejercicios despues de entrenar.

### Git Local

Este proyecto trabaja sin remoto por ahora.

Ritmo recomendado:

1. rama por fase
2. cambios acotados
3. verificacion
4. docs
5. commit local

## Cosas A Evitar

- Meter Firebase antes de cerrar el flujo local.
- Crear abstracciones por si acaso.
- Hacer navegacion compleja antes de que el flujo lo pida.
- Dejar documentos de progreso sin actualizar.
- Decir que una fase esta cerrada sin test/build reciente.
