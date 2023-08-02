package com.yes4all.service;

import com.yes4all.domain.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;

public interface LogService {

    boolean writeLog(String functionDescription, String functionCode, String functionId, Object object, String project,String action) throws IOException, URISyntaxException;

}
