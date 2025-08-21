package com.analistas.luzclaritaweb.model.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.analistas.luzclaritaweb.model.service.interfaces.IUploadFileService;

@Service
public class UploadFileServiceImpl implements IUploadFileService {

    private final static String UPLOADS_FOLDER = "uploads";

    @Override
    public Resource load(String filename) throws Exception {
        Path pathFoto = getPath(filename);
        Resource recurso = null;

        recurso = new UrlResource(pathFoto.toUri());
        if(!recurso.exists() || !recurso.isReadable()){
            throw new RuntimeException("Error: no se puede cargar la imagen: " + pathFoto.toString());
        }
        return recurso;
    }

    @Override
    public String copy(MultipartFile file) throws Exception {
        
        String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path rootPath = getPath(uniqueFilename);

        Files.copy(file.getInputStream(), rootPath);
            
        return uniqueFilename;
    }

    @Override
    public boolean delete(String filename) {
        
        Path rootPath = getPath(filename);
        File archivo = rootPath.toFile();

        if(archivo.exists() && archivo.canRead()){
            if(archivo.delete()){
                return true;
            }
        }
        return false;
    }

    public Path getPath(String filename){
        return Paths.get(UPLOADS_FOLDER).resolve(filename).toAbsolutePath();
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(Paths.get(UPLOADS_FOLDER).toFile());
    }

    @Override
    public void init() throws Exception {
        Files.createDirectory(Paths.get(UPLOADS_FOLDER));
    }
    
}
