package com.github.k7.coursein.model;

import com.github.k7.coursein.entity.Intended;
import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCourseRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    private String description;

    @Min(0)
    private Double price;

    @NotBlank
    @Size(max = 200)
    private String link;

    @NotNull
    @Size(max = 100)
    private CourseCategory category;

    @NotNull
    @Size(max = 50)
    private CourseType type;

    @NotNull
    @Size(max = 50)
    private CourseLevel level;

    @NotNull
    private Set<Intended> intendeds;
}
