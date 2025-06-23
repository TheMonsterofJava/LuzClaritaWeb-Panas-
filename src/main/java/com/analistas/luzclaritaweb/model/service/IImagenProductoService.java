package com.analistas.luzclaritaweb.model.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import com.analistas.luzclaritaweb.model.domain.ImagenProducto;

public interface IImagenProductoService {
    
    /**
     * Guarda una imagen en el sistema de archivos y devuelve la entidad ImagenProducto
     * @param archivo Archivo de imagen a guardar
     * @return Entidad ImagenProducto con los datos de la imagen guardada
     * @throws IOException Si ocurre un error al guardar el archivo
     * @throws IllegalArgumentException Si el archivo no es válido
     */
    ImagenProducto guardarImagen(MultipartFile archivo) throws IOException, IllegalArgumentException;
    
    /**
     * Elimina una imagen por su ID
     * @param id ID de la imagen a eliminar
     */
    void eliminarPorId(Long id);
    
    /**
     * Elimina una imagen del sistema de archivos
     * @param rutaArchivo Ruta del archivo a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean eliminarImagenDelSistema(String rutaArchivo);
}