package com.analistas.luzclaritaweb.web.excepciones;

//Clase personalizada para manejar excepciones relacionadas con la sincronización del carrito.
//Extiende RuntimeException para ser lanzada en cualquier parte del código sin necesidad de declarar en el método.
//Esta excepción se utiliza para indicar problemas específicos durante la sincronización del carrito, como errores de
public class CarritoSyncException extends RuntimeException{

    public CarritoSyncException(String message, Throwable cause) {
        super(message, cause);
    }

}
