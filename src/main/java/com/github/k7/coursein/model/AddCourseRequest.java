package com.github.k7.coursein.model;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCourseRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 100)
    private String author;

    private String description;

    @NotNull
    @Min(0)
    private Double price;

    @NotBlank
    @Size(max = 200)
    private String link;

    @NotNull
    private CourseCategory category;

    @NotNull
    private CourseType type;

    @NotNull
    private CourseLevel level;

    @NotEmpty
    private Set<String> intendeds;

}
