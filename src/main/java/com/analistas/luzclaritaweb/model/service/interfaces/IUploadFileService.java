package com.analistas.luzclaritaweb.model.service.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadFileService {

    public Resource load(String filename) throws Exception;

    public String copy(MultipartFile file) throws Exception;

    public boolean delete(String filename);

    public void deleteAll();

    public void init() throws Exception;
}
