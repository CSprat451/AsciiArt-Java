package com.company.asciiart;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SpringBootApplication
@RestController
public class Main {

    public static void main(String[] args){
        SpringApplication.run(Main.class, args);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/ascii")
    public String fileHandleUpload(@RequestParam("file") MultipartFile file) throws IOException {
        File convertedFile = multipartToFile(file, "temp");
        ImageConfig image = new ImageConfig(convertedFile);
        image.saveAsciiImage();
        return "http://localhost:8080/files/" + image.uniqueFilename;
    }

    public static File multipartToFile(MultipartFile file, String fileName) throws IllegalStateException, IOException {
        File convertedFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        file.transferTo(convertedFile);
        return convertedFile;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/files/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
            Resource file = loadAsResource(filename);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    public Resource loadAsResource(String fileName) {
        try {
            Path file = Paths.get("results/" + fileName + ".jpg");
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new RuntimeException("Could not read file: " + fileName);
            }
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }

}
