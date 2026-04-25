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
