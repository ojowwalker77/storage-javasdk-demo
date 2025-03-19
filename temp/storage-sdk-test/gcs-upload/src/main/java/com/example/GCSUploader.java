package com.example;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCSUploader {

    private static final Logger logger = LoggerFactory.getLogger(
        GCSUploader.class
    );

    private static String getEnvVar(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            logger.warn(
                "Environment variable {} not set, using default value: {}",
                name,
                defaultValue
            );
            return defaultValue;
        }
        return value;
    }

    /**
     * Uploads a file to Google Cloud Storage.
     *
     * @param projectId The Google Cloud project ID.
     * @param bucketName The name of the GCS bucket.
     * @param objectName The desired name of the object (file) in the bucket.
     * @param filePath The local path to the file to upload.
     * @throws IOException If an error occurs during file reading.
     * @throws StorageException If an error occurs during the upload to GCS.
     */
    public static void uploadFile(
        String projectId,
        String bucketName,
        String objectName,
        String filePath
    ) throws IOException, StorageException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            logger.error("File not found: {}", filePath);
            throw new IOException("File not found: " + filePath);
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(
            new FileInputStream("sga-prod-compute-ad216b698091.json")
        );

        Storage storage = StorageOptions.newBuilder()
            .setProjectId(projectId)
            .setCredentials(credentials)
            .build()
            .getService();

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try {
            logger.info(
                "Uploading file {} to gs://{}/{}",
                filePath,
                bucketName,
                objectName
            );
            storage.create(blobInfo, Files.readAllBytes(path));
            logger.info(
                "File {} successfully uploaded to gs://{}/{}",
                filePath,
                bucketName,
                objectName
            );
        } catch (StorageException e) {
            logger.error(
                "Error uploading file {} to GCS: {}",
                filePath,
                e.getMessage(),
                e
            );
            throw e;
        }
    }

    public static void main(String[] args) {

        String projectId = getEnvVar(
            "GCP_PROJECT_ID",
            "sga-prod-compute"
        );
        String bucketName = getEnvVar(
            "GCP_BUCKET_NAME",
            ""
        );
        String objectName = getEnvVar("GCP_OBJECT_NAME", "test.txt");
        String filePath = getEnvVar("FILE_PATH", "test.txt");

        try {
            uploadFile(projectId, bucketName, objectName, filePath);
        } catch (IOException | StorageException e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
        }
    }
}
