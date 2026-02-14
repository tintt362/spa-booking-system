package com.trongtin.spabooking.repository;


import com.trongtin.spabooking.entity.TokenType;
import com.trongtin.spabooking.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

}