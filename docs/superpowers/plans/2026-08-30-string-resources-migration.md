# Plan: Migrar literales de texto a stringResource (P6-1)

> **NO empezar esta rama todavia.** Prerequisito bloqueante: `refactor/home-stats-visual-locale`
> y `fix/shared-accessibility-and-copy` deben estar ambas fusionadas en `develop` primero. Es un
> cambio global que toca casi cada pantalla — ejecutarlo mientras esas dos ramas siguen vivas
> garantiza conflictos de merge. Cuando ambas esten fusionadas, crear la rama
> `refactor/string-resources` desde `develop` y seguir este plan fase por fase.

**Entorno macOS:** `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && sh gradlew test`

Commits separados por fase (paquete), conventional commits, sin Co-Authored-By. Verificar
`test` + `build` antes de cada commit. No commits de WIP.

---

## Tarea 0: Preparar rama e inventario

- [ ] Confirmar que las dos ramas prerequisito ya estan en `develop`: `git log develop --oneline | grep -i "home-stats-visual-locale\|shared-accessibility"`
- [ ] `git checkout develop && git pull && git checkout -b refactor/string-resources`
- [ ] Generar inventario de literales por fichero (no hace falta commitearlo, es solo guia de trabajo)

---

## Fase 1: `core/design`

- [ ] Extraer literales de dialogos, botones y labels compartidos a `strings.xml` (prefijo `design_`)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract core design system strings to resources`

---

## Fase 2: `feature/onboarding` y `feature/launch`

- [ ] Extraer literales de las pantallas de onboarding e intro (prefijo `onboarding_`, `launch_`)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract onboarding and launch screen strings to resources`

---

## Fase 3: `feature/settings`

- [ ] Extraer literales de Ajustes (prefijo `settings_`)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract settings screen strings to resources`

---

## Fase 4: `feature/home`

- [ ] Extraer literales de Home (prefijo `home_`), incluyendo los plurales de sesiones/dias con `<plurals>`
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract home screen strings to resources`

---

## Fase 5: `feature/routines`

- [ ] Extraer literales del editor de rutinas, dias, ejercicios y alternativas (prefijo `routines_`)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract routines screen strings to resources`

---

## Fase 6: `feature/workout`

- [ ] Extraer literales de Entrenar, incluyendo dialogos de finalizar sesion y alternativas (prefijo `workout_`)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract workout screen strings to resources`

---

## Fase 7: `feature/history`

- [ ] Extraer literales de Historial (prefijo `history_`)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract history screen strings to resources`

---

## Fase 8: `feature/stats`

- [ ] Extraer literales de Datos, incluyendo descripciones de accesibilidad ya añadidas en `refactor/home-stats-visual-locale` (prefijo `stats_`)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract stats screen strings to resources`

---

## Fase 9: Widget y resto

- [ ] Extraer literales de `FitTrackPlusWidget.kt` y cualquier notificacion/deep link con texto visible, con plurales via recursos (prefijo `widget_`)
- [ ] `test` + `build` en verde
- [ ] Commit: `refactor: extract widget strings to resources`

---

## Verificacion final

- [ ] `sh gradlew test` en verde
- [ ] `sh gradlew build` en verde
- [ ] Grep de `Text(\s*"` sobre `app/src/main/kotlin` para confirmar que no quedan literales sueltos fuera de los casos explicitamente excluidos (logs, nombres internos)
- [ ] Revision de que ningun texto cambio de contenido, solo de ubicacion
- [ ] Detenerse y pedir autorizacion antes de push, como en las ramas anteriores

**Merge:** a `develop` cuando compile, pasen los tests, y tras pasada manual del dueño confirmando que ningun texto visible cambio.
