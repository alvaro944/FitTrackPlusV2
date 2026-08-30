# Patrones, Tips Y Skills

Este documento guarda patrones reutilizables de trabajo tecnico. No intenta describir todas las fases del proyecto, sino dejar bases que sirvan tambien en proyectos futuros.

## 1. Patrones De Coordinacion

### Una sola fuente de ejecucion

- varias herramientas pueden analizar o proponer
- una sola debe ejecutar cambios sobre el repo en cada iteracion
- el cierre y la verificacion conviene que los haga la misma herramienta ejecutora

### Relevo entre herramientas

- no retomar desde memoria conversacional
- releer estado, docs y area tocada
- revisar `git status`
- distinguir cambios propios, ajenos y no verificados

### Propuesta != decision

- un backlog o comentario externo no obliga a implementar nada
- la decision se fija cuando el usuario la valida y el repo la recoge
- la documentacion viva debe reflejar la decision final, no solo la conversacion

## 2. Patrones De Implementacion

### Crecer por capas, no por impulso

- la UI pinta estado y emite eventos
- la coordinacion vive fuera de la UI
- la persistencia queda encapsulada
- las reglas de negocio o calculos importantes no se reparten por varias capas

### Separar pulido de cambio estructural

- una iteracion de UX no deberia reescribir arquitectura
- una iteracion visual no deberia justificar cambios de dominio sin necesidad
- una iteracion de deuda no deberia mezclar features nuevas

### Prototipo externo -> implementacion real

- usar el prototipo para entender composicion, ritmo, capas y prioridades
- no perseguir fidelidad ciega si rompe mantenibilidad
- adaptar la idea al runtime real del producto

### Assets de documentacion -> runtime

- los assets de `docs/` sirven como referencia o fuente visual
- si un asset pasa a producto, debe copiarse al arbol operativo de la app o proyecto
- no dejar dependencias de runtime apuntando a carpetas documentales

## 3. Patrones De Verificacion

### Verificacion proporcional

- cambio pequeno de docs: revisar consistencia
- cambio de codigo: tests + build
- cambio visible o de flujo: verificacion manual cuando sea posible
- cambio de proceso: comprobar que la documentacion no se contradiga

### Verificacion escalonada para ahorrar coste

En este repo, `build` completo consume bastante tiempo y salida de terminal. Para reducir CPU, espera y tokens sin perder rigor:

- docs only: no ejecutar Gradle; revisar consistencia documental
- cambio pequeno de UI Compose: ejecutar `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain` y hacer revision manual si aplica
- cambio en ViewModel, dominio, repositorios o reglas: ejecutar `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain` o el test concreto si existe
- cambio en Room, DI, navegacion compartida o superficie transversal: ejecutar `.\gradlew.bat test --no-daemon --console=plain` y considerar `build`
- cierre de fase, merge, push importante o cambio con riesgo: ejecutar `.\gradlew.bat test --no-daemon --console=plain` y `.\gradlew.bat build --no-daemon --console=plain`
- GitHub Actions debe confirmar `test`, `build` y `detekt` en limpio cuando haya push o PR

Regla practica:

- durante una iteracion, usar el check mas pequeno que pruebe el cambio real
- agrupar micro cambios relacionados y verificar una vez
- reservar `build` completo para cierres o cambios con blast radius real
- no afirmar cierre de fase sin verificacion completa reciente o CI verde

### Tooling fragil

Si una toolchain falla de forma intermitente:

- documentar el comando estable
- documentar el patron de recuperacion
- evitar combinaciones peligrosas si ya se ha visto que rompen caches o daemons

### Verificacion secuencial

Cuando un proyecto tenga herramientas con caches sensibles:

- ejecutar checks pesados en secuencia
- no paralelizar por sistema
- registrar cuando el problema es de tooling y no del cambio funcional

## 4. Tips Tecnicos

### OpenAI / GPT-5.5

El proyecto no tiene ahora llamadas runtime a OpenAI. Si se abre esa linea:

- empezar por `gpt-5.5` para trabajo complejo de codigo, agentes, razonamiento o contexto largo
- usar Responses API para flujos con herramientas, razonamiento o estado conversacional
- arrancar con `reasoning.effort = medium` para tareas complejas y medir antes de subir
- usar `low` o `none` solo en tareas estrechas, baratas o sensibles a latencia
- escribir prompts outcome-first: resultado esperado, criterios de exito, restricciones, evidencia y formato
- evitar migrar prompts antiguos tal cual si traen rol exagerado, pasos mecanicos o restricciones repetidas
- no exponer claves OpenAI en Android; cualquier integracion real debe pasar por backend o proxy seguro

### Gradle En Windows

Comandos estables usados aqui:

```powershell
.\gradlew.bat test --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
.\\gradlew.bat detekt --no-daemon --console=plain
```

Si hace falta limpiar estado:

```powershell
.\gradlew.bat --stop
```

Si `compileDebugJavaWithJavac` falla con muchos simbolos faltantes de KSP/Hilt y no aparecen clases en `app/build/tmp/kotlin-classes/debug`, primero recompilar Kotlin y luego repetir la verificacion:

```powershell
.\gradlew.bat :app:compileDebugKotlin --rerun-tasks --no-daemon --console=plain
```

### APK Preview Y Releases

Para este repo, una preview descargable y una release de produccion no son lo mismo:

- `assembleDebug` puede generar un APK instalable y valido para compartir previews
- `assembleRelease` sigue produciendo `app-release-unsigned.apk` hasta que exista firma real
- no conviene publicar una `unsigned release` como si fuera el canal oficial para usuarios

Patron reusable que ha funcionado aqui:

- documentar en `README.md` que el artefacto publico actual es preview
- publicar previews con GitHub Releases mediante tags (`v*-preview*`)
- renombrar el asset a un nombre estable como `FitTrackPlus-preview.apk`
- dejar la release firmada para una fase separada con keystore, secretos y pipeline propios

Regla practica:

- si no hay firma real, hablar de preview
- si se publica en GitHub, que el nombre del asset y la documentacion no oculten esa realidad

### Kotlin Y Estado De UI

Si el estado delegado impide smart cast, conviene asignar primero a una variable local clara antes de bifurcar la UI.

### Datos Derivados

Si un dato mostrado depende de varias fuentes o formulas, suele ser mejor:

- leer datos crudos desde la capa adecuada
- derivar el agregado en una capa de dominio o coordinacion
- exponer a UI un estado ya preparado

## 5. Cosas A Evitar

- declarar cerrado algo sin verificacion reciente
- abrir varias lineas de trabajo grandes en una misma iteracion
- ejecutar propuestas externas sin contraste con el repo
- usar la conversacion como sustituto del estado real del proyecto
- mezclar backlog, decision y cierre como si fueran lo mismo

## 6. Skills Practicadas

Hasta ahora, las habilidades mas utiles que merece la pena repetir han sido:

- leer estado real antes de tocar codigo
- traducir referencias visuales externas a implementacion nativa mantenible
- separar propuesta, decision e implementacion
- actualizar metodologia cada vez que aparece un patron reusable
- documentar no solo que fallo, sino como se recupero el flujo de trabajo
