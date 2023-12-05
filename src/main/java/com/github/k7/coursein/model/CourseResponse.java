package com.github.k7.coursein.model;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private String code;

    private String name;

    private String author;

    private String description;

    private Double price;

    private String link;

    private CourseCategory category;

    private CourseType type;

    private CourseLevel level;

    private String createdAt;

    private String updatedAt;

    private Set<String> intendeds;

}
