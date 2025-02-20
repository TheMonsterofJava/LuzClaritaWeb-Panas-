/*Insertar Roles*/
/*Permisos o Roles*/
/*Tabla de Permisos*/
INSERT INTO `permisos` (nombre, descripcion) VALUES ('Programador', 'Permiso mas alto, puede modificar todo'),('Administrador', 'Dueño de la empresa - Tiene menos permisos que programador'),('Operador', 'Trabajador de la empresa - Tiene algunos permisos'),('Cliente', 'Usuario Registrado que realiza una compra'),('Unloged', 'Consultas algunos elementos de la pagina sin estar registrado');

/*Crear Usuarios*/
INSERT INTO `usuarios` (activo, fecha_creacion, id_permiso, nomb_usu, clave, email) VALUES (1, '2024-12-01', 1, 'Osvaldo', '$2y$10$ZP7gE3toR10ffABvlsvJXOGQsHaUQPizzXpuBZj5Gkf.7mT3hFmQS', 'Osvaldo@gmail.com.ar') /*user*/
INSERT INTO `usuarios` (activo, fecha_creacion, id_permiso, nomb_usu, clave, email) VALUES (1, '2024-12-01', 4, 'Santi', '$2y$10$Y5PE96MBydWiAVCAkf1TQu99d7NKYNPWU/HEAZGQ2l5OQu2XiPiiC', 'Santi@gmail.com.ar') /*cliente*/

/* Crear las categorias */
INSERT INTO `luzclaritaweb`.`categorias` (`nombre`) VALUES ('Chocolate');
INSERT INTO `luzclaritaweb`.`categorias` (`nombre`) VALUES ('Frutilla');
INSERT INTO `luzclaritaweb`.`categorias` (`nombre`) VALUES ('Vainilla');
INSERT INTO `luzclaritaweb`.`categorias` (`nombre`) VALUES ('Donas');

/* Crear los productos */
INSERT INTO `luzclaritaweb`.`productos` (`activo`, `precio`, `stock`, `id_categoria`, `descripcion`, `lnk_img`) VALUES ('1', '5000', '6', '2', 'Torta Spiderman', 'https://donolli.com.ar/wp-content/uploads/2023/01/20221015_122724-scaled.jpg');
INSERT INTO `luzclaritaweb`.`productos` (`activo`) VALUES ('1');
INSERT INTO `luzclaritaweb`.`productos` (`precio`, `stock`, `id_categoria`, `descripcion`, `lnk_img`) VALUES ('6840', '10', '1', 'Torta chocolate', 'https://i.ytimg.com/vi/H7uMpjzyaTU/maxresdefault.jpg');
INSERT INTO `luzclaritaweb`.`productos` (`activo`, `precio`, `stock`, `id_categoria`, `descripcion`, `lnk_img`) VALUES ('1', '4500', '20', '4', 'Donas glaseadas', 'https://64.media.tumblr.com/155ba7881a89c4b5a70ad4466e8d86f5/tumblr_nuvqnoaaEe1tf311io1_1280.jpg');

/*Crear articulos del inventario*/
INSERT INTO `luzclaritaweb`.`inventario` (`id`, `cantidad`, `fecha_ingreso`, `fecha_vencimiento`, `nombre_ingrediente`, `precio`, `unidad_medida`, `id_proveedor`, `id_usuario`, `activo`) VALUES ('1', '4', '2024-07-04', '2024-04-21', 'harina ', '1500', '1kg', '1', '1', '1');
INSERT INTO `luzclaritaweb`.`inventario` (`id`, `cantidad`, `fecha_ingreso`, `fecha_vencimiento`, `nombre_ingrediente`, `precio`, `unidad_medida`, `id_proveedor`, `id_usuario`, `activo`) VALUES ('2', '7', '2025-04-06', '2024-07-24', 'azucar', '1000', '1kg', '2', '2', '1');
INSERT INTO `luzclaritaweb`.`inventario` (`id`, `cantidad`, `fecha_ingreso`, `fecha_vencimiento`, `nombre_ingrediente`, `precio`, `unidad_medida`, `id_proveedor`, `id_usuario`, `activo`) VALUES ('3', '5', '2024-09-03', '2024-02-23', 'crema de leche', '3500', '500 ml', '1', '1', '1');
INSERT INTO `luzclaritaweb`.`inventario` (`activo`) VALUES ('1');

/*crear proveedores*/
INSERT INTO `luzclaritaweb`.`proveedores` (`id`, `contacto`, `direccion`, `email`, `nombre`, `telefono`, `id_compra`, `activo`) VALUES ('1', '3624083784', 'mitre 154', 'elias@gmail..com', 'elias', '3624083784', '1', '1');
INSERT INTO `luzclaritaweb`.`proveedores` (`id`, `contacto`, `direccion`, `email`, `nombre`, `telefono`, `id_compra`, `activo`) VALUES ('2', '3624038475', 'colon 444', 'marcos3947@gmail.com', 'marcos', '3624038475', '2', '1');
INSERT INTO `luzclaritaweb`.`proveedores` (`id`, `contacto`, `direccion`, `email`, `nombre`, `telefono`, `id_compra`, `activo`) VALUES ('3', '3624034856', 'ameguino 489', 'pedro@gmail.com', 'pedro', '3624034856', '3', '1');
INSERT INTO `luzclaritaweb`.`proveedores` (`activo`) VALUES ('1');

/*crear compras*/
(1, 'Compra de prueba 1', '2024-01-01', 1000),
(2, 'Compra de prueba 2', '2024-02-01', 2000),
(3, 'Compra de prueba 3', '2024-03-01', 3000);
