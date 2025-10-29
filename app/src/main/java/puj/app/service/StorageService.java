package puj.app.service;

import java.io.InputStream;

public interface StorageService {
    String upload(String key, InputStream stream, long size, String contentType);
}
