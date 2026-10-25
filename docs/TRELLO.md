# Planificación del proyecto (Trello)

Tablero sugerido con 4 listas: **Backlog**, **En progreso**, **En revisión**, **Hecho**.
Cada tarjeta tiene responsable (I1 = Integrante 1, I2 = Integrante 2) y se cierra con el commit que la implementa.

| # | Tarjeta | Responsable | Etiqueta | Commit asociado |
|---|---|---|---|---|
| 1 | Definir caso Kaiju, requisitos y reglas de stock | I1 + I2 | Análisis | docs: README inicial |
| 2 | Configurar proyecto Compose, dependencias y navegación | I2 | Configuración | build: dependencias |
| 3 | Modelos Producto, Movimiento y TipoMovimiento | I1 | Modelo | feat(model) |
| 4 | Base de datos SQLite con tablas productos y movimientos | I1 | Persistencia | feat(data) |
| 5 | Repositorio con StateFlow como fuente única de verdad | I1 | Arquitectura | feat(repository) |
| 6 | Validadores de producto y movimiento + tests unitarios | I2 | Validación | feat(validation) / test |
| 7 | ViewModels (inventario, formulario, movimiento) | I1 | Estado | feat(viewmodel) |
| 8 | Tema Material 3 con paleta Kaiju | I2 | Diseño | style(theme) |
| 9 | Componentes reutilizables (CampoValidado, StockChip, FotoProducto) | I2 | UI | feat(ui) components |
| 10 | Pantallas Inicio, Productos y Movimientos + barra inferior | I2 | UI | feat(ui) screens |
| 11 | Formulario de producto con validación visual | I2 | Formularios | feat(ui) formulario |
| 12 | Registro de movimientos con vista previa de stock | I1 | Formularios | feat(ui) movimiento |
| 13 | Cámara y galería para foto del producto | I2 | Recursos nativos | feat(nativo) imagen |
| 14 | Notificaciones de stock bajo y vibración | I1 | Recursos nativos | feat(nativo) notificaciones |
| 15 | Animaciones de transición y retroalimentación | I2 | Animaciones | feat(ui) animaciones |
| 16 | Pruebas en emulador y dispositivo, corrección de errores | I1 + I2 | QA | fix |
| 17 | README final con integrantes y pasos de ejecución | I1 | Documentación | docs |

Importar rápido: crear las tarjetas copiando la columna "Tarjeta", asignar miembros y mover a "Hecho" a medida que se suben los commits.
