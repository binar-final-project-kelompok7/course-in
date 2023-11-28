package com.github.k7.coursein.entity;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "courses")
@ToString(exclude = {"users", "intendeds", "orderDetails"})
@EqualsAndHashCode(exclude = {"users", "intendeds", "orderDetails"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 200)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(unique = true, nullable = false, length = 200)
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private CourseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CourseType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CourseLevel level;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "courses")
    private Set<User> users = new HashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "intended_courses",
        joinColumns = @JoinColumn(name = "course_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "intended_id", referencedColumnName = "id")
    )
    private Set<Intended> intendeds = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private List<OrderDetail> orderDetails = new LinkedList<>();

}
