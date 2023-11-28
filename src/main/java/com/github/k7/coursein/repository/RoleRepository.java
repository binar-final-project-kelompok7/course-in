package com.github.k7.coursein.repository;

import com.github.k7.coursein.entity.Role;
import com.github.k7.coursein.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleRepository, Long> {

    Role findByName(UserRole userRole);

}
