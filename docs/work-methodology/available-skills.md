# Skills Disponibles En Claude Code

Registro de skills instaladas a nivel usuario (`~/.claude/plugins/`) para su uso en sesiones de Claude Code en este proyecto.

## Como Usar

Invocar mediante el tool `Skill` o escribiendo `/<nombre-skill>` en la conversacion.
Las skills se cargan en el momento de invocacion; no es necesario recordar su contenido de memoria.

---

## Humanizer
**Origen:** `blader/humanizer`  
**Skill:** `humanizer`

Suaviza y naturaliza el tono de respuestas o textos generados. Util para pulir mensajes de usuario en la app, textos de onboarding o cualquier cadena visible en UI.

---

## Napkin
**Origen:** `blader/napkin`  
**Skill:** `napkin`

Genera esquemas o diagramas rapidos de arquitectura y flujos. Util para visualizar la relacion entre capas (domain, data, feature) antes de implementar.

---

## Skill Forge
**Origen:** `AgriciDaniel/skill-forge`  
**Skill:** `skill-forge`

Herramienta para crear nuevas skills personalizadas. Usar cuando se quiera codificar un proceso de trabajo repetitivo del proyecto como skill reutilizable.

---

## Caveman (suite)
**Origen:** `juliusbrussee/caveman`

| Skill | Uso |
|---|---|
| `caveman` | Simplifica explicaciones tecnicas usando lenguaje basico. Util para documentar decisiones de arquitectura de forma accesible. |
| `caveman-commit` | Genera mensajes de commit en formato directo y claro. |
| `caveman-help` | Responde preguntas sobre el proyecto en lenguaje simple. |
| `caveman-review` | Revisa codigo con comentarios directos, sin formalismos. |
| `caveman-compress` | Comprime contexto largo en resumen esencial. Util al inicio de sesiones largas o antes de cambiar de rama. |

---

## Token Optimizer (suite)
**Origen:** `alexgreensh/token-optimizer`

| Skill | Uso |
|---|---|
| `token-optimizer` | Optimiza prompts para reducir consumo de tokens sin perder precision. |
| `token-coach` | Da consejos en tiempo real sobre como escribir instrucciones mas eficientes. |
| `token-dashboard` | Muestra metricas de uso de tokens en la sesion actual. |
| `fleet-auditor` | Audita el consumo de tokens en multiples sesiones o tareas en paralelo. |

---

## Referencia En CLAUDE.md

Ver seccion "Skills disponibles" en `CLAUDE.md` para la lista resumida.
