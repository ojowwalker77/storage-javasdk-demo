package com.example;

import com.google.cloud.storage.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCSDownloader {

    private static final Logger logger = LoggerFactory.getLogger(
        GCSDownloader.class
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
     * Downloads a file from Google Cloud Storage.
     *
     * @param projectId  The Google Cloud project ID.
     * @param bucketName The name of the GCS bucket.
     * @param objectName The name of the object (file) in the bucket to download.
     * @param destFilePath The local path where the downloaded file will be saved.
     * @throws IOException If an error occurs during the download or file writing.
     */
    public static void downloadFile(
        String projectId,
        String bucketName,
        String objectName,
        String destFilePath
    ) throws IOException {
        Storage storage = StorageOptions.newBuilder()
            .setProjectId(projectId)
            .build()
            .getService();

        BlobId blobId = BlobId.of(bucketName, objectName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            logger.error(
                "Object {} in bucket {} not found.",
                objectName,
                bucketName
            );
            throw new IOException(
                "Object " +
                objectName +
                " in bucket " +
                bucketName +
                " not found."
            );
        }

        try {
            logger.info(
                "Downloading gs://{}/{} to {}",
                bucketName,
                objectName,
                destFilePath
            );
            byte[] content = blob.getContent();
            Files.write(Paths.get(destFilePath), content);
            logger.info(
                "Downloaded gs://{}/{} to {}",
                bucketName,
                objectName,
                destFilePath
            );
        } catch (StorageException e) {
            logger.error(
                "Error downloading gs://{}/{} to {}: {}",
                bucketName,
                objectName,
                destFilePath,
                e.getMessage(),
                e
            );
            throw new IOException(
                "Error downloading file from GCS: " + e.getMessage(),
                e
            );
        }
    }

    public static void main(String[] args) {
        String projectId = getEnvVar(
            "GCP_PROJECT_ID",
        );
        String bucketName = getEnvVar(
            "GCP_BUCKET_NAME",
        );
        String objectName = getEnvVar("GCP_OBJECT_NAME", "test.txt");
        String destFilePath = getEnvVar(
            "DEST_FILE_PATH",
            "downloaded_test.txt"
        );

        try {
            downloadFile(projectId, bucketName, objectName, destFilePath);
        } catch (IOException e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
        }
    }
}
