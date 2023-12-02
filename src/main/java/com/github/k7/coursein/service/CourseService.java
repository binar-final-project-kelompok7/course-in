package com.github.k7.coursein.service;

import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import org.springframework.data.domain.Page;

public interface CourseService {

    void addCourse(AddCourseRequest courseRequest);

    CourseResponse getCourse(Long id);

    Page<CourseResponse> getAllCourse(int page, int size);

    CourseResponse updateCourse(Long id, UpdateCourseRequest request);

    void deleteCourse(Long id);

}
