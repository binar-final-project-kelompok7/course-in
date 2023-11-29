package com.github.k7.coursein.service;

import com.github.k7.coursein.model.CourseResponse;
import org.springframework.data.domain.Page;

public interface CourseService {
    CourseResponse getCourse(String courseName);
    Page<CourseResponse> getAllAvailableCourse(int page, int size);
}
