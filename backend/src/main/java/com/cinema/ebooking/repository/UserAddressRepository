package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAddressRepository
    extends JpaRepository<UserAddress, Long> {

    Optional<UserAddress> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
