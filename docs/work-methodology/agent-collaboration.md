# Colaboracion Claude / Codex

Referencia canonica del modelo de trabajo. Leer antes de ejecutar cualquier cosa.

## El modelo en una frase

Claude disena y especifica. Codex implementa y verifica. El usuario decide y aprueba.

---

## Que hace Claude

- Lee el repo, los docs y el estado actual
- Analiza opciones y propone
- Escribe specs de diseno en `docs/superpowers/specs/`
- Escribe planes de implementacion en `docs/superpowers/plans/`
- Revisa codigo ya escrito por Codex

Claude NO escribe codigo de produccion salvo encargo explicito del usuario.
Claude NO hace commits, no crea ramas, no hace push.

## Que hace Codex

- Lee la spec y el plan antes de empezar
- Crea la rama indicada en el plan
- Implementa segun la spec
- Verifica localmente: `./gradlew test` + `./gradlew build`
- Hace pasada manual en emulador cuando es posible
- Hace commit solo cuando el codigo esta verificado y funciona
- Hace push y avisa al usuario

Codex NO hace commits de WIP.
Codex NO cambia de rama sin motivo.
Codex NO amplia el alcance de la spec sin confirmar con el usuario.

---

## Estrategia de ramas

- Una rama por grupo de mejoras relacionadas: `codex/<nombre-descriptivo>`
- Todo el trabajo del grupo va en esa rama hasta que esta completo
- No se crean ramas por micro-cambios o fixes puntuales
- El merge a main ocurre cuando el grupo entero esta verificado

## Disciplina de commits

- Solo se hace commit cuando el codigo compila, los tests pasan y el flujo manual esta ok
- Un commit = una unidad logica completa, no un estado intermedio
- El mensaje describe que se hizo, no que se intento
- Los schemas de Room se commitean junto con los cambios de entidad

---

## Flujo completo de una mejora

```
1. Claude lee el estado del repo y los docs
2. Claude escribe spec en docs/superpowers/specs/<fecha>-<nombre>.md
3. Claude escribe plan en docs/superpowers/plans/<fecha>-<nombre>.md
4. Usuario revisa y aprueba
5. Codex crea rama: codex/<nombre>
6. Codex implementa segun spec y plan
7. Codex verifica localmente (test + build + emulador)
8. Codex hace commit limpio
9. Codex hace push y avisa
10. Usuario revisa en emulador
11. Usuario aprueba merge a main
```

---

## Al cambiar de sesion o plataforma

Antes de continuar cualquier trabajo:

1. `git status` — ver estado real del repo
2. `git log --oneline -5` — ver ultimos commits
3. Releer la spec y el plan de la mejora en curso
4. Abrir los archivos concretos que se van a tocar

No continuar desde memoria conversacional sin contrastar con el repo.

---

## Cuando hay duda

Si el alcance no esta claro, preguntar al usuario antes de implementar.
Si algo en la spec parece incompleto o contradictorio, avisar antes de inventar.
Si aparece un bug o mejora fuera de alcance, anotarlo y continuar — no arreglarlo de pasada.
