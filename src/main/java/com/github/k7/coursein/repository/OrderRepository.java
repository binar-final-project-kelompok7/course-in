package com.github.k7.coursein.repository;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.entity.Order;
import com.github.k7.coursein.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    boolean existsByUserAndCourse(User user, Course course);

    Page<Order> findAllByUser(User user, Pageable pageable);

}
