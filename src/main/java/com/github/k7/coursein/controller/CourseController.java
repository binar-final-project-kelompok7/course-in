package com.github.k7.coursein.controller;

import com.github.k7.coursein.enums.CourseCategory;
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
import java.util.stream.Collectors;

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
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<CourseResponse> allCourse = courseService.getAllCourse(page, size);
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

    @GetMapping(
        path = "/filtered",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<CourseResponse>> filterAllCourse(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "filter") List<String> filters,
        @RequestParam(name = "category", required = false) List<CourseCategory> category,
        @RequestParam(name = "level", required = false) List<CourseLevel> levels,
        @RequestParam(name = "type", required = false) CourseType type
    ) {

        Page<CourseResponse> filteredCourse = courseService.filterAllCourses(
            page,
            size,
            filters.toArray(new String[0]),
            category,
            levels,
            type);

        return WebResponse.<List<CourseResponse>>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(filteredCourse.getContent())
            .paging(PagingResponse.builder()
                .currentPage(filteredCourse.getNumber())
                .totalPage(filteredCourse.getTotalPages())
                .size(filteredCourse.getSize())
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
        path = "/count-course",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Long> numberOfCourse(
        @RequestParam(name = "type", required = false) String type
    ) {
        if (type == null) {
            return WebResponse.<Long>builder()
                .data(courseService.numberOfCourse(null))
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .build();
        }

        return WebResponse.<Long>builder()
            .data(courseService.numberOfCourse(CourseType.valueOf(type)))
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .build();
    }

}
