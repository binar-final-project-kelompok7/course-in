package com.github.k7.coursein.model;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCourseRequest {

    @Size(max = 255)
    private String name;

    private String description;

    @Min(1)
    private Double price;

    @NotBlank
    @Size(max = 200)
    private String link;

    @NotNull
    @Size(max = 50)
    private CourseCategory category;

    @NotNull
    @Size(max = 50)
    private CourseType type;

    @NotNull
    @Size(max = 50)
    private CourseLevel level;

}
