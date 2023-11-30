package com.github.k7.coursein.service;

import com.github.k7.coursein.model.CourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import org.springframework.data.domain.Page;

public interface CourseService {

    CourseResponse getCourse(Long id);

    Page<CourseResponse> getAllCourse(int page, int size);

    Boolean nameLinkAvailability(CourseRequest courseRequest);

    Boolean addCourse(CourseRequest courseRequest);

}
