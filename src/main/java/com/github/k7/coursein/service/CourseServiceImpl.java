package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.entity.Intended;
import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import com.github.k7.coursein.repository.CourseRepository;
import com.github.k7.coursein.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    private final ValidationService validationService;

    private static final String COURSE_NOT_FOUND_MESSAGE = "Course not found";

    @Override
    @Transactional
    public CourseResponse addCourse(AddCourseRequest request) {
        validationService.validate(request);

        if (Boolean.TRUE.equals(courseRepository.existsByCode(request.getCode()))) {
            log.info("Course with code : {} already exists", request.getCode());
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Course with code : " + request.getCode() + " already exist"
            );
        }

        if (Boolean.TRUE.equals(courseRepository.existsByNameOrLink(request.getName(), request.getLink()))) {
            log.info(
                "Course with name : {} or link : {} already exists", request.getName(), request.getLink()
            );
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name or link course already exist");
        }

        if (request.getPrice() == 0.0 && request.getType().equals(CourseType.PREMIUM)) {
            request.setType(CourseType.FREE);
        }

        if (request.getPrice() > 0.0 && request.getType().equals(CourseType.FREE)) {
            request.setType(CourseType.PREMIUM);
        }

        Course course = Course.builder()
            .code(request.getCode().toUpperCase())
            .name(request.getName())
            .author(request.getAuthor())
            .description(request.getDescription())
            .price(request.getPrice())
            .link(request.getLink())
            .category(request.getCategory())
            .type(request.getType())
            .level(request.getLevel())
            .build();

        HashSet<Intended> intendeds = new HashSet<>();

        mapIntendedStringToObject(request.getIntendeds(), course, intendeds);

        course.setIntendeds(intendeds);

        courseRepository.save(course);
        log.info("Course saved successfully");

        return toCourseResponse(course);
    }

    private static void mapIntendedStringToObject(Set<String> request,
                                                  Course course,
                                                  HashSet<Intended> intendeds) {
        request.forEach(purpose -> {
            Intended intended = Intended.builder()
                .purpose(purpose.trim())
                .course(course)
                .build();

            intendeds.add(intended);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourse(String code) {
        log.info("Getting course by id: {}", code);

        Course course = courseRepository.findByCode(code)
            .orElseThrow(() -> {
                log.warn("Course not found: {}", code);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND_MESSAGE);
            });

        log.info("Found course: {}", course);

        return toCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourse(int page, int size, String[] filters, List<CourseCategory> categories, List<CourseLevel> levels, CourseType type) {
        log.info("Fetching all available courses. Page: {}, Size: {}", page, size);

        PageRequest pageRequest = PageRequest.of(page, size);

        List<Course> filteredCourse = new ArrayList<>();
        List<Course> filteredByCategories = new ArrayList<>();
        List<Course> filteredByLevels = new ArrayList<>();
        List<Course> filteredByType = new ArrayList<>();

        if (categories != null && !categories.isEmpty()) {
            log.info("Checking categories: {}", categories);
            categories.forEach(
                courseCategory -> courseRepository
                    .findAll()
                    .stream()
                    .filter(course -> course.getCategory().equals(courseCategory))
                    .forEach(filteredByCategories::add)
            );
            filteredCourse = filteredByCategories;
        }

        if (levels != null && !levels.isEmpty()) {
            log.info("Checking levels: {}", levels);
            if (categories == null || categories.isEmpty()) {
                levels.forEach(
                    courseLevel -> courseRepository
                        .findAll()
                        .stream()
                        .filter(course -> course.getLevel().equals(courseLevel))
                        .forEach(filteredByLevels::add)
                );
            } else {
                levels.forEach(
                    courseLevel -> filteredByCategories
                        .stream()
                        .filter(course -> course.getLevel().equals(courseLevel))
                        .forEach(filteredByLevels::add)
                );
            }
            filteredCourse = filteredByLevels;
        }

        if (type != null) {
            log.info("Checking type: {}", type);
            if ((categories == null || categories.isEmpty()) && (levels == null || levels.isEmpty())) {
                courseRepository
                    .findAll()
                    .stream()
                    .filter(course -> course.getType().equals(type))
                    .forEach(filteredByType::add);
            } else if (levels == null || levels.isEmpty()) {
                filteredByCategories
                    .stream()
                    .filter(course -> course.getType().equals(type))
                    .forEach(filteredByType::add);
            } else {
                filteredByLevels
                    .stream()
                    .filter(course -> course.getType().equals(type))
                    .forEach(filteredByType::add);
            }
            filteredCourse = filteredByType;
        }

        if (categories == null && levels == null && type == null) {
            filteredCourse.addAll(courseRepository
                .findAll());
        }

        if (filters != null && filters.length != 0) {
            log.info("Checking filters");
            pageRequest = PageRequest.of(page, size, Sort.by(filters).descending());
        }

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filteredCourse.size());
        List<CourseResponse> pageContent = filteredCourse.stream()
            .map(CourseServiceImpl::toCourseResponse)
            .collect(Collectors.toList()).subList(start, end);

        return new PageImpl<>(pageContent, pageRequest, filteredCourse.size());
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(String code, UpdateCourseRequest request) {
        validationService.validate(request);

        Course course = courseRepository.findByCode(code)
            .orElseThrow(() -> {
                log.info("Course not found: {}", code);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND_MESSAGE);
            });

        log.info("Updating course with Code: {}", code);

        updateCourseProperties(course, request);
        courseRepository.save(course);

        log.info("Course updated successfully");

        return toCourseResponse(course);
    }

    private void updateCourseProperties(Course course, UpdateCourseRequest request) {
        if (Objects.nonNull(request.getName())) {
            course.setName(request.getName());
            log.info("Updated course name to: {}", request.getName());
        }

        if (Objects.nonNull(request.getAuthor())) {
            course.setAuthor(request.getAuthor());
            log.info("Updated course author to: {}", request.getAuthor());
        }

        if (Objects.nonNull(request.getDescription())) {
            course.setDescription(request.getDescription());
            log.info("Updated course description to: {}", request.getDescription());
        }

        if (Objects.nonNull(request.getPrice())) {
            course.setPrice(request.getPrice());
            log.info("Updated course price to: {}", request.getPrice());
        }

        if (Objects.nonNull(request.getLink())) {
            course.setLink(request.getLink());
            log.info("Updated course link to: {}", request.getLink());
        }

        if (Objects.nonNull(request.getCategory())) {
            course.setCategory(request.getCategory());
            log.info("Updated course category to: {}", request.getCategory());
        }

        if (Objects.nonNull(request.getType())) {
            course.setType(request.getType());
            log.info("Updated course type to: {}", request.getType());
        }

        if (Objects.nonNull(request.getLevel())) {
            course.setLevel(request.getLevel());
            log.info("Updated course level to: {}", request.getLevel());
        }

        if (Objects.nonNull(request.getIntendeds())) {
            HashSet<Intended> intendeds = new HashSet<>();

            mapIntendedStringToObject(request.getIntendeds(), course, intendeds);
            course.setIntendeds(intendeds);
        }
    }

    public static CourseResponse toCourseResponse(Course course) {
        return CourseResponse.builder()
            .code(course.getCode())
            .author(course.getAuthor())
            .name(course.getName())
            .description(course.getDescription())
            .price(course.getPrice())
            .link(course.getLink())
            .category(course.getCategory())
            .type(course.getType())
            .level(course.getLevel())
            .createdAt(TimeUtil.formatToString(course.getCreatedAt()))
            .updatedAt(TimeUtil.formatToString(course.getUpdatedAt()))
            .intendeds(course.getIntendeds().stream()
                .map(Intended::getPurpose)
                .collect(Collectors.toSet()))
            .build();
    }

    @Override
    @Transactional
    public void deleteCourse(String code) {
        log.info("Deleting course with Code: {}", code);

        Course course = courseRepository.findByCode(code)
            .orElseThrow(() -> {
                log.info("Course with Code {} not found", code);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND_MESSAGE);
            });

        log.info("Course found: {}", course);

        courseRepository.delete(course);

        log.info("Course deleted successfully");
    }

    @Override
    public Long numberOfCourse(CourseType courseType) {
        if (courseType == null) {
            return courseRepository.count();
        }
        return courseRepository.countByType(courseType);
    }

}
