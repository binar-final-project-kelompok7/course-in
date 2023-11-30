package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import com.github.k7.coursein.repository.CourseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    private final ValidationService validationService;

    private static final String COURSE_NOT_FOUND_MESSAGE = "Course not found";

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id) {
        log.info("Getting course by id: {}", id);

        Course course = courseRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Course not found: {}", id);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND_MESSAGE);
            });

        log.info("Found course: {}", course);

        return toCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourse(int page, int size) {
        log.info("Fetching all available courses. Page: {}, Size: {}", page, size);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> allCoursesPage = courseRepository.findAll(pageRequest);

        List<CourseResponse> courseResponses = allCoursesPage.getContent().stream()
            .map(CourseServiceImpl::toCourseResponse)
            .collect(Collectors.toList());

        log.info("Returning {} courses on page {} of size {}", courseResponses.size(), page, size);

        return new PageImpl<>(courseResponses, pageRequest, allCoursesPage.getTotalElements());
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);

        Course course = courseRepository.findById(id)
            .orElseThrow(() -> {
                log.info("Course not found : {}", id);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND_MESSAGE);
            });

        log.info("Course found: {}", course);

        courseRepository.delete(course);

        log.info("Course deleted successfully");
    }

    @Override
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        validationService.validate(request);

        Course course = courseRepository.findById(id)
            .orElseThrow(() -> {
                log.info("Course not found: {}", id);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND_MESSAGE);
            });

        log.info("Updating course with ID: {}", id);

        updateCourseProperties(course, request);

        course.setUpdatedAt(LocalDateTime.now());

        courseRepository.save(course);

        log.info("Course updated successfully");

        return toCourseResponse(course);
    }

    private void updateCourseProperties(Course course, UpdateCourseRequest request) {
        if (request.getName() != null) {
            course.setName(request.getName());
            log.info("Updated course name to: {}", request.getName());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
            log.info("Updated course description to: {}", request.getDescription());
        }

        if (request.getPrice() != null) {
            course.setPrice(request.getPrice());
            log.info("Updated course price to: {}", request.getPrice());
        }

        if (request.getLink() != null) {
            course.setLink(request.getLink());
            log.info("Updated course link to: {}", request.getLink());
        }

        if (request.getCategory() != null) {
            course.setCategory(request.getCategory());
            log.info("Updated course category to: {}", request.getCategory());
        }

        if (request.getType() != null) {
            course.setType(request.getType());
            log.info("Updated course type to: {}", request.getType());
        }

        if (request.getLevel() != null) {
            course.setLevel(request.getLevel());
            log.info("Updated course level to: {}", request.getLevel());
        }
    }


    public static CourseResponse toCourseResponse(Course course) {
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
