package com.github.k7.coursein.service;

import com.github.k7.coursein.model.UploadImageRequest;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    String upload(UploadImageRequest request);
}
