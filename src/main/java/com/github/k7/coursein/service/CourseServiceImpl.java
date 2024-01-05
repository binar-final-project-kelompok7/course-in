package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.entity.Intended;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseFilter;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import com.github.k7.coursein.repository.CourseRepository;
import com.github.k7.coursein.repository.UserRepository;
import com.github.k7.coursein.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

    private final UserRepository userRepository;

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
    public Page<CourseResponse> getAllCourse(CourseType type,
                                             Set<CourseFilter> filters,
                                             Set<CourseCategory> categories,
                                             Set<CourseLevel> levels,
                                             int page, int size) {
        log.info("Fetching all available courses. Page: {}, Size: {}", page, size);

        PageRequest pageRequest = PageRequest.of(page, size);
        List<Course> courses = courseRepository.findAll();
        return filteringAndPagingCourse(type, filters, categories, levels, pageRequest, courses);
    }

    @Override
    public Page<CourseResponse> getAllCourseUser(String username, CourseType type, Set<CourseFilter> filters, Set<CourseCategory> categories, Set<CourseLevel> levels, int page, int size) {
        log.info("Fetching all user courses. Page: {}, Size: {}", page, size);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        PageRequest pageRequest = PageRequest.of(page, size);
        List<Course> courses = courseRepository.findAllByUsers(user);
        return filteringAndPagingCourse(type, filters, categories, levels, pageRequest, courses);
    }

    private Page<CourseResponse> filteringAndPagingCourse(CourseType type, Set<CourseFilter> filters, Set<CourseCategory> categories, Set<CourseLevel> levels, PageRequest pageRequest, List<Course> courses) {
        List<Course> filteredCourses = filterCourses(type, filters, categories, levels, courses);
        List<CourseResponse> courseResponses = paginateAndConvertToResponse(pageRequest, filteredCourses);

        log.info("Returning {} courses on page {} of size {}",
            courseResponses.size(),
            pageRequest.getPageNumber(),
            pageRequest.getPageSize()
        );

        return new PageImpl<>(courseResponses, pageRequest, filteredCourses.size());
    }

    private List<Course> filterCourses(CourseType type, Set<CourseFilter> filters,
                                       Set<CourseCategory> categories, Set<CourseLevel> levels,
                                       List<Course> courses) {
        List<Course> filteredCourses;

        if (type == null && filters == null && categories == null && levels == null) {
            filteredCourses = courses;
        } else {
            filteredCourses = new LinkedList<>();
        }

        filteredCourses = applyFilterByType(type, courses, filteredCourses);
        filteredCourses = applyFilter(filters, courses, filteredCourses);
        filteredCourses = applyFilterByCategories(categories, courses, filteredCourses);
        filteredCourses = applyFilterByLevels(levels, courses, filteredCourses);

        return filteredCourses;
    }

    private List<CourseResponse> paginateAndConvertToResponse(PageRequest pageRequest, List<Course> filteredCourses) {
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filteredCourses.size());

        List<Course> pageContent = filteredCourses.subList(start, end);
        return pageContent.stream()
            .map(CourseServiceImpl::toCourseResponse)
            .collect(Collectors.toList());
    }

    private static List<Course> applyFilterByLevels(Set<CourseLevel> levels,
                                                    List<Course> courses,
                                                    List<Course> filteredCourses) {
        List<Course> filteredCoursesByLevels;
        if (levels != null && !levels.isEmpty()) {
            filteredCoursesByLevels = new LinkedList<>();
            if (!filteredCourses.isEmpty()) {
                levels
                    .forEach(level -> filteredCourses.stream()
                        .filter(course -> course.getLevel().equals(level))
                        .forEach(filteredCoursesByLevels::add));
            } else {
                levels
                    .forEach(level -> courses.stream()
                        .filter(course -> course.getLevel().equals(level))
                        .forEach(filteredCoursesByLevels::add));
            }
        } else {
            filteredCoursesByLevels = filteredCourses;
        }

        return filteredCoursesByLevels;
    }

    private static List<Course> applyFilterByCategories(Set<CourseCategory> categories,
                                                        List<Course> courses,
                                                        List<Course> filteredCourses) {
        List<Course> filteredCoursesByCategories;
        if (categories != null && !categories.isEmpty()) {
            filteredCoursesByCategories = new LinkedList<>();
            if (!filteredCourses.isEmpty()) {
                categories
                    .forEach(category -> filteredCourses.stream()
                        .filter(course -> course.getCategory().equals(category))
                        .forEach(filteredCoursesByCategories::add));
            } else {
                categories
                    .forEach(category -> courses.stream()
                        .filter(course -> course.getCategory().equals(category))
                        .forEach(filteredCoursesByCategories::add));
            }
        } else {
            filteredCoursesByCategories = filteredCourses;
        }

        return filteredCoursesByCategories;
    }

    private static List<Course> applyFilter(Set<CourseFilter> filters,
                                            List<Course> courses,
                                            List<Course> filteredCourses) {
        if (filters != null && !filters.isEmpty() && (filteredCourses.isEmpty())) {
            if (filters.contains(CourseFilter.NEWEST)) {
                courses.stream()
                    .sorted(Comparator.comparing((Course::getCreatedAt)).reversed())
                    .forEach(filteredCourses::add);
            }

            if (filters.contains(CourseFilter.POPULAR)) {
                courses.stream()
                    .filter(course -> !course.getUsers().isEmpty())
                    .sorted((o1, o2) -> o2.getUsers().size() - o1.getUsers().size())
                    .forEach(filteredCourses::add);
            }
        }

        return filteredCourses;
    }

    private static List<Course> applyFilterByType(CourseType type,
                                                  List<Course> courses,
                                                  List<Course> filteredCourses) {
        List<Course> filteredCoursesByType = filteredCourses;
        if (type != null) {
            filteredCoursesByType = courses.stream()
                .filter(course -> course.getType().equals(type))
                .collect(Collectors.toList());
        }

        return filteredCoursesByType;
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
    @Transactional(readOnly = true)
    public long countCourse() {
        return courseRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countPremiumCourse() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
            .filter(course -> course.getType().equals(CourseType.PREMIUM))
            .count();
    }

}
