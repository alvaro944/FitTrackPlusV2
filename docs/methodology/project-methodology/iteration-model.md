# Iteration Model

Este documento describe el ciclo de una iteracion reusable entre proyectos.

## Dos Niveles De Iteracion

### Modo Ligero

Para cambios pequenos y de bajo riesgo.

Usarlo cuando:

- el cambio afecta pocos archivos
- el alcance es claro
- no cambia arquitectura, producto ni flujo critico
- la verificacion puede ser proporcional y rapida

Requiere:

- leer el area afectada
- hacer el cambio acotado
- verificar de forma proporcional
- resumir resultado y pendiente

No requiere actualizar metodologia general salvo que aparezca una regla reusable.

### Modo Fase

Para cambios amplios, cross-cutting o de producto.

Usarlo cuando:

- el cambio afecta varias zonas o capas
- hay decisiones de producto, arquitectura o flujo
- requiere coordinacion entre agentes o herramientas
- necesita actualizar progreso, bitacora o roadmap
- deja pendientes que otra sesion podria continuar

Requiere:

- revisar contexto base
- acotar alcance y fuera de alcance
- implementar con verificacion
- actualizar progreso y bitacora
- dejar handoff si otra herramienta puede continuar

## Cambio De Modo Durante Una Iteracion

Un cambio ligero debe pasar a modo fase si:

- aparece riesgo transversal
- el alcance empieza a crecer
- hace falta tocar zonas no previstas
- entra otro ejecutor
- la verificacion deja de ser simple

Una fase puede dividirse en iteraciones ligeras si:

- cada parte tiene ownership claro
- cada parte puede verificarse de forma independiente
- el integrador mantiene la vision de cierre

## 1. Abrir

Antes de tocar nada:

- leer contexto base
- revisar estado real del repo
- confirmar el alcance
- distinguir contexto, backlog y decisiones ya tomadas

## 2. Acotar

Definir:

- que entra
- que no entra
- que riesgo tiene
- que verificacion exigira

Regla practica:

- una iteracion = una responsabilidad principal

## 3. Implementar

- tocar solo el alcance comprometido
- verificar pronto si el cambio afecta a compilacion o tooling
- evitar arreglar de pasada lo que no pertenece a la iteracion
- si el cambio es visual, bajar primero la idea a jerarquia, tokens y componentes

## 4. Verificar

- ejecutar verificacion proporcional al cambio
- si el proyecto tiene tooling fragil, usar el patron estable
- si la validacion manual no puede hacerse, dejarlo anotado

## 5. Documentar

- actualizar estado actual
- registrar cambios, decisiones y problemas
- guardar patrones reutilizables solo si son generales y repetibles

## 6. Cerrar

Una iteracion esta bien cerrada si:

- el alcance existe
- lo fuera de alcance se ha respetado
- la verificacion esta hecha o explicitamente pendiente
- la documentacion ya refleja el nuevo estado
- el siguiente paso esta claro

## Regla De Documentacion

No todo cierre actualiza la metodologia general.

- estado del proyecto -> progreso
- historia de la iteracion -> bitacora
- aprendizaje especifico -> docs del repo
- regla reusable entre proyectos -> metodologia general

Regla practica:

- modo ligero normalmente no actualiza progreso ni bitacora salvo que cambie el estado real
- modo fase normalmente actualiza progreso y bitacora
- ninguno actualiza metodologia general por rutina
