package com.github.k7.coursein.service;

import com.github.k7.coursein.model.UploadImageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {

    String upload(String username, UploadImageRequest request) throws IOException;

    void delete(String username) throws IOException;
}
