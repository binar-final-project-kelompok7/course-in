package com.github.k7.coursein.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.github.k7.coursein.model.UploadImageRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(UploadImageRequest request) {
        try {
            log.info("Uploading");

            Map<?, ?> uploadedImage = cloudinary
                .uploader()
                .upload(request.getMultipartFile().getBytes(),
                    ObjectUtils.asMap("public_id", request.getUploader()));

            log.info("finished");
            return uploadedImage.get("url").toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    //TODO: image compression, delete image from media, ...
}
