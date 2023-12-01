package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
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
    public void addCourse(AddCourseRequest request) {
        LocalDateTime createdAndUpdated = LocalDateTime.now();

        validationService.validate(request);

        if (courseRepository.existsByNameOrLink(request.getName(), request.getLink())) {
            log.info(
                "Course with name : {} or link : {} already exists", request.getName(), request.getLink()
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course already exists");
        }

        if (request.getType().equals(CourseType.FREE)) {
            request.setPrice(0.0);
        }

        log.info("Building the course from request.");
        Course toBeAddedCourse = Course
            .builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .link(request.getLink())
            .category(request.getCategory())
            .type(request.getType())
            .level(request.getLevel())
            .intendeds(request.getIntendeds())
            .createdAt(createdAndUpdated)
            .updatedAt(createdAndUpdated)
            .build();
        log.info("Done building. Saving to database...");
        courseRepository.save(toBeAddedCourse);
        log.info("Course saved successfully");
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
