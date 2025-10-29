package puj.app.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
@Profile("!s3")
@Slf4j
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local-root:uploads}")
    private String root;

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(Path.of(root));
            log.info("Local storage listo en {}", Path.of(root).toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el directorio local de uploads", e);
        }
    }

    @Override
    public String upload(String key, InputStream stream, long size, String contentType) {
        try {
            Path path = Path.of(root, key).normalize();
            Files.createDirectories(path.getParent());
            try (var out = Files.newOutputStream(path)) {
                stream.transferTo(out);
            }
            return "file://" + path.toAbsolutePath(); // sólo para DEV
        } catch (Exception e) {
            throw new RuntimeException("Error guardando archivo local", e);
        }
    }

    @Override
    public String presignGet(String key, String downloadFileName, String contentType, Duration ttl) {
        // DEV: devolvemos un file:// absoluto. Si algún día usas el perfil !s3,
        // puede que el navegador bloquee file://; en ese caso, sirve el archivo
        // con un controller propio (te dejo abajo un opcional).
        Path path = Path.of(root, key).normalize();
        return path.toUri().toString(); // "file:///C:/.../uploads/..."
    }
}
