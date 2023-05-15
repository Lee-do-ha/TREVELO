package com.ssafy.enjoytrip.global.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ssafy.enjoytrip.global.ErrorCode;
import com.ssafy.enjoytrip.global.exception.AmazonS3Exception;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class AmazonS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    public List<String> uploadFiles(List<MultipartFile> multipartFiles) {
        log.debug("AmazonS3Service - uploadFiles. '{}' 개의 사진이 입력되었습니다.", multipartFiles.size());
        List<String> fileNameList = new ArrayList<>();

        multipartFiles.forEach(file -> {
            String fileName = createFileName(file.getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(
                    new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new AmazonS3Exception(ErrorCode.FILE_UPLOAD_FAIL, ErrorCode.FILE_UPLOAD_FAIL.getMessage());
            }

            fileNameList.add(fileName);

        });

        return fileNameList;
    }

    private String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    //파일 형식 에러
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch( StringIndexOutOfBoundsException e) {
            throw new AmazonS3Exception(ErrorCode.FILE_UPLOAD_FAIL, ErrorCode.FILE_UPLOAD_FAIL.getMessage());
        }
    }
}
