# Spec: Mejoras UX del editor de rutinas

**Fecha:** 2026-06-10
**Rama:** `codex/ux-improvements`
**Estado:** aprobada

---

## Problema

El editor de rutinas muestra todos los dias y todos los ejercicios expandidos a la vez. Con una rutina de 4 dias, la pantalla se vuelve larga y agobiante. Ademas, al navegar fuera sin guardar no hay confirmacion, y el boton de guardar solo esta arriba.

---

## Comportamiento objetivo

### 1. Dias colapsables

- Cada dia se muestra como cabecera colapsable
- Por defecto todos los dias estan **colapsados** al abrir el editor
- El usuario expande el dia que quiere editar
- Solo un dia puede estar expandido a la vez (accordion) — o libre, segun se vea mejor en prueba real
- La cabecera del dia colapsado muestra: nombre del dia + numero de ejercicios

### 2. Jerarquia visual mas clara

Tres niveles de profundidad con diferenciacion clara:

| Nivel | Elemento | Tratamiento visual |
|---|---|---|
| 1 | Dia | Fondo ligeramente diferenciado, borde lateral de acento, tipografia headline |
| 2 | Ejercicio | Card con fondo surface, borde sutil, separacion clara del dia |
| 3 | Serie / config | Dentro del card del ejercicio, tipografia body |

El objetivo es que de un vistazo se entienda a que nivel pertenece cada elemento.

### 3. Confirmacion al salir con cambios pendientes

- Si el usuario pulsa atras o cambia de pestana con cambios sin guardar, aparece un dialogo
- Texto: "Tienes cambios sin guardar. ¿Quieres descartarlos?"
- Acciones: "Descartar" (sale sin guardar) y "Seguir editando" (cierra el dialogo)
- Si no hay cambios pendientes, la navegacion es directa sin dialogo

### 4. Boton guardar flotante

- Aparece un boton flotante "Guardar" en la parte inferior de la pantalla mientras hay cambios pendientes
- Al guardar, el boton desaparece
- El boton del header puede mantenerse como acceso alternativo

---

## Fuera de alcance

- No se cambia la estructura de datos ni el modelo de la rutina
- No se anade busqueda de ejercicios en esta iteracion
- No se cambia el flujo de creacion de rutina nueva

---

## Criterios de aceptacion

- [ ] Una rutina de 4 dias se abre con todos los dias colapsados
- [ ] Expandir un dia muestra sus ejercicios con jerarquia visual clara
- [ ] Editar un ejercicio y pulsar atras muestra el dialogo de confirmacion
- [ ] Navegar sin cambios no muestra el dialogo
- [ ] El boton flotante aparece al editar y desaparece al guardar
