// RefreshTokenRepository.java
package com.kuspidsamples.repository;

import com.kuspidsamples.entity.RefreshToken;
import com.kuspidsamples.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUser(User user);
}