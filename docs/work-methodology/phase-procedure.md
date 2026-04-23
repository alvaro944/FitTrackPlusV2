# Procedimiento Por Fases

Este procedimiento describe como avanzar una fase de FitTrackPlus v2 sin mezclar alcance, manteniendo aprendizaje y trazabilidad.

## 1. Abrir Fase

1. Leer documentacion base:
   - `README.md`
   - `AGENTS.md`
   - `docs/development-workflow.md`
   - `docs/project-plan.md`
   - `docs/project-progress.md`
   - `docs/phase-log.md`
   - `docs/architecture.md`
   - `CLAUDE.md` o documentos equivalentes de coordinacion, si existen
2. Revisar el area concreta que se va a tocar.
3. Confirmar rama actual y estado de Git.
4. Si vienes de otra plataforma o editor, releer tambien:
   - archivos tocados en la iteracion anterior
   - backlog o notas externas que esten actuando como contexto
   - cualquier regla operativa nueva dejada en `docs/work-methodology/` o `CLAUDE.md`
5. Crear rama de fase:

```powershell
git switch -c codex/phase-nombre
```

## 2. Desarrollar

1. Tocar solo el alcance de la fase.
2. Mantener Compose como capa de UI:
   - pinta estado
   - envia eventos
   - no contiene logica de negocio
3. Mantener ViewModels con:
   - `UiState`
   - `StateFlow`
   - funciones de evento claras
4. Usar repositorios para ocultar Room/DataStore.
5. Evitar refactors no necesarios.
6. Si se necesitan datos demo, hacerlos controlados:
   - solo debug
   - solo base vacia
   - sin Firebase ni sync
7. En fases de UX funcional, separar pulido minimo de redisenio visual:
   - mejorar textos, estados, confirmaciones y accesibilidad basica
   - no rehacer tema, navegacion ni componentes si pertenecen a una fase visual
8. Verificar pronto con compilacion o tests cuando aparezcan cambios relevantes.
9. En fases visuales, bajar primero el diseno a tokens y componentes compartidos antes de reescribir pantallas:
   - tema
   - tipografia
   - superficies
   - estados reutilizables
   - navegacion
10. Si hay varias herramientas proponiendo cambios:
   - revisar primero que es propuesta y que es decision ya validada
   - elegir un solo agente como ejecutor del cambio
   - anotar en metodologia cualquier regla nueva de coordinacion que haya funcionado
11. Si se alternan plataformas:
   - no asumir que la memoria de una conversacion refleja el estado real actual
   - usar `git status` y la documentacion viva como punto de resincronizacion
   - evitar editar la misma zona sin una nueva lectura del codigo ya modificado

## 3. Verificar

Para cambios de codigo:

```powershell
.\gradlew.bat test
.\gradlew.bat build --no-daemon --console=plain
```

Si Gradle queda atascado o deja daemons vivos:

```powershell
.\gradlew.bat --stop
```

Para cambios visibles de UI:

- Probar en emulador o dispositivo si esta disponible.
- Si no se puede probar manualmente, dejarlo anotado como pendiente.
- Si la fase nace de una referencia externa de diseno, validar la app contra el comportamiento real y no contra el HTML/JSX original.

## 4. Cerrar Fase

Antes de cerrar:

1. Revisar `git status`.
2. Revisar archivos modificados.
3. Confirmar que tests/build pasan.
4. Actualizar:
   - `README.md` si cambia el estado general
   - `docs/project-plan.md` si cambia el estado de fases
   - `docs/project-progress.md` con avances y siguiente paso
   - `docs/phase-log.md` con cambios, problemas, decisiones y verificacion
   - `docs/work-methodology/` con aprendizajes del proceso
5. Comentar al usuario que se hizo y que queda pendiente.
6. Hacer commit local de cierre.

## Checklist De Cierre

- [ ] Alcance de fase implementado.
- [ ] Fuera de alcance respetado.
- [ ] Tests ejecutados.
- [ ] Build ejecutado.
- [ ] Prueba manual hecha o pendiente anotado.
- [ ] Progreso actualizado.
- [ ] Bitacora actualizada.
- [ ] Guia de metodologia actualizada.
- [ ] Commit local creado.
