# Work Methodology

Esta carpeta deja de ser la fuente canonica de metodologia general.

Ahora el starter pack reusable vive en:

- `docs/project-methodology/README.md`

## Para Que Queda Esta Carpeta

Como apoyo del repo actual:

- aprendizajes historicos
- notas de arquitectura nacidas en este proyecto
- patrones tecnicos observados durante la ejecucion real

## Regla Practica

- si buscas metodologia portable entre proyectos -> ir a `docs/project-methodology/`
- si buscas aprendizajes concretos de este repo -> usar `docs/work-methodology/`

## Aprendizajes Del Repo

- En Windows, KSP/Hilt puede dejar archivos de `app/build/generated/ksp/` bloqueados por procesos Java/Kotlin tras builds largos o fallidos. Si aparece `NoSuchFileException`, `FileAlreadyExistsException` o `FileNotFoundException` sobre salidas generadas, parar daemons/procesos Java bloqueantes y repetir con `.\gradlew.bat clean test --no-daemon --console=plain` antes de relanzar `build`.
