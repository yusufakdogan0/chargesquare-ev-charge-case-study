package com.yusufakdogan.session_service.service;

import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.exception.ResourceNotFoundException;
import com.yusufakdogan.session_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;

    /**
     * Debits the specified amount from the user's wallet.
     * Allows negative balance (user can go into debt).
     *
     * @param userId The user ID
     * @param amount The amount to debit (must be positive)
     * @return The new wallet balance after debit
     */
    @Transactional
    public BigDecimal debit(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BigDecimal newBalance = user.getWalletBalance().subtract(amount);
        user.setWalletBalance(newBalance);
        userRepository.save(user);

        log.info("Debited {} from user {}. New balance: {}", amount, userId, newBalance);

        return newBalance;
    }

    /**
     * Tops up the user's wallet with the specified amount.
     *
     * @param userId The user ID
     * @param amount The amount to add (must be positive)
     * @return The new wallet balance after top-up
     */
    @Transactional
    public BigDecimal topUp(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BigDecimal newBalance = user.getWalletBalance().add(amount);
        user.setWalletBalance(newBalance);
        userRepository.save(user);

        log.info("Topped up {} for user {}. New balance: {}", amount, userId, newBalance);

        return newBalance;
    }

    /**
     * Gets the current wallet balance for a user.
     *
     * @param userId The user ID
     * @return The current wallet balance
     */
    public BigDecimal getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getWalletBalance();
    }
}
