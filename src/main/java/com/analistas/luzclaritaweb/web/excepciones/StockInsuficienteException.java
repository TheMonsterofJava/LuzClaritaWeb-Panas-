package com.analistas.luzclaritaweb.web.excepciones;

public class StockInsuficienteException extends Exception {
    public StockInsuficienteException() {
        super();
    }

    public StockInsuficienteException(String message) {
        super(message);
    }

}
