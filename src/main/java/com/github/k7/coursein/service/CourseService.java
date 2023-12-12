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

    Page<CourseResponse> filterAllCourses(int page, int size, String[] filters);

    Page<CourseResponse> filterAllCourses(int page, int size, String[] filters, List<CourseCategory> courseCategories);

    Page<CourseResponse> filterAllCourses(int page, int size, String[] filters, List<CourseCategory> courseCategories, List<CourseLevel> courseLevels);

    Page<CourseResponse> filterAllCourses(int page, int size, String[] filters, List<CourseCategory> courseCategories, List<CourseLevel> courseLevels, CourseType courseType);

    Page<CourseResponse> filterAllCourses1(int page, int size, String[] filters, List<CourseLevel> courseLevels);

    Page<CourseResponse> filterAllCourses1(int page, int size, String[] filters, List<CourseLevel> courseLevels, CourseType courseType);

    Page<CourseResponse> filterAllCourses2(int page, int size, String[] filters, CourseType courseType);

    Page<CourseResponse> filterAllCourses2(int page, int size, String[] filters, CourseType courseType, List<CourseCategory> courseCategories);

    CourseResponse updateCourse(String code, UpdateCourseRequest request);

    void deleteCourse(String code);

    Long numberOfCourse(CourseType courseType);

}
