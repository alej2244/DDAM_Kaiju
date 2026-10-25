# Kaiju Inventario

Aplicación Android para la **gestión y trazabilidad de inventario de Kaiju** (caso DSY1105 · Desarrollo de Aplicaciones Móviles, Duoc UC).

Kaiju controlaba su stock con planillas y registros manuales, lo que generaba diferencias entre el stock registrado y el real, olvidos al registrar entradas o salidas y poca visibilidad de los productos por reponer. Esta app reemplaza ese proceso: cada producto tiene su stock, su stock mínimo y un historial de movimientos, y la app avisa cuando algo necesita reposición.

**Integrante:** Alejandro Naranjo · Sección DSY1105 · Grupo 13

## Funcionalidades implementadas

- **Panel de inicio**: total de productos, unidades y valor del inventario (contadores animados), productos que requieren reposición y ranking de más vendidos.
- **Catálogo de productos**: búsqueda por nombre o código, filtros por categoría y por stock bajo, foto y estado de stock con color.
- **Formulario de producto (crear/editar)** con validación por campo en tiempo real: código (formato y unicidad), nombre, descripción, categoría, precio, stock inicial y stock mínimo. Cada campo muestra ícono, borde rojo, ícono de error o de válido y mensaje.
- **Registro de movimientos**: entrada, venta, salida (merma/consumo) y ajuste por conteo físico. Valida que no se venda más que el stock disponible y que el ajuste tenga motivo, y muestra una vista previa del stock resultante.
- **Trazabilidad**: historial por producto y historial global filtrable por tipo. El stock solo cambia mediante movimientos (en edición el campo stock está bloqueado).
- **Persistencia local** con SQLite: los datos se mantienen al cerrar la app. Incluye un catálogo ficticio inicial.
- **Recursos nativos**:
  1. **Cámara** (permiso `CAMERA` solicitado en tiempo de ejecución) para fotografiar el producto.
  2. **Galería** mediante el selector de fotos del sistema (no requiere permiso).
  3. **Notificaciones** del sistema cuando un producto queda en stock bajo (permiso `POST_NOTIFICATIONS` en Android 13+).
  4. **Vibración** como confirmación al registrar un movimiento.
- **Animaciones**: transiciones entre pantallas, barra inferior que aparece/desaparece, contadores animados, color y número del stock animados, aparición/reordenamiento de elementos de listas, mensajes de error que se despliegan, sacudida del formulario al guardar con errores, botón que se encoge al presionarlo con indicador de carga, vista previa del stock con transición.

## Arquitectura (MVVM)

```
com.example.kaijuinventory
├── KaijuApp.kt              # Application: crea el repositorio y el canal de notificaciones
├── MainActivity.kt          # Única Activity, pide permiso de notificaciones y monta la navegación
├── model/                   # Producto, Movimiento, TipoMovimiento, Categorias
├── data/                    # KaijuDatabase (SQLiteOpenHelper) y DatosIniciales
├── repository/              # InventarioRepository: única fuente de verdad, expone StateFlow
├── validation/              # ProductoValidator y MovimientoValidator (reglas sin UI, con tests)
├── viewmodel/               # InventarioViewModel, ProductoFormViewModel, MovimientoViewModel
├── nativo/                  # ImagenStorage (cámara/galería), Notificaciones, Vibracion
├── util/                    # Formato de pesos chilenos y fechas
└── ui/
    ├── navigation/          # AppNavigation: NavHost, rutas y barra inferior
    ├── screens/             # Inicio, Productos, Formulario, Detalle, Movimiento, Movimientos
    ├── components/          # CampoValidado, StockChip, FotoProducto, FilaMovimiento...
    └── theme/               # Tema Material 3 con paleta Kaiju
```

- **View (Composables)**: solo dibujan el estado y envían eventos. No contienen reglas de negocio.
- **ViewModel**: guarda el estado de cada pantalla en `StateFlow`, valida usando `validation/` y llama al repositorio.
- **Repository**: oculta SQLite; después de cada escritura refresca los `StateFlow` y la UI se actualiza sola.
- **Model**: clases de datos del dominio.

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow · Corrutinas · SQLite (SQLiteOpenHelper) · Activity Result API.

## Pasos para ejecutar

1. Instalar **Android Studio** (versión reciente con soporte para AGP 9.4) y un emulador con Android 7.0 (API 24) o superior. Para probar la cámara usar un dispositivo físico o un emulador con cámara virtual.
2. Clonar el repositorio:
   ```bash
   git clone https://github.com/alej2244/DDAM_Kaiju.git
   ```
3. Abrir la carpeta del proyecto en Android Studio (`File > Open`) y esperar la sincronización de Gradle (Android Studio crea `local.properties` con la ruta del SDK).
4. Seleccionar el emulador o dispositivo y presionar **Run ▶** (módulo `app`).
5. Al abrir, aceptar el permiso de notificaciones. El permiso de cámara se pide al tocar "Tomar foto".

Tests unitarios de las validaciones:

```bash
./gradlew testDebugUnitTest
```