package com.analistas.luzclaritaweb.web.excepciones;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException() {
        super();
    }

    public StockInsuficienteException(String message) {
        super(message);
    }

}

