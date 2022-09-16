package com.dits.dits.repository;

import com.dits.dits.model.Role;
import com.dits.dits.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {

   public Role findByName(String name);
}
