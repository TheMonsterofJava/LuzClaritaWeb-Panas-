package com.analistas.luzclaritaweb.model.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

public interface IFileStorageService {

    void init();

    String store(MultipartFile file);

    Resource loadAsResource(String filename);

    void delete(String filename);

}
