package puj.app.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import puj.app.model.Rubric;
import puj.app.model.Syllabus;
import puj.app.repository.RubricRepository;
import puj.app.repository.SyllabusRepository;
import puj.app.service.StorageService;

import java.net.URI;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final StorageService storage;
    private final RubricRepository rubricRepo;
    private final SyllabusRepository syllabusRepo;

    // -------- Helpers --------
    private static final Pattern HTTPS_PATH = Pattern.compile("^https?://[^/]+/(.+)$");

    private String extractKeyFromUri(String uri) {
        if (uri == null) throw new IllegalArgumentException("URI vacío");
        String u = uri.trim();

        // s3://bucket/prefix/key
        if (u.startsWith("s3://")) {
            int firstSlash = u.indexOf('/', 5); // después de s3://
            if (firstSlash < 0) throw new IllegalArgumentException("s3 URI inválido: " + uri);
            return u.substring(firstSlash + 1); // todo lo que sigue a 'bucket/'
        }

        // https://bucket.s3.region.amazonaws.com/prefix/key  (o variantes)
        Matcher m = HTTPS_PATH.matcher(u);
        if (m.find()) {
            return m.group(1); // path sin el host
        }

        throw new IllegalArgumentException("URI no soportado: " + uri);
    }

    private RedirectView redirectTo(String url) {
        RedirectView rv = new RedirectView();
        rv.setUrl(url);
        rv.setExposeModelAttributes(false);
        return rv;
    }

    // -------- Endpoints --------

    @GetMapping("/syllabus/{id}")
    public RedirectView downloadSyllabus(@PathVariable String id) {
        Syllabus s = syllabusRepo.findById(id).orElseThrow();
        String key = extractKeyFromUri(s.getFileUri());
        String downloadName = (s.getTitle() != null && !s.getTitle().isBlank())
                ? (s.getTitle().trim().replaceAll("\\s+", "_") + ".pdf")
                : "temario.pdf";
        String presigned = storage.presignGet(key, downloadName, "application/pdf", Duration.ofMinutes(15));
        return redirectTo(presigned);
    }

    @GetMapping("/rubric/{id}")
    public RedirectView downloadRubric(@PathVariable String id) {
        Rubric r = rubricRepo.findById(id).orElseThrow();
        String key = extractKeyFromUri(r.getFileUri());
        String name = (r.getName() != null && !r.getName().isBlank())
                ? (r.getName().trim().replaceAll("\\s+", "_") + ".pdf")
                : "rubrica.pdf";
        String presigned = storage.presignGet(key, name, "application/pdf", Duration.ofMinutes(15));
        return redirectTo(presigned);
    }
}
