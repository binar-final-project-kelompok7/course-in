package com.github.k7.coursein.repository;

import com.github.k7.coursein.entity.Intended;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntendedRepository extends JpaRepository<Intended, Long> {
}
