package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.AddCourseRequest;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.UpdateCourseRequest;
import com.github.k7.coursein.repository.CourseRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
class CourseServiceTest {

    @Mock
    ValidationService validationService;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void testGetCourse_success() {
        when(courseRepository.findByCode("WP1"))
            .thenReturn(Optional.ofNullable(Course.builder()
                .id(1L)
                .code("WP1")
                .name("test")
                .description("bla bla bla")
                .price(50.0)
                .link("https://inilink.com")
                .category(CourseCategory.UIUX_DESIGN)
                .type(CourseType.FREE)
                .level(CourseLevel.BEGINNER)
                .build()));

        CourseResponse result = courseService.getCourse("WP1");
        Assertions.assertNotNull(result);

        Mockito.verify(courseRepository, Mockito.times(1)).findByCode("WP1");
    }

    @Test
    void testGetAllCourse_success() {
        List<Course> mockCourses = new ArrayList<>();

        mockCourses.add(Course.builder()
            .id(1L)
            .code("WP1")
            .name("Test 1")
            .description("Deskripsi test 1")
            .price(100.0)
            .link("http://inilink.com/test1")
            .category(CourseCategory.ANDROID_DEVELOPMENT)
            .type(CourseType.PREMIUM)
            .level(CourseLevel.BEGINNER)
            .build());

        mockCourses.add(Course.builder()
            .id(2L)
            .code("WP2")
            .name("Test 2")
            .description("Deskripsi test 2")
            .price(200.0)
            .link("http://inilink.com/test2")
            .category(CourseCategory.IOS_DEVELOPMENT)
            .type(CourseType.PREMIUM)
            .level(CourseLevel.INTERMEDIATE)
            .build());

        Page<Course> mockPage = new PageImpl<>(mockCourses);

        when(courseRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        Page<CourseResponse> resultPage = courseService.getAllCourse(null, null, null, null, 0, 8);
        Assertions.assertEquals(mockCourses.size(), resultPage.getContent().size());

        Mockito.verify(courseRepository).findAll(PageRequest.of(0, 8));
    }

    @Test
    void testDeleteCourse_success() {
        Course mockCourse = Course.builder()
            .id(1L)
            .code("WP1")
            .name("Test 1")
            .price(100.0)
            .link("http://inilink.com/test1")
            .category(CourseCategory.WEB_DEVELOPMENT)
            .build();

        when(courseRepository.findByCode("WP1")).thenReturn(java.util.Optional.of(mockCourse));

        courseService.deleteCourse("WP1");

        Mockito.verify(courseRepository, Mockito.times(1)).findByCode("WP1");
        Mockito.verify(courseRepository, Mockito.times(1)).delete(mockCourse);
    }

    @Test
    void testValidationCourse_failed_field() {
        Mockito.doThrow(ConstraintViolationException.class).when(validationService).validate(Mockito.any());
        AddCourseRequest cr = AddCourseRequest.builder()
            .name("")
            .build();
        Assertions.assertThrows(ConstraintViolationException.class, () -> validationService.validate(cr));
    }

    @Test
    void testUpdateCourse_success() {
        Course existingCourse = Course.builder()
            .id(1L)
            .code("WP1")
            .name("Past Course")
            .description("test past description")
            .price(100.0)
            .link("https://inilink.com")
            .category(CourseCategory.WEB_DEVELOPMENT)
            .type(CourseType.FREE)
            .level(CourseLevel.BEGINNER)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        UpdateCourseRequest updateCourseRequest = UpdateCourseRequest.builder()
            .name("New Course Name")
            .description("This is new course")
            .link("https://newlink.com")
            .category(CourseCategory.PRODUCT_MANAGEMENT)
            .type(CourseType.PREMIUM)
            .level(CourseLevel.INTERMEDIATE)
            .build();

        doNothing().when(validationService).validate(any(UpdateCourseRequest.class));

        when(courseRepository.findByCode("WP1")).thenReturn(Optional.of(existingCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(existingCourse);

        CourseResponse courseResponse = courseService.updateCourse("WP1", updateCourseRequest);

        Mockito.verify(courseRepository, Mockito.times(1)).findById(1L);

        Assertions.assertEquals("WP1", courseResponse.getCode());
        Assertions.assertEquals(updateCourseRequest.getName(), courseResponse.getName());
        Assertions.assertEquals(updateCourseRequest.getDescription(), courseResponse.getDescription());
        Assertions.assertEquals(existingCourse.getPrice(), courseResponse.getPrice());
    }

}
