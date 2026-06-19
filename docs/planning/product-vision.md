# FitTrackPlus — Vision de Producto

Ultima actualizacion: 2026-06-19

---

## 1. Que es FitTrackPlus

FitTrackPlus es una aplicacion movil nativa para Android orientada al registro y seguimiento de entrenamientos de gimnasio. Nacio como proyecto de fin de ciclo DAM y ha evolucionado hasta convertirse en un producto real con usuarios activos y un plan de publicacion concreto.

El objetivo a medio plazo es doble:

- **FitTrackPlus Personal**: aplicacion de registro de entrenamiento para usuarios autonomos.
- **FitTrackPlus Coach**: plataforma SaaS para entrenadores personales con panel web y app movil para sus clientes.

---

## 2. El problema que resuelve

La mayoria de personas que entrenan con seriedad gestionan sus rutinas con hojas de calculo, notas de movil, PDFs o cuadernos. Las aplicaciones existentes o son demasiado complejas o no permiten adaptar las rutinas correctamente.

FitTrackPlus cubre el flujo esencial:

1. Crear una rutina y organizarla por dias.
2. Definir ejercicios, series y rangos de repeticiones.
3. Registrar peso y repeticiones por serie en cada entrenamiento.
4. Consultar el historial sin que cambie aunque se edite la rutina.
5. Comparar el progreso entre sesiones.
6. Saber que sesion corresponde realizar.

Enfoque: **fuerza, hipertrofia y gimnasio**. Sin cardio, sin HIIT, sin ruido.

---

## 3. Estado actual de la aplicacion

La segunda version de FitTrackPlus esta en uso personal real. Esto permite detectar problemas que no surgen en desarrollo.

### Funcionalidades implementadas

- Creacion y edicion de rutinas.
- Organizacion por dias con nombre personalizado.
- Creacion de ejercicios con series y rangos de repeticiones.
- Ejercicios alternativos por ejercicio.
- Registro de peso y repeticiones por serie.
- Completion bidireccional de sets (peso y reps).
- Timer de descanso con haptic feedback.
- Historial de entrenamientos con snapshots (el historial no cambia al editar la rutina).
- Comparativa con la sesion anterior.
- Stats con heatmap, rachas y graficas por periodo.
- Deteccion de records personales en vivo.
- Onboarding de 3 pasos.
- Skeletons con shimmer en pantallas principales.
- Widget homescreen y app shortcuts.
- Notificacion persistente durante sesion activa.
- Integracion con Health Connect (pasos).
- Selector de tema (sistema / claro / oscuro).
- Demo data para primeros pasos.
- Soporte para kg y lb.

### Pendiente antes de publicar

Ver seccion 5 (Estabilizacion).

---

## 4. FitTrackPlus Personal

### Audiencia

Personas que entrenan solas en el gimnasio y quieren llevar un registro ordenado sin complicaciones.

### Promesa principal

> Crea tu rutina, registra tus entrenamientos, revisa tu progreso. Sin hojas de calculo, sin complicaciones.

### Criterio de publicacion

La aplicacion puede publicarse cuando un usuario puede completar este flujo sin errores bloqueantes:

1. Instalar la aplicacion.
2. Crear una rutina.
3. Definir dias y ejercicios.
4. Registrar un entrenamiento.
5. Guardar peso y repeticiones por serie.
6. Ver el entrenamiento en el historial.
7. Editar la rutina sin que cambie el historial anterior.
8. Volver a usar la aplicacion en el siguiente entrenamiento.

### Modelo de negocio

- **Gratuita** en la primera version: el objetivo es conseguir usuarios, valoraciones y metricas reales.
- **Premium** en el futuro: estadisticas avanzadas, backup, sincronizacion multidispositivo, exportacion, mas personalizacion, programas predefinidos, IA.

No desarrollar premium antes de comprobar que funciones valoran realmente los usuarios.

### Metrica clave

> Porcentaje de usuarios que registra un segundo entrenamiento en los primeros 7 dias.

Esto indica que la aplicacion empieza a integrarse en el habito del usuario, no solo que se instalo.

Otras metricas de interes:

- Usuarios que crean una rutina.
- Usuarios que registran su primer entrenamiento.
- Pantallas donde se abandona el flujo.
- Sesiones completadas por usuario.
- Retencion a los 7 dias.

---

## 5. Estabilizacion (antes de publicar)

### Clasificacion de errores

#### Bloqueantes — deben resolverse antes de la prueba cerrada

Son los problemas que impiden que el producto pueda usarse:

- La aplicacion se cierra inesperadamente.
- Se pierden entrenamientos o rutinas.
- Los datos no se guardan correctamente.
- Un campo no permite escribir.
- El teclado se abre y se cierra automaticamente.
- No se puede editar una rutina.
- No se puede registrar un entrenamiento.
- Un boton principal no funciona.
- Una pantalla no permite volver atras.
- La navegacion bloquea al usuario.
- Los datos se duplican o quedan corruptos.
- Una modificacion rompe el historial.

#### Importantes de experiencia — idealmente resueltos antes de publicar

No bloquean el uso pero perjudican la calidad percibida:

- Menus dificiles de cerrar.
- Pantallas visualmente incoherentes.
- Botones importantes poco visibles.
- Falta de mensajes de confirmacion.
- Formularios confusos.
- Estados vacios sin explicacion.
- Acciones cuyo resultado no queda claro.
- Navegacion poco intuitiva.

#### Mejoras futuras — no bloquean v1

Animaciones avanzadas, IA, nutricion, funciones sociales, estadisticas complejas, integracion con relojes, iOS, tablets, gamificacion, marketplace de rutinas.

### Criterio para congelar nuevas funcionalidades

Una funcionalidad nueva solo justifica desarrollo si cumple al menos una de estas condiciones:

- Bloquea el flujo principal.
- Mejora una funcion muy utilizada.
- Ha sido pedida por usuarios reales.
- Facilita la publicacion o conseguir testers.
- Facilita vender a entrenadores.
- Permite cobrar.
- Reduce un riesgo importante.
- Es necesaria para un piloto.

---

## 6. Prueba cerrada en Google Play

Google Play exige una prueba cerrada con al menos 12 testers activos durante 14 dias para ciertas cuentas nuevas.

Objetivo: conseguir entre 18 y 25 testers para tener margen.

### Donde encontrar testers

- Familiares y amigos que entrenen.
- Contactos del gimnasio.
- Companeros desarrolladores.
- Comunidades online de Android y desarrollo indie.
- Grupos de intercambio de testing.
- Foros y discords sobre fitness y tecnologia.

### Que deben hacer los testers

No es necesario que entrenen todos los dias. El objetivo es que:

1. Instalen la aplicacion.
2. Creen una cuenta.
3. Creen una rutina y añadan ejercicios.
4. Registren al menos un entrenamiento.
5. Consulten el historial.
6. Informen de errores o elementos confusos.

Cada tester puede centrarse en un area (rutinas, registro, historial, diseño, navegacion, accesibilidad).

---

## 7. Publicacion en Android

### Orden de lanzamiento

1. Android para telefonos (prioridad).
2. Prueba cerrada.
3. Publicacion en Google Play.
4. Metricas y feedback real.
5. Mejoras basadas en uso real.
6. Adaptacion para tablets si hay demanda.
7. iOS si hay demanda suficiente.

No se desarrollara iOS inicialmente porque implica un proyecto separado con tecnologias, procesos y publicacion diferentes.

---

## 8. FitTrackPlus Coach

### El problema del entrenador personal

La mayoria de entrenadores gestionan a sus clientes con una combinacion de hojas de calculo, PDF, WhatsApp, notas y formularios. La informacion esta dispersa y el entrenador no sabe con facilidad si el cliente ha entrenado, que peso ha usado o como ha progresado.

### La propuesta

> Gestiona las rutinas y el progreso de todos tus clientes desde un unico panel, mientras cada alumno registra sus entrenamientos desde el movil.

### Como funciona

**Entrenador** — panel web responsive (ordenador, tablet, movil):

- Crear clientes e invitarles a la app.
- Crear rutinas y asignarlas a clientes.
- Reutilizar plantillas entre clientes.
- Consultar sesiones completadas: series, peso, repeticiones.
- Comparar lo planificado con lo realizado.
- Dejar comentarios y ajustar la programacion.

**Cliente** — app movil Android:

- Ver la rutina asignada por el entrenador.
- Registrar el entrenamiento (igual que en FitTrackPlus Personal).
- Leer comentarios del entrenador.
- Consultar su historial y progreso.
- Continuar usando la app si cambia de entrenador.

### Diferenciacion

- **Simplicidad**: facil de configurar y usar, sin curva de aprendizaje larga.
- **Especializacion**: fuerza, hipertrofia y gimnasio. No intenta ser todo para todos.
- **Registro detallado**: series, peso, repeticiones, rangos, volumen, sesiones anteriores.
- **Mercado espanol**: interfaz en espanol, precio en euros, atencion cercana, pensado para entrenadores independientes.
- **Continuidad del alumno**: el historial del cliente le pertenece. Puede seguir usando la app aunque cambie de entrenador.

### Modelo de negocio

Suscripcion mensual o anual para el entrenador, segun numero de clientes activos.

| Plan | Clientes | Precio orientativo |
|---|---|---|
| Inicio | 2-3 | Gratuito o muy reducido |
| Coach | Hasta 10 | 19-29 €/mes |
| Coach Pro | Hasta 30 | 49-69 €/mes |
| Studio | Varios entrenadores | Precio adaptado |

Estos precios son hipotesis iniciales. Deben validarse con entrenadores reales antes de fijarlos.

### Por que el modelo Coach es interesante

Cada entrenador incorpora directamente varios clientes a la plataforma. Un entrenador con 20 alumnos aporta 20 usuarios nuevos. Al terminar la relacion, el alumno puede seguir usando FitTrackPlus Personal, lo que mantiene la retencion.

### MVP de Coach

La primera version para entrenadores debe demostrar una sola cosa:

> El entrenador asigna una rutina y puede comprobar que ha realizado realmente el cliente.

Funcionalidades minimas:

- Entrenador puede crear clientes e invitarles.
- Entrenador puede crear rutinas y asignarlas.
- Entrenador puede ver sesiones completadas con detalle.
- Cliente puede ver su rutina asignada y registrar el entrenamiento.
- Cliente puede ver comentarios del entrenador.

Lo que queda fuera del MVP: automatizaciones, alertas avanzadas, nutricion, mensajeria en tiempo real, videollamadas, pagos, reservas.

### Estados de entrenamiento en Coach

Cada sesion puede tener uno de estos estados visibles para el entrenador:

- Pendiente
- Iniciada
- Completada
- Completada parcialmente
- Omitida
- Revisada por el entrenador

### Modelo de datos y privacidad

- El alumno es propietario de su historial.
- El entrenador accede solo a lo necesario durante la relacion profesional.
- El alumno puede finalizar la relacion, revocar el acceso y conservar su historial.
- Las rutinas se clasifican por origen: personal, asignada por entrenador, copiada, archivada.

---

## 9. Evolucion futura

Tras validar Personal y Coach:

- Estadisticas avanzadas y graficas de progresion.
- Volumen por grupo muscular y estimacion de 1RM.
- Inteligencia artificial (progresion, deteccion de estancamiento).
- Nutricion y seguimiento corporal (complejidad alta, no antes de validar core).
- Mensajeria entrenador-cliente.
- Notificaciones inteligentes.
- Integracion con wearables y Apple Health.
- iOS si hay demanda suficiente.
- Panel para gimnasios y gestion de equipos de entrenadores.
- Marketplace de programas y rutinas predefinidas.

---

## 10. Roadmap de fases

### Fase 1 — Estabilizacion (estado actual)

- Detectar y clasificar errores con uso personal real.
- Corregir todos los bloqueantes.
- Corregir los problemas de UX mas graves.
- Congelar nuevas funcionalidades.
- Verificar persistencia de datos y coherencia del historial.

### Fase 2 — Prueba cerrada

- Crear la cuenta de desarrollador en Google Play.
- Preparar la ficha de la aplicacion (capturas, descripcion, icono).
- Conseguir entre 18 y 25 testers reales.
- Publicar la prueba cerrada.
- Recoger feedback y corregir problemas criticos.

### Fase 3 — Publicacion Personal

- Completar los requisitos de Google Play.
- Publicar FitTrackPlus Personal.
- Incorporar analitica de uso.
- Registrar errores en produccion.
- Obtener primeras valoraciones y metricas.

### Fase 4 — Validacion Coach

- Hablar con 5-10 entrenadores personales reales.
- Presentar la aplicacion y una maqueta del panel.
- Conocer sus herramientas actuales y sus problemas principales.
- Identificar que funciones pagarian.
- Buscar un entrenador piloto dispuesto a probar la plataforma.

### Fase 5 — MVP Coach

- Crear roles de entrenador y cliente.
- Implementar la relacion entrenador-alumno.
- Permitir asignacion de rutinas desde el panel.
- Sincronizar los entrenamientos del alumno con el panel.
- Mostrar resultados y permitir comentarios.
- Probar con un entrenador piloto y pocos clientes.

### Fase 6 — Monetizacion

- Definir los planes de suscripcion definitivos.
- Cobrar a los primeros entrenadores.
- Medir uso, retencion y permanencia.
- Iterar sobre las funciones mas usadas.
- Ampliar progresivamente.

---

## 11. Principio de trabajo

> No todo el desarrollo tecnico es productividad falsa.  
> Corregir un error que impide escribir, guardar datos o completar un entrenamiento es trabajo necesario.

La productividad falsa aparece cuando se usan mejoras secundarias para evitar publicar, mostrar la app o validar el producto con usuarios reales.

El criterio para cualquier tarea nueva: **¿esto acerca la app a estar en manos de usuarios reales, o lo retrasa?**
