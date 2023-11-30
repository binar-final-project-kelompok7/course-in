package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.CourseRequest;
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
    public Boolean nameLinkAvailability(CourseRequest courseRequest) {

        validationService.validate(courseRequest);

        Boolean courseNameAlreadyExist = courseRepository.existsByName(courseRequest.getName());
        Boolean courseLinkAlreadyExist = courseRepository.existsByLink(courseRequest.getLink());

        if (courseNameAlreadyExist && courseLinkAlreadyExist) {
            log.error("Course with name {} and link {} already existed in the database", courseRequest.getName(), courseRequest.getLink());
            return Boolean.FALSE;
        } else if (courseNameAlreadyExist) {
            log.error("Course with name {} already existed in the database", courseRequest.getName());
            return Boolean.FALSE;
        } else if (courseLinkAlreadyExist) {
            log.error("Course with link {} already existed in the database", courseRequest.getLink());
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean addCourse(CourseRequest courseRequest) {

        if (this.nameLinkAvailability(courseRequest)) {
            LocalDateTime createdAndUpdated = LocalDateTime.now();

            if (courseRequest.getType().equals(CourseType.FREE)) {
                courseRequest.setPrice(0.0);
            }

            try {
                log.info("Building the course from request.");
                Course toBeAddedCourse = Course
                    .builder()
                    .name(courseRequest.getName())
                    .description(courseRequest.getDescription())
                    .price(courseRequest.getPrice())
                    .link(courseRequest.getLink())
                    .category(courseRequest.getCategory())
                    .type(courseRequest.getType())
                    .level(courseRequest.getLevel())
                    .intendeds(courseRequest.getIntendeds())
                    .createdAt(createdAndUpdated)
                    .updatedAt(createdAndUpdated)
                    .build();
                log.info("Done building. Saving to database...");
                courseRepository.save(toBeAddedCourse);
                log.info("Course saved successfully");
                return Boolean.TRUE;
            } catch (Exception e) {
                log.error("Failed to add course.");
                log.error("Cause: {}", e.getMessage());
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
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
