package com.analistas.luzclaritaweb.web.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class CarritoInterceptor implements HandlerInterceptor {

    // Rutas donde SI debe aparecer el carrito
    private final List<String> rutasConCarrito = Arrays.asList(
        "/home",
        "/productos/listado",
        "/recetas/cards",
        "/cursos/listado2",
        "/productos/detalle"
    );

    // Rutas donde NO debe aparecer el carrito
    private final List<String> rutasSinCarrito = Arrays.asList(
        "/consultas/consulta",
        "/admin",
        "/productos/nuevo",
        "/productos/editar",
        "/productos/borrar",
        "/inventario",
        "/proveedor",
        "/caja",
        "/compras",
        "/recetas/form",
        "/recetas/listado"
    );

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        
        if (modelAndView != null) {
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            
            // Remover contexto para rutas relativas
            String relativeUri = uri.startsWith(contextPath) 
                ? uri.substring(contextPath.length()) 
                : uri;

            boolean mostrar = rutasConCarrito.stream().anyMatch(relativeUri::startsWith) &&
                             rutasSinCarrito.stream().noneMatch(relativeUri::startsWith);

            modelAndView.addObject("mostrarCarrito", mostrar);
        }
    }
}