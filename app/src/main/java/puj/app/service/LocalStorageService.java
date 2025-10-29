package puj.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local-root:uploads}")
    private String root;

    @Override
    public String upload(String key, InputStream stream, long size, String contentType) {
        try {
            Path base = Paths.get(root).toAbsolutePath();
            Files.createDirectories(base);
            String name = (key == null || key.isBlank()) ? UUID.randomUUID() + ".bin" : key;
            Path target = base.resolve(name);
            Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toUri().toString();
        } catch (IOException e) {
            throw new RuntimeException("Error guardando archivo local", e);
        }
    }
}