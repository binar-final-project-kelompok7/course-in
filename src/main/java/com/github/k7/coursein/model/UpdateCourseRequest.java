package com.github.k7.coursein.model;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCourseRequest {

    @Size(max = 200)
    private String name;

    @Size(max = 100)
    private String author;

    private String description;

    @Min(1)
    private Double price;

    @Size(max = 200)
    private String link;

    private CourseCategory category;

    private CourseType type;

    private CourseLevel level;

    private Set<String> intendeds;

}
