package puj.app.service;

import java.io.InputStream;
import java.time.Duration;

public interface StorageService {
    String upload(String key, InputStream stream, long size, String contentType);

    // URL temporal de descarga (GET) con “attachment”
    String presignGet(String key, String downloadFileName, String contentType, Duration ttl);
}
