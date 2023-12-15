package com.github.k7.coursein.service;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseService {

    CourseResponse addCourse(AddCourseRequest courseRequest);

    CourseResponse getCourse(String code);

    Page<CourseResponse> getAllCourse(int page, int size);

    Page<CourseResponse> filterAllCourses(int page, int size, String[] filters, List<CourseCategory> categories, List<CourseLevel> levels, CourseType type);

    CourseResponse updateCourse(String code, UpdateCourseRequest request);

    void deleteCourse(String code);

    Long numberOfCourse(CourseType courseType);

}
