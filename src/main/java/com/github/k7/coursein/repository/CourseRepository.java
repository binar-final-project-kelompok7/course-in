package com.github.k7.coursein.repository;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByNameOrLink(String name, String link);

    Page<Course> findAllByCategoryIn(List<CourseCategory> courseCategory, Pageable pageable);

    Page<Course> findAllByLevelIn(List<CourseLevel> courseLevels, Pageable pageable);

    Page<Course> findAllByType(CourseType courseType, Pageable pageable);

    Page<Course> findAllByCategoryInAndLevelIn(List<CourseCategory> courseCategory,
                                               List<CourseLevel> courseLevels,
                                               Pageable pageable);

    Page<Course> findAllByCategoryInAndType(List<CourseCategory> courseCategory,
                                            CourseType courseType,
                                            Pageable pageable);

    Page<Course> findAllByLevelInAndType(List<CourseLevel> courseLevels,
                                         CourseType courseType,
                                         Pageable pageable);

    Page<Course> findAllByCategoryInAndLevelInAndType(List<CourseCategory> courseCategory,
                                                      List<CourseLevel> courseLevels,
                                                      CourseType courseType, Pageable pageable);

    Long countByType(CourseType courseType);
}
