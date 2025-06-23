package com.analistas.luzclaritaweb.model.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.analistas.luzclaritaweb.model.domain.ImagenProducto;
import com.analistas.luzclaritaweb.model.repository.IImagenProductoRepository;

@Service
public class ImagenServiceImpl implements IImagenProductoService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImagenServiceImpl.class);
    private static final int MAX_FILE_NAME_LENGTH = 100;
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @Autowired
    private IImagenProductoRepository imagenRepository;

    @Override
    public ImagenProducto guardarImagen(MultipartFile archivo) throws IOException {
        // Validaciones exhaustivas
        validateFile(archivo);
        
        // Preparar directorio de destino
        Path uploadPath = prepareUploadDirectory();
        
        // Generar nombre seguro para el archivo
        String safeFileName = generateSafeFileName(archivo.getOriginalFilename());
        
        // Guardar archivo en sistema
        Path filePath = saveFileToDisk(archivo, uploadPath, safeFileName);
        
        // Crear y guardar entidad ImagenProducto
        return createAndSaveImageEntity(archivo, safeFileName, filePath);
    }

    @Override
    public void eliminarPorId(Long id) {
        try {
            // Primero obtenemos la imagen para tener la ruta del archivo
            ImagenProducto imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada con ID: " + id));
            
            // Eliminar del sistema de archivos
            eliminarImagenDelSistema(imagen.getRutaArchivo());
            
            // Eliminar de la base de datos
            imagenRepository.deleteById(id);
            
            logger.info("Imagen eliminada correctamente - ID: {}", id);
        } catch (Exception e) {
            logger.error("Error al eliminar imagen con ID: {}", id, e);
            throw new RuntimeException("No se pudo eliminar la imagen: " + e.getMessage());
        }
    }

    @Override
    public boolean eliminarImagenDelSistema(String rutaArchivo) {
        try {
            Path filePath = Paths.get(uploadDir, "productos", extractFileNameFromPath(rutaArchivo));
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                logger.info("Archivo eliminado del sistema: {}", filePath);
            } else {
                logger.warn("El archivo no existía: {}", filePath);
            }
            
            return deleted;
        } catch (IOException e) {
            logger.error("Error al eliminar archivo: {}", rutaArchivo, e);
            return false;
        }
    }

    // Métodos auxiliares privados
    
    private void validateFile(MultipartFile archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("El archivo no puede ser nulo");
        }
        
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Tipo de archivo no soportado. Solo se permiten imágenes");
        }
        
        if (archivo.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("El tamaño del archivo excede el límite permitido (10MB)");
        }
    }
    
    private Path prepareUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(uploadDir, "productos").toAbsolutePath().normalize();
        
        if (!Files.exists(uploadPath)) {
            logger.info("Creando directorio de uploads: {}", uploadPath);
            Files.createDirectories(uploadPath);
        }
        
        return uploadPath;
    }
    
    private String generateSafeFileName(String originalFileName) {
        String fileExtension = FilenameUtils.getExtension(originalFileName);
        String baseName = FilenameUtils.getBaseName(originalFileName);
        
        // Limitar longitud del nombre y quitar caracteres especiales
        String safeBaseName = baseName.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (safeBaseName.length() > MAX_FILE_NAME_LENGTH) {
            safeBaseName = safeBaseName.substring(0, MAX_FILE_NAME_LENGTH);
        }
        
        return UUID.randomUUID().toString() + "_" + safeBaseName + "." + fileExtension;
    }
    
    private Path saveFileToDisk(MultipartFile archivo, Path uploadPath, String fileName) throws IOException {
        Path filePath = uploadPath.resolve(fileName);
        
        logger.info("Guardando archivo en: {}", filePath);
        Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filePath;
    }
    
    private ImagenProducto createAndSaveImageEntity(MultipartFile archivo, String fileName, Path filePath) {
        ImagenProducto imagen = new ImagenProducto();
        imagen.setNombreArchivo(archivo.getOriginalFilename());
        imagen.setRutaArchivo("/uploads/productos/" + fileName);
        imagen.setTipoMime(archivo.getContentType());
        imagen.setTamanio(archivo.getSize());
        
        logger.info("Guardando entidad ImagenProducto para archivo: {}", fileName);
        return imagenRepository.save(imagen);
    }
    
    private String extractFileNameFromPath(String rutaArchivo) {
        return rutaArchivo.substring(rutaArchivo.lastIndexOf('/') + 1);
    }
}