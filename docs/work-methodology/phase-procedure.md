# Procedimiento Por Iteraciones

Este procedimiento define una forma de abrir, desarrollar, verificar y cerrar trabajo sin depender demasiado del tipo de proyecto.

La palabra "fase" puede significar una fase grande, una feature, un fix o una iteracion corta. Lo importante no es el nombre, sino que el alcance quede bien acotado.

## 1. Abrir

Antes de tocar nada:

1. leer la documentacion base
2. revisar el estado real del repo
3. confirmar el alcance de la iteracion
4. distinguir que es contexto, que es backlog y que es decision ya tomada

Lectura minima recomendada:

- `README.md`
- `AGENTS.md` o equivalente
- `docs/development-workflow.md`
- `docs/project-progress.md`
- `docs/phase-log.md`
- `docs/work-methodology/`
- el area concreta que se va a tocar

Si vienes de otra plataforma o editor, relee tambien:

- cambios recientes
- notas de relevo
- archivos tocados en la iteracion anterior

## 2. Acotar

Antes de implementar, fijar:

- que entra
- que no entra
- que riesgo tiene
- que verificacion exigira

Reglas practicas:

- una iteracion = una responsabilidad principal
- no mezclar feature nueva con refactor transversal si no es imprescindible
- si el cambio tiene partes muy distintas, dividirlo

## 3. Implementar

Durante el desarrollo:

- tocar solo el alcance comprometido
- verificar pronto si el cambio afecta a compilacion, tipado o build
- evitar arreglar "ya que estamos" fuera del objetivo principal
- dejar clara la separacion entre UI, coordinacion y persistencia

Si la iteracion es visual:

- bajar antes el diseno a tokens, jerarquia y componentes reutilizables
- usar referencias externas como direccion visual, no como runtime
- simplificar efectos que encarezcan mantenimiento sin aportar valor real

Si la iteracion viene de varias herramientas:

- elegir un ejecutor principal
- tratar el resto como apoyo
- no editar la misma zona desde dos plataformas sin nueva lectura del estado real

## 4. Verificar

La verificacion debe ser proporcional al riesgo del cambio.

### Minimo

- tests si hay logica afectada
- build si hay cambios de codigo
- comprobacion manual si el cambio es visible o de flujo

### Reglas utiles

- si la toolchain tiene estados fragiles, ejecutar comandos de forma estable y documentar el patron
- si una verificacion manual no puede hacerse, dejar el pendiente por escrito
- si el cambio nace de un prototipo externo, validar contra el producto real y no solo contra la referencia

## 5. Documentar

Antes de cerrar una iteracion:

- actualizar el estado actual del proyecto
- anotar que se hizo y que no
- registrar problemas y decisiones
- guardar patrones reutilizables en metodologia

La documentacion no es solo memoria; tambien evita releer todo el repo en la siguiente sesion.

## 6. Cerrar

Una iteracion se considera bien cerrada cuando:

- el alcance esta implementado
- lo fuera de alcance se ha respetado
- la verificacion relevante esta hecha o anotada como pendiente
- la documentacion ya refleja el nuevo estado
- el siguiente paso esta claro

## Checklist

- [ ] Contexto releido
- [ ] Alcance acotado
- [ ] Implementacion sin deriva
- [ ] Verificacion proporcional ejecutada
- [ ] Estado actualizado
- [ ] Bitacora actualizada
- [ ] Metodologia enriquecida si aparecio un patron reusable
