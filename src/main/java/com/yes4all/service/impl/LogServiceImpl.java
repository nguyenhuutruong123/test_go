package com.yes4all.service.impl;


import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.LogService;

import com.yes4all.repository.LogServiceRepository;
import com.yes4all.repository.VendorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;


/**
 * Service class for managing users.
 */
@Service
@Transactional
public class LogServiceImpl implements com.yes4all.service.LogService {

    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    @Autowired
    private LogServiceRepository logServiceRepository;
    @Override
    public boolean writeLog(String functionDescription, String functionCode, String functionId, Object object,String project,String action) {
        String payload = CommonDataUtil.convertObjectToStringJson(object);
        LogService logService=new LogService();
        logService.setTime(new Date().toInstant());
        logService.setPayload(payload);
        logService.setFunctionCode(functionCode);
        logService.setFunctionId(String.valueOf(functionId));
        logService.setFunctionDescription(functionDescription);
        logService.setProject(project);
        logService.setAction(action);
        logServiceRepository.saveAndFlush(logService);
        return true;
    }
}
