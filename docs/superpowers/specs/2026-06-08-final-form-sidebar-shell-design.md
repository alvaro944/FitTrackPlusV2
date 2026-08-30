# Final Form Sidebar Shell Design

**Goal:** Aplicar el shell visual de `docs/design/final-form-v2/` a la app Android actual, moviendo `Ajustes` fuera de la bottom bar y centralizando navegación secundaria y preferencias en un drawer lateral.

**Scope**

- Bottom navigation de 5 tabs: `Inicio`, `Rutinas`, `Entrenar`, `Historial`, `Datos`.
- Botón hamburguesa compartido en el shell principal.
- Drawer lateral con:
  - acceso real a `Ajustes`
  - selector de tema real
  - selector de unidad real
  - accesos futuros visibles como `Widget & atajos` y `Exportar datos`
- Reutilizar la lógica existente de tema, unidad y pantalla de ajustes.
- Mantener intactas las reglas de negocio de rutinas, entreno, historial, stats, intro y onboarding.

**Out of scope**

- Exportación real de datos.
- Integración real de widgets o atajos.
- Nuevas reglas de persistencia.
- Cambios de backend, sync o Firebase.
- Reescritura funcional de las features existentes.

**Architecture**

El shell nuevo vivirá en `core/navigation` y `core/design`, no dentro de cada pantalla. `FitTrackPlusNavHost` seguirá siendo el orquestador principal, pero pasará a controlar tanto la bottom bar principal como el drawer lateral y la navegación secundaria hacia `Ajustes`.

La configuración visual del shell se separará en un modelo reutilizable y testeable para evitar lógica duplicada entre navegación inferior, drawer y futuras extensiones del shell.

**Behavior**

- La navegación inferior no mostrará `Ajustes`.
- `Ajustes` se abrirá desde el drawer lateral como ruta real.
- Tema y unidad cambiarán desde el drawer usando la infraestructura existente de `UserPreferencesRepository`.
- Las acciones futuras se mostrarán como visibles pero no implementadas funcionalmente.
- El usuario debe percibir una estructura fiel a la referencia visual sin asumir que las acciones futuras ya existen.

**Files likely touched**

- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/navigation/FitTrackPlusNavHost.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/navigation/Routes.kt`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/*`
- `app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/settings/SettingsScreen.kt`
- pantallas principales para encaje visual con el shell

**Verification**

- `./gradlew test`
- `./gradlew build`
- revisión manual del shell, drawer, tabs y acceso a ajustes

