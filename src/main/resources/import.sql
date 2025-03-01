/*Insertar Roles*/
/*Permisos o Roles*/
/*Tabla de Permisos*/
INSERT INTO `permisos` (nombre, descripcion) VALUES ('1 - PROGRAMADOR', 'Permiso mas alto, puede modificar todo'),('2 - ADMIN', 'Dueño de la empresa - Tiene menos permisos que programador'),('3 - OPERADOR', 'Trabajador de la empresa - Tiene algunos permisos'),('4 - CLIENTE', 'Usuario Registrado que realiza una compra'),('5 - UNLOGUED', 'Consultas algunos elementos de la pagina sin estar registrado');

/*Crear Usuarios*/
INSERT INTO `usuarios` (activo, fecha_creacion, id_permiso, nombre, clave, email) VALUES (1, '2024-12-01', 1, 'Osvaldo', '$2y$10$ZP7gE3toR10ffABvlsvJXOGQsHaUQPizzXpuBZj5Gkf.7mT3hFmQS', 'Osvaldo@gmail.com.ar') /*user*/
INSERT INTO `usuarios` (activo, fecha_creacion, id_permiso, nombre, clave, email) VALUES (1, '2024-12-01', 4, 'Santi', '$2y$10$Y5PE96MBydWiAVCAkf1TQu99d7NKYNPWU/HEAZGQ2l5OQu2XiPiiC', 'Santi@gmail.com.ar') /*cliente*/

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

