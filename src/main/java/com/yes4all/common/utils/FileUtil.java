package com.yes4all.common.utils;

import com.yes4all.common.errors.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    // private constructor
    private FileUtil() {}

    public static byte[] zip(Map<String, byte[]> files){
        byte[] result = null;
        try {
            ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(byteArr);

            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                String fileName = file.getKey();
                byte[] data = file.getValue();
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOut.putNextEntry(zipEntry);
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = bis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
                bis.close();
                zipOut.closeEntry();
            }

            zipOut.close();
            result = byteArr.toByteArray();

            if(CommonDataUtil.isNull(result)){
                throw new BusinessException("Could not zip file");
            }
            return result;
        } catch(Exception e){
            log.error("Could not zip file");
            throw new BusinessException(e.getMessage());
        }
    }
}
