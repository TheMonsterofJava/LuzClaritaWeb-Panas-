/*Insertar Roles*/
/*Permisos o Roles*/
/*Tabla de Permisos*/
INSERT INTO `permisos` (nombre, descripcion) VALUES ('ROLE_ADMIN', 'Dueño de la empresa - Tiene menos permisos que programador'),('ROLE_OPERADOR', 'Trabajador de la empresa - Tiene algunos permisos'),('ROLE_CLIENTE', 'Usuario Registrado que realiza una compra'),('ROLE_UNLOGUED', 'Consultas algunos elementos de la pagina sin estar registrado');

/*Crear Usuarios*/
INSERT INTO `usuarios` (activo, fecha_creacion, id_permiso, nomb_usu, clave, email) VALUES (1, '2024-12-01', 1, 'Osvaldo', '$2y$10$ZP7gE3toR10ffABvlsvJXOGQsHaUQPizzXpuBZj5Gkf.7mT3hFmQS', 'Osvaldo@gmail.com.ar') /*user*/
INSERT INTO `usuarios` (activo, fecha_creacion, id_permiso, nomb_usu, clave, email) VALUES (1, '2024-12-01', 3, 'Santi', '$2y$10$Y5PE96MBydWiAVCAkf1TQu99d7NKYNPWU/HEAZGQ2l5OQu2XiPiiC', 'Santi@gmail.com.ar') /*cliente*/





-- CATEGORÍAS
INSERT INTO `LuzClaritaWebOva`.`categorias` (`nombre`) VALUES ('Chocolate');
INSERT INTO `LuzClaritaWebOva`.`categorias` (`nombre`) VALUES ('Frutilla');
INSERT INTO `LuzClaritaWebOva`.`categorias` (`nombre`) VALUES ('Vainilla');
INSERT INTO `LuzClaritaWebOva`.`categorias` (`nombre`) VALUES ('Donas');

-- PRODUCTOS
INSERT INTO `LuzClaritaWebOva`.`productos` (`activo`, `precio`, `stock`, `id_categoria`, `descripcion`, `lnk_img`) VALUES ('1', '1.00', '6', '2', 'Torta Spiderman', 'https://donolli.com.ar/wp-content/uploads/2023/01/20221015_122724-scaled.jpg');
INSERT INTO `LuzClaritaWebOva`.`productos` (`activo`, `precio`, `stock`, `id_categoria`, `descripcion`, `lnk_img`) VALUES ('1', '1.00', '10', '1', 'Torta chocolate', 'https://i.ytimg.com/vi/H7uMpjzyaTU/maxresdefault.jpg');
INSERT INTO `LuzClaritaWebOva`.`productos` (`activo`, `precio`, `stock`, `id_categoria`, `descripcion`, `lnk_img`) VALUES ('1', '1.00', '20', '4', 'Donas glaseadas', 'https://64.media.tumblr.com/155ba7881a89c4b5a70ad4466e8d86f5/tumblr_nuvqnoaaEe1tf311io1_1280.jpg');

-- CAJAS
INSERT INTO `LuzClaritaWebOva`.`caja` (`saldo_final`, `saldo_inicial`, `fecha`, `id_usuario`, `estado`) VALUES ('10000.00', '10000.00', '2025-04-20 00:00:00', 1, 'ABIERTA');
INSERT INTO `LuzClaritaWebOva`.`caja` (`saldo_final`, `saldo_inicial`, `fecha`, `id_usuario`, `estado`) VALUES ('10000.00', '10000.00', '2025-04-20 00:00:00', 2, 'CERRADA');

-- PROVEEDORES
-- INSERT INTO `luzclaritaweb`.`proveedores` (`activo`, `telefono`, `contacto`, `nombre`, `email`, `direccion`) VALUES ('1', '3625052244', '3624400921', 'Osval', 'Osva@gmail.com', 'Mitre 22');
INSERT INTO `LuzClaritaWebOva`.`proveedores` (`activo`, `telefono`, `contacto`, `nombre`, `email`, `direccion`) VALUES ('1', '3624242424', '3624021414', 'Franco', 'Franco@gmail.com', 'san lorenzo200');
INSERT INTO `LuzClaritaWebOva`.`proveedores` (`activo`, `telefono`, `contacto`, `nombre`, `email`, `direccion`) VALUES ('1', '3625052244', '3624400921', 'Osval', 'Osva@gmail.com', 'Mitre 22');
INSERT INTO `LuzClaritaWebOva`.`proveedores` (`activo`, `telefono`, `contacto`, `nombre`, `email`, `direccion`) VALUES ('1', '3684252515', '444464', 'Arcor', 'ArcorSA@gmail.com', 'bs as');
INSERT INTO `LuzClaritaWebOva`.`proveedores` (`activo`, `telefono`, `contacto`, `nombre`, `email`, `direccion`) VALUES ('1', '3624242424', '3624021453', 'ledesma', 'LDESMa@gmail.com', 'san lorenzo200');
INSERT INTO `LuzClaritaWebOva`.`proveedores` (`activo`, `telefono`, `contacto`, `nombre`, `email`, `direccion`) VALUES ('1', '3625052244', '3624503248', 'milka', 'MilkaS.A.@gmail.com', 'Mitre 22');
INSERT INTO `LuzClaritaWebOva`.`proveedores` (`activo`, `telefono`, `contacto`, `nombre`, `email`, `direccion`) VALUES ('1', '3684252515', '444345', 'nestle', 'NestleSA@gmail.com', 'bs as');

-- COMPRAS
INSERT INTO `LuzClaritaWebOva`.`compras` (`activo`, `fecha_hora`, `id_caja`, `id_proveedor`, `id_usuario`, `descripcion`) VALUES ('1', '2025-05-14 00:00:00', 1, 1, 1, 'levadura');


INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '145', '2000', '2024-09-14', '2026-08-21', 1, 1, 'azucar', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '15', '1500', '2025-09-20', '2024-01-16', 2, 2, 'harina', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '10', '1200', '2025-04-10', '2026-03-15', 1, 1, 'sal', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '145', '2500', '2024-09-14', '2026-08-21', 1, 1, 'azucar impalpable', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '15', '3500', '2025-09-20', '2024-01-16', 2, 2, 'levadura', 'gr');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '10', '5200', '2025-04-10', '2026-03-15', 1, 1, 'chocolate barra', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '145', '2000', '2024-09-14', '2026-08-21', 1, 1, 'huevos', 'doc');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '15', '6500', '2025-09-20', '2024-01-16', 2, 2, 'dulce de leche', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '10', '3200', '2025-04-10', '2026-03-15', 1, 1, 'durazno', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '145', '2000', '2024-09-14', '2026-08-21', 1, 1, 'azucar', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '15', '1500', '2025-09-20', '2024-01-16', 2, 2, 'harina', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '10', '1200', '2025-04-10', '2026-03-15', 1, 1, 'sal', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '145', '2000', '2024-09-14', '2026-08-21', 1, 1, 'azucar impalpable', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '15', '1500', '2025-09-20', '2024-01-16', 2, 2, 'levadura', 'gr');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '10', '1200', '2025-04-10', '2026-03-15', 1, 1, 'chocolate barra', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '145', '2000', '2024-09-14', '2026-08-21', 1, 1, 'huevos', 'doc');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '15', '1500', '2025-09-20', '2024-01-16', 2, 2, 'dulce de leche', 'kg');
INSERT INTO `LuzClaritaWebOva`.`inventario` (`activo`, `cantidad`, `precio`, `fecha_ingreso`, `fecha_vencimiento`, `id_proveedor`, `id_usuario`, `nombre_Ingrediente`, `unidad_Medida`) VALUES ('1', '10', '1200', '2025-04-10', '2026-03-15', 1, 1, 'durazno', 'kg');


