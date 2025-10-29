package puj.app.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;

@Service
@Profile("s3")
@Slf4j
public class S3StorageService implements StorageService {

    @Value("${app.storage.s3.bucket}") private String bucket;
    @Value("${app.storage.s3.prefix:portal}") private String prefix;
    @Value("${app.aws.region:us-east-1}") private String region;

    @Value("${app.aws.access-key-id:}")     private String accessKeyId;
    @Value("${app.aws.secret-access-key:}") private String secretAccessKey;

    private S3Client s3;
    private S3Presigner presigner;

    private AwsCredentialsProvider creds() {
        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            );
        }
        return DefaultCredentialsProvider.create();
    }

    @PostConstruct
    void init() {
        var reg = Region.of(region);
        var cp  = creds();
        this.s3 = S3Client.builder().region(reg).credentialsProvider(cp).build();
        this.presigner = S3Presigner.builder().region(reg).credentialsProvider(cp).build();
        log.info("S3 listo. bucket={}, prefix={}, region={}", bucket, prefix, region);
    }

    private String objectKey(String key) {
        var p = (prefix == null || prefix.isBlank()) ? "" : (prefix.endsWith("/") ? prefix : prefix + "/");
        return p + key;
    }

    private String filenameFromKey(String key) {
        String name = key.replace('\\','/');
        int i = name.lastIndexOf('/');
        return (i >= 0 ? name.substring(i + 1) : name);
    }

    @Override
    public String upload(String key, InputStream stream, long size, String contentType) {
        var objectKey = objectKey(key);

        var putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(Objects.requireNonNullElse(contentType, "application/pdf"))
                // fuerza descarga en el navegador
                .contentDisposition("attachment; filename=\"" + filenameFromKey(key) + "\"")
                // NOTA: NO .acl(...) porque el bucket tiene “Bucket owner enforced”
                .build();

        s3.putObject(putReq, RequestBody.fromInputStream(stream, size));

        // URL pública (tu bucket policy permite GetObject en este prefijo)
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;
    }

    @Override
    public String presignGet(String key, String downloadFileName, String contentType, Duration ttl) {
        String objectKey = objectKey(key);

        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .responseContentDisposition("attachment; filename=\"" + downloadFileName + "\"")
                .responseContentType(contentType != null ? contentType : "application/pdf")
                .build();

        var presigned = presigner.presignGetObject(b -> b
                .signatureDuration(ttl != null ? ttl : Duration.ofMinutes(15))
                .getObjectRequest(get)
        );

        return presigned.url().toString();
    }
}
