package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CourseServiceImpl implements CourseService{

    @Autowired
    private CourseRepository courseRepository;

    private static final String COURSE_NOT_FOUND_MESSAGE = "Course not found";

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourse(String courseName) {
        log.info("Getting course by name: {}", courseName);

        Course course = courseRepository.findByName(courseName)
            .orElseThrow(() -> {
                log.warn("Course not found: {}", courseName);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND_MESSAGE);
            });

        log.info("Found course: {}", course);

        return toCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllAvailableCourse(int page, int size) {
        log.info("Fetching all available courses. Page: {}, Size: {}", page, size);

        if (page < 0 || size <= 0) {
            log.error("Invalid page or size provided. Page: {}, Size: {}", page, size);
            throw new IllegalArgumentException("The page must not be negative and the size must be more than 0");
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> allCoursesPage = courseRepository.findAll(pageRequest);

        if (allCoursesPage.isEmpty()) {
            log.warn("No course available right now");
        } else {
            log.info("Found {} available courses", allCoursesPage.getTotalElements());
        }

        // mengkonversi setiap objek course dalam allCoursesPage menjadi objek CourseResponse
        List<CourseResponse> courseResponses = allCoursesPage.getContent().stream()
            .map(CourseServiceImpl::toCourseResponse)
            .collect(Collectors.toList());

        log.info("Returning {} courses on page {} of size {}", courseResponses.size(), page, size);

        return new PageImpl<>(courseResponses, pageRequest, allCoursesPage.getTotalElements());
    }

    public static CourseResponse toCourseResponse(Course course) {
        // set properti Course Response
        return CourseResponse.builder()
            .id(course.getId())
            .name(course.getName())
            .description(course.getDescription())
            .price(course.getPrice())
            .link(course.getLink())
            .category(course.getCategory())
            .type(course.getType())
            .level(course.getLevel())
            .createdAt(course.getCreatedAt())
            .updatedAt(course.getUpdatedAt())
            .build();
    }

}
