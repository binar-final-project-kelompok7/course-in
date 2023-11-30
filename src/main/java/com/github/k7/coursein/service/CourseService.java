package com.github.k7.coursein.service;

import com.github.k7.coursein.model.CourseResponse;
import org.springframework.data.domain.Page;

public interface CourseService {
    CourseResponse getCourse(Long id);

    Page<CourseResponse> getAllCourse(int page, int size);

    void deleteCourse(Long id);
}
