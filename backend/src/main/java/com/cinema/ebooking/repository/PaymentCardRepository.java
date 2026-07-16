package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentCardRepository
    extends JpaRepository<PaymentCard, Long> {

    List<PaymentCard> findAllByUserIdOrderByCardSlotAsc(
        Long userId
    );

    void deleteAllByUserId(Long userId);

    long countByUserId(Long userId);
}
