package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.repository.CourseRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void getCourse_success() {
        when(courseRepository.findByName("test"))
            .thenReturn(Optional.ofNullable(Course.builder()
                .id(1L)
                .name("test")
                .description("bla bla bla")
                .price(50.0)
                .link("https://inilink.com")
                .category(CourseCategory.UIUX_DESIGN)
                .type(CourseType.FREE)
                .level(CourseLevel.BEGINNER)
                .build()));

        // melakukan pengetesan method getCourse apakah null atau tidak
        CourseResponse result = courseService.getCourse("test");
        Assertions.assertNotNull(result);

        Mockito.verify(courseRepository, Mockito.times(1)).findByName("test");
    }

    @Test
    void testGetAllAvailableCourse() {
        // Buat data kursus palsu (data dummy)
        List<Course> mockCourses = new ArrayList<>();

        mockCourses.add(Course.builder()
            .name("Test 1")
            .description("Deskripsi test 1")
            .price(100.0)
            .link("http://inilink.com/test1")
            .category(CourseCategory.ANDROID_DEVELOPMENT)
            .type(CourseType.PREMIUM)
            .level(CourseLevel.BEGINNER)
            .build());

        mockCourses.add(Course.builder()
            .name("Test 2")
            .description("Deskripsi test 2")
            .price(200.0)
            .link("http://inilink.com/test2")
            .category(CourseCategory.IOS_DEVELOPMENT)
            .type(CourseType.PREMIUM)
            .level(CourseLevel.INTERMEDIATE)
            .build());

        Page<Course> mockPage = new PageImpl<>(mockCourses);

        // melakukan simulasi repositori untuk mengembalikan halaman dummy saat metode findAll dipanggil
        when(courseRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(mockPage);

        Page<CourseResponse> resultPage = courseService.getAllAvailableCourse(0, 8);
        Assertions.assertEquals(mockCourses.size(), resultPage.getContent().size());

        Mockito.verify(courseRepository).findAll(PageRequest.of(0, 8));
    }

}
