package com.dits.dits.repository;

import com.dits.dits.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtUserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

}
