# Project Adaptation

Esta carpeta no sustituye la documentacion especifica de cada repo. La idea es que sirva como base para crearla rapido y con criterio.

## Lo Que Debe Aportar Cada Proyecto

Para aterrizar esta metodologia a un proyecto concreto hacen falta pocos datos:

- stack y tecnologia principal
- arquitectura concreta o limites de capas
- restricciones importantes
- roadmap o plan activo
- comandos de verificacion
- reglas especiales del repo

## Lo Que Se Genera A Partir De Esa Base

Normalmente:

- `AGENTS.md`
- `README.md` con estado y convenciones
- plan o roadmap del proyecto
- documento de progreso
- bitacora de iteraciones
- notas de arquitectura concretas si hacen falta

## Como Derivar `AGENTS.md`

Para crear un `AGENTS.md` concreto desde esta base:

1. copiar solo las reglas que el agente debe obedecer durante la ejecucion
2. anadir lectura inicial del repo concreto
3. declarar el agente principal por defecto
4. definir como pueden participar otros agentes
5. fijar limites de arquitectura, carpetas o ownership
6. escribir comandos de verificacion reales del proyecto
7. explicar cuando usar modo ligero y modo fase
8. exigir handoff para relevos entre agentes o sesiones
9. indicar donde viven progreso, bitacora, plan y aprendizajes locales
10. repetir que la metodologia general no se actualiza por rutina

`AGENTS.md` debe ser operativo, no enciclopedico. Si una regla no afecta a la ejecucion diaria, probablemente pertenece a otro documento.

## Mezcla Correcta En `AGENTS.md`

`AGENTS.md` mezcla dos capas:

- metodologia reusable: como trabajar, verificar, hacer handoff y coordinar agentes
- proyecto concreto: stack, carpetas, arquitectura, comandos, ramas, restricciones y docs vivas

La metodologia general aporta el criterio.

El proyecto aporta las reglas reales.

Despues del kickoff, el agente debe operar principalmente desde `AGENTS.md`, no desde toda la carpeta metodologica.

## Regla De Adaptacion

La metodologia general no debe llenarse de detalles de stack.

Los detalles de stack deben vivir en:

- `AGENTS.md`
- `architecture.md`
- README del repo
- docs tecnicas especificas

## Resultado Buscado

Que al empezar un proyecto nuevo no haya que reinventar:

- jerarquia de decisiones
- modelo multiagente
- ciclo de iteracion
- sistema de documentacion
- criterios de verificacion

Y solo haya que completar:

- que tecnologia usa
- como esta organizado
- que se esta construyendo
- como se verifica
