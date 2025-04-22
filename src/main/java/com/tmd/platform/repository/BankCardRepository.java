package com.tmd.platform.repository;

import com.tmd.platform.domain.BankCard;
import com.tmd.platform.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {
    List<BankCard> findByUser(User user);
    Optional<BankCard> findByUserAndIsDefault(User user, boolean isDefault);
    boolean existsByUser(User user);
} 