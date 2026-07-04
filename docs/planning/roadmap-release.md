# Roadmap hacia release — FitTrackPlus Personal

Fecha: 2026-07-04
Fuentes: `docs/planning/product-vision.md` (fases de producto) + `docs/planning/technical-audit-2026-07.md` (hallazgos con evidencia).
Este roadmap sustituye operativamente a `roadmap-2.1.md` (cerrado: sus fases 2.1A-2.1C estan completas).

Regla de oro heredada de la vision de producto: **cada tarea debe acercar la app a manos de usuarios reales**. Las fases estan ordenadas para que cada una desbloquee la siguiente.

---

## Mapa de fases

```
R1 Integridad de datos ──> R2 Crashes y UX ──> R3 Play readiness ──> R4 Prueba cerrada ──> Publicacion
                                   │
                                   └──(en paralelo, sin bloquear)── R5 Deuda de calidad
                                                                     R6 Pre-sync (antes de Coach)
```

R1-R3 son secuenciales y bloqueantes para publicar. R5 se hace por goteo en las mismas ramas cuando se toca la zona afectada. R6 son decisiones de diseño, no implementacion.

---

## Fase R1 — Integridad de datos

**Por que primero**: son los unicos fallos que pueden corromper o ensuciar datos reales, y el dueño usa la app a diario. Cada dia sin fix es riesgo acumulado.

Rama sugerida: `fix/r1-data-integrity`

| # | Tarea | Referencia auditoria |
|---|---|---|
| 1 | Indice unico parcial `WHERE finishedAt IS NULL` + transaccion en start de sesion (requiere migracion DB v3) | DAT-C1 |
| 2 | `finishSession` como UPDATE atomico idempotente con guarda `AND finishedAt IS NULL` | DAT-C2 |
| 3 | `enabled = !isFinishing` en el boton Finalizar del dialogo | DAT-C2 (UI) |
| 4 | Funcion canonica de agrupacion de `performedVariantKey` (resuelve formatos `exercise-$id`, `legacy:`, UUID) usada por Stats | DAT-H1 |
| 5 | Heatmap con fecha local (mismo patron que streak) | DAT-M1 |
| 6 | Excluir `fittrackplus_v2.db` del backup (backup_rules + data_extraction_rules) | REL-B1 |
| 7 | Tests: finish/discard de sesion (fakes, patron existente) + test de migracion con `MigrationTestHelper` (v1→v2→v3) | REL-H6, REL-M1 |
| 8 | Limpieza de sesiones huerfanas existentes (query one-shot al arrancar o en migracion v3: cerrar/borrar sesiones abiertas antiguas sin sets con datos) | DAT-C1 |

**Criterio de salida**: imposible tener dos sesiones abiertas (garantia a nivel de DB, no solo de UI); finalizar es idempotente; un usuario migrado de v1 ve sus ejercicios agrupados correctamente en Stats; el backup no puede corromper la DB; `test` + `build` + test de migracion en verde.

---

## Fase R2 — Crashes y UX bloqueantes

**Por que**: son los fallos que un tester encontraria en la primera semana. Corresponde a "errores bloqueantes" y "problemas importantes de experiencia" de la vision de producto (seccion 5).

Rama sugerida: `fix/r2-crashes-ux`

| # | Tarea | Referencia |
|---|---|---|
| 1 | `options.firstOrNull()` + guarda de apertura en dialogo de alternativas | UI-C1 |
| 2 | Eliminar `requireNotNull(LocalActivity)`: pasar navigation blocker como lambda desde el shell | UI-C2 |
| 3 | Pedir `POST_NOTIFICATIONS` en runtime al iniciar el primer entrenamiento (con rationale) | UI-H2 |
| 4 | `onNewIntent` en MainActivity para que la notificacion navegue con la app viva | UI-H1 |
| 5 | Dialogo de alternativas: altura maxima con cabecera fija + IME fiable (valorar ModalBottomSheet) | UI-H4 |
| 6 | Salida limpia del modo edicion de historial cuando la sesion desaparece del flow | UI-H3 |
| 7 | Mensaje al usuario cuando la sesion recuperada ya estaba finalizada (snackbar) | UI (C-3) |
| 8 | `derivedStateOf` para imeBottom (flicker del teclado) | UI-H5 |
| 9 | Area tactil de 48dp en steppers de reps | UI-L6 |

**Criterio de salida**: cero crashes conocidos; la notificacion funciona en Android 13+ con la app viva o muerta; ningun flujo pierde ediciones en silencio; pasada manual en dispositivo del flujo completo.

---

## Fase R3 — Preparacion Google Play

**Por que**: sin esto no existe artefacto publicable. Se puede empezar en paralelo con R2 (no toca codigo de features).

Rama sugerida: `feature/r3-play-readiness`

| # | Tarea | Referencia |
|---|---|---|
| 1 | Politica de privacidad publica (GitHub Pages) + declaracion Health Connect + Data Safety en Play Console | REL-B2 |
| 2 | `signingConfigs.release` con keystore en secrets + `bundleRelease` en CI | REL-H2 |
| 3 | Activar R8 (`isMinifyEnabled` + `shrinkResources`) con keep rules Hilt/Room; verificar app funcional minificada | REL-H1 |
| 4 | Versionado: `2.0.0-alpha1` + estrategia de incremento de versionCode | REL-H3 |
| 5 | Sentry para crash reporting (con subida de mapas R8) | REL-H5 |
| 6 | Metrica de activacion local en DataStore: timestamp primera sesion + contador de sesiones ("segundo entrenamiento en 7 dias") | vision seccion 4 |
| 7 | Icono launcher real (los assets de branding ya existen en `docs/branding/`) | REL-M4 |
| 8 | Retirar `default_web_client_id` de strings.xml | REL-M6 |
| 9 | Cuenta de desarrollador + ficha de Play (capturas, descripcion) | vision fase 2 |

**Criterio de salida**: AAB firmado y minificado que instala y funciona; Play Console acepta la subida a track de prueba cerrada; los crashes de testers llegan a Sentry simbolicados.

---

## Fase R4 — Prueba cerrada

Sin rama: es operacion, no codigo. Guiada por `product-vision.md` secciones 6 y 9.

1. Reclutar 18-25 testers (gimnasio, amigos, comunidades dev/fitness).
2. Publicar en track cerrado; 14 dias con 12+ testers activos (requisito de cuentas nuevas).
3. Instrucciones simples a testers (crear rutina → registrar entrenamiento → consultar historial → reportar).
4. Triage semanal de feedback con la clasificacion de la vision (bloqueante / importante / futuro).
5. Solo se corrigen bloqueantes e importantes durante la prueba. Features congeladas.

**Criterio de salida**: 14 dias completados, bloqueantes a cero, metricas de activacion recogidas → solicitar produccion.

---

## Fase R5 — Deuda de calidad (por goteo, no bloquea)

Regla: cada item se hace cuando una tarea de R1-R3 ya toque ese fichero, o en huecos entre fases. Nunca como rama propia de "refactor grande".

| Item | Cuando |
|---|---|
| Extraer utilidades de parseo peso/reps a `core/util/` (rompe acoplamiento history→workout) | con R2.6 o al tocar HistoryViewModel |
| Trocear `WorkoutScreen.kt` (~1500 lineas) en componentes por seccion | al tocar workout en R2 |
| Debounce de `persistSet` (300ms por setId) | con R2 en WorkoutViewModel |
| Queries dirigidas: previous comparable session y streak en SQL | al tocar los use cases |
| Migrar textos a `strings.xml` (empezar por pantallas que se toquen; decision: antes de crecer mas) | progresivo desde R2 |
| Verde `0xFF2E7D32` → token semantico del design system | al tocar WorkoutScreen |
| Bump Compose BOM + AGP + Health Connect estable | rama propia corta post-R3, antes de produccion |
| CI: job de `connectedAndroidTest` con emulador | post-R3 |
| Tests de WorkoutViewModel (finish/discard como minimo) | parte de R1.7 |
| Borrar tests de plantilla, funcion muerta `shouldAutoStartRestTimer`, zips de docs/branding | cualquier rama que pase cerca |
| Interfaces de repositorio: metodos abstractos en vez de `error()` defaults | al tocar repositorios |

---

## Fase R6 — Decisiones pre-sync (antes de empezar Coach)

No es implementacion: es un documento de diseño que hay que escribir ANTES de la fase 5 de la vision de producto (MVP Coach). Cada semana de uso real encarece estas decisiones.

Decisiones a tomar (detalle en auditoria, seccion 4):

1. Estrategia de identidad: `remoteId` nullable vs UUIDs desde origen en todas las tablas.
2. Identidad canonica de ejercicios entre dispositivos (el `variantKey` local no sirve para merge).
3. `updatedAt` en workout_sessions/exercises/sets para reconciliacion.
4. `replaceRoutine` basado en diff en vez de delete+reinsert.
5. Que es local-only por diseño (activeRoutineId, preferencias de tema).

Entregable: ADR en `docs/adr/0003-sync-data-model.md` cuando se abra la fase Coach. Mientras tanto: cualquier migracion de DB que ocurra por otro motivo (p.ej. la v3 de R1) puede aprovechar para añadir `updatedAt` si el coste marginal es trivial.

---

## Correspondencia con las fases de producto

| Fase producto (product-vision.md) | Fases de este roadmap |
|---|---|
| Fase 1 — Estabilizacion | R1 + R2 |
| Fase 2 — Prueba cerrada | R3 + R4 |
| Fase 3 — Publicacion Personal | salida de R4 |
| Fase 4 — Validacion Coach | (sin codigo; conversaciones con entrenadores) |
| Fase 5 — MVP Coach | requiere R6 decidido |

## Metodologia

- Una rama por fase con prefijo semantico (`fix/r1-...`, `feature/r3-...`), no por tarea, segun el workflow del proyecto.
- Claude especifica (specs en `docs/superpowers/specs/`), Codex implementa y verifica (`test` + `build`), el usuario valida en dispositivo.
- Cierre de fase: actualizar `docs/progress/project-progress.md` y `docs/progress/phase-log.md`.
- Features nuevas congeladas hasta salir de R4, salvo que cumplan el criterio de la vision (seccion 5.3 / principio de trabajo).
