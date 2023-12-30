package com.github.k7.coursein.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.github.k7.coursein.model.UploadImageRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@AllArgsConstructor
@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(String username, UploadImageRequest request) throws Exception {
        log.info("Uploading {}", request.getMultipartFile().getOriginalFilename());

        cloudinary.uploader()
            .upload(request.getMultipartFile().getBytes(),
                ObjectUtils.asMap("public_id", username));

        String fileExtension = Objects.requireNonNull(request.getMultipartFile().getContentType()).split("/")[1];

        String link = cloudinary.url().transformation(new Transformation<>()
                .quality("60")
                .chain()
                .dpr("auto")
            )
            .version(cloudinary.api().resource(username, ObjectUtils.emptyMap()).get("version"))
            .generate(username + "." + fileExtension);

        log.info("finished");

        return link;
    }

    @Override
    public void delete(String username) throws IOException {
        log.info("Deleting profile picture of user {}", username);
        cloudinary.uploader().destroy(username, ObjectUtils.asMap("resource_type", "image"));
    }

}
