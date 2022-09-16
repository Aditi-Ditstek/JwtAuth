package com.dits.dits.repository;

import com.dits.dits.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken ,Integer> {

    public RefreshToken findByRefreshToken(String token);
    public void deleteById(int id);
}
