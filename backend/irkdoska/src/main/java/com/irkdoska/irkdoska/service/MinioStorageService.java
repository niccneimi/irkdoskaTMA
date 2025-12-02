package com.irkdoska.irkdoska.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public List<String> uploadPhotos(Long adId, MultipartFile[] photos) {
        List<String> paths = new ArrayList<>();
        if (photos == null || photos.length == 0) {
            return paths;
        }

        try {
            ensureBucket();
            for (MultipartFile photo : photos) {
                if (photo.isEmpty()) {
                    continue;
                }
                String objectName = buildObjectName(adId, photo.getOriginalFilename());
                try (InputStream is = photo.getInputStream()) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .stream(is, -1, 10 * 1024 * 1024)
                                    .contentType(photo.getContentType())
                                    .build()
                    );
                }
                paths.add(objectName);
            }
        } catch (Exception e) {
            log.error("Failed to upload photos to MinIO", e);
            throw new RuntimeException("Failed to upload photos", e);
        }
        return paths;
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
            );
        }
    }

    private String buildObjectName(Long adId, String originalFilename) {
        String safeName = (originalFilename != null) ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "photo";
        return "ads/" + adId + "/" + UUID.randomUUID() + "_" + safeName;
    }
}


