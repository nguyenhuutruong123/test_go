package com.yes4all.service;

import com.yes4all.domain.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ResourceService {

    Resource handleUploadFile(MultipartFile multipartFile, Integer id, String module, String pathName) throws IOException, URISyntaxException;


    Boolean deleteFileUploadTemp(Integer id, String module, String pathName) throws IOException, URISyntaxException;

}
