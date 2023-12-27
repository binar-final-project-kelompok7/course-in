package com.github.k7.coursein.service;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseFilter;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import org.springframework.data.domain.Page;

import java.util.Set;

public interface CourseService {

    CourseResponse addCourse(AddCourseRequest courseRequest);

    CourseResponse getCourse(String code);

    Page<CourseResponse> getAllCourse(CourseType type,
                                      Set<CourseFilter> filters,
                                      Set<CourseCategory> categories,
                                      Set<CourseLevel> levels,
                                      int page, int size);

    Page<CourseResponse> getAllCourseUser(String username,
                                          CourseType type,
                                          Set<CourseFilter> filters,
                                          Set<CourseCategory> categories,
                                          Set<CourseLevel> levels,
                                          int page, int size);

    CourseResponse updateCourse(String code, UpdateCourseRequest request);

    void deleteCourse(String code);

    long countCourse();

    long countPremiumCourse();

}
