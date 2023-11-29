package com.github.k7.coursein.controller;

import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.PagingResponse;
import com.github.k7.coursein.model.WebResponse;
import com.github.k7.coursein.service.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    @GetMapping(
        path = "/{courseId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CourseResponse> getCourse(@PathVariable("courseId") Long courseId) {
        CourseResponse courseResponse = courseService.getCourse(courseId);
        return WebResponse.<CourseResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(courseResponse)
            .build();
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<CourseResponse>> getAllCourse(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<CourseResponse> allProduct = courseService.getAllCourse(page, size);
        return WebResponse.<List<CourseResponse>>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(allProduct.getContent())
            .paging(PagingResponse.builder()
                .currentPage(allProduct.getNumber())
                .totalPage(allProduct.getTotalPages())
                .size(allProduct.getSize())
                .build())
            .build();
    }

}
