package com.github.k7.coursein.repository;

import com.github.k7.coursein.entity.Order;
import com.github.k7.coursein.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findAllByUser(User user);

    Page<Order> findAllByUser(User user, Pageable pageable);

}
