package com.github.k7.coursein.service;

import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import org.springframework.data.domain.Page;

public interface CourseService {

    CourseResponse addCourse(AddCourseRequest courseRequest);

    CourseResponse getCourse(String code);

    Page<CourseResponse> getAllCourse(int page, int size);

    CourseResponse updateCourse(String code, UpdateCourseRequest request);

    void deleteCourse(String code);

}
