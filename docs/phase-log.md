# Phase Log

Bitacora viva del proyecto. Cada fase debe anadir aqui lo que se hizo, lo que se verifico, problemas encontrados y decisiones tomadas.

## Fase 0 - Mobile foundation

Rama:

- `codex/phase-0-mobile-foundation`

Commit:

- `c1b2f31 Initialize FitTrackPlus v2 mobile foundation`

Objetivo:

- Crear una base Android moderna para FitTrackPlus v2.
- Separar la nueva app Compose de la v1 XML/Fragments.
- Preparar arquitectura, persistencia local y documentacion.

Cambios principales:

- Se habilito Compose.
- Se agrego Hilt.
- Se agrego Room con KSP.
- Se agrego DataStore.
- Se agrego Navigation Compose.
- Se creo estructura por `core`, `data`, `domain` y `feature`.
- Se creo una shell visual con tabs iniciales.
- Se creo modelo de datos v2 con snapshots historicos.
- Se crearon repositorios y casos de uso iniciales.

Problemas encontrados:

- El workspace no tenia `.git`; se inicializo un repo local.
- Git marco la carpeta como `dubious ownership`; se agrego como `safe.directory`.
- Algunas versiones nuevas de AndroidX/Hilt pedian AGP mas nuevo; se ajustaron a versiones compatibles con AGP `8.5.1`.
- `build` fallo inicialmente por lint en XML legacy con `android:paddingHorizontal` y `android:paddingVertical`; se reemplazaron por paddings compatibles con minSdk 23.
- Se detecto `.gitignore` con restos de conflicto y se limpio.

Decisiones:

- Mantener Firebase fuera del MVP.
- No trackear `google-services.json`.
- No trackear `app/src/main/java` legacy.
- No trackear XML legacy de `layout`, `menu` y `navigation`.
- Mantener un solo modulo Gradle por ahora.

Verificacion:

```powershell
.\gradlew.bat clean test
.\gradlew.bat build --no-daemon
```

Resultado:

- Verificacion automatica correcta.

Pendiente:

- Prueba manual en emulador/dispositivo.

## Fase 1 - Rutinas

Estado:

- Pendiente.

Rama recomendada:

- `codex/phase-1-routines`

Objetivo:

- Crear, listar, editar, archivar y seleccionar rutinas.

Fuera de alcance:

- Registro de entrenamientos.
- Historial.
- Estadisticas.
- Firebase.

Verificacion prevista:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Prueba manual prevista:

- Crear rutina Push/Pull/Legs.
- Editar dias y ejercicios.
- Seleccionar rutina activa.
- Cerrar y abrir la app para comprobar persistencia.
