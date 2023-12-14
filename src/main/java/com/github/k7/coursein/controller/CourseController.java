package com.github.k7.coursein.controller;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseFilter;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.PagingResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import com.github.k7.coursein.model.WebResponse;
import com.github.k7.coursein.service.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<CourseResponse>> addCourse(@RequestBody AddCourseRequest addCourseRequest) {
        CourseResponse courseResponse = courseService.addCourse(addCourseRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(WebResponse.<CourseResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .data(courseResponse)
                .build());
    }

    @GetMapping(
        path = "/{courseCode}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CourseResponse> getCourse(@PathVariable("courseCode") String courseCode) {
        CourseResponse courseResponse = courseService.getCourse(courseCode);
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
        @RequestParam(name = "type", required = false) CourseType type,
        @RequestParam(name = "filters", required = false) Set<CourseFilter> filters,
        @RequestParam(name = "categories", required = false) Set<CourseCategory> categories,
        @RequestParam(name = "levels", required = false) Set<CourseLevel> levels,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<CourseResponse> allCourse = courseService.getAllCourse(type, filters, categories, levels, page, size);
        return WebResponse.<List<CourseResponse>>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(allCourse.getContent())
            .paging(PagingResponse.builder()
                .currentPage(allCourse.getNumber())
                .totalPage(allCourse.getTotalPages())
                .size(allCourse.getSize())
                .build())
            .build();
    }

    @PatchMapping(
        path = "/{courseCode}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CourseResponse> updateCourse(@PathVariable("courseCode") String courseCode,
                                                    @RequestBody UpdateCourseRequest updateCourseRequest) {
        CourseResponse courseResponse = courseService.updateCourse(courseCode, updateCourseRequest);
        return WebResponse.<CourseResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(courseResponse)
            .build();
    }

    @DeleteMapping(
        path = "/delete/{courseCode}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> deleteCourse(@PathVariable("courseCode") String courseCode) {
        courseService.deleteCourse(courseCode);
        return WebResponse.<String>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .build();
    }

    @GetMapping(
        path = "/count",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Long> countCourse() {
        long count = courseService.countCourse();
        return WebResponse.<Long>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(count)
            .build();
    }

    @GetMapping(
        path = "/count/premium",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Long> countPremiumCourse() {
        long count = courseService.countPremiumCourse();
        return WebResponse.<Long>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(count)
            .build();
    }

}
