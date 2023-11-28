package com.github.k7.coursein.repository;

import com.github.k7.coursein.entity.OrderDetail;
import com.github.k7.coursein.entity.identifier.OrderDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {
}
