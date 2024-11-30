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