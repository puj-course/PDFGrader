package puj.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Profile("s3")
public class S3StorageService implements StorageService {

    @Value("${app.storage.s3.bucket}")
    private String bucket;

    @Value("${app.storage.s3.prefix:portal}")
    private String prefix;

    @Override
    public String upload(String key, InputStream stream, long size, String contentType) {
        // TODO: Implementa con AWS SDK v2 (S3Client.putObject)
        // Devuelve s3://bucket/prefix/key o https://...
        throw new UnsupportedOperationException("Implementar S3");
    }
}