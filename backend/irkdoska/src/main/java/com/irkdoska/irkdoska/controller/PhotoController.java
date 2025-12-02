package com.irkdoska.irkdoska.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URLConnection;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @GetMapping
    public ResponseEntity<InputStreamResource> getPhoto(@RequestParam("path") String path) {
        try {
            InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );

            String contentType = URLConnection.guessContentTypeFromName(path);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(is));
        } catch (Exception e) {
            log.error("Failed to load photo from storage, path={}", path, e);
            return ResponseEntity.notFound().build();
        }
    }
}


