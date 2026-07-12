package com.yusufakdogan.session_service.service;

import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.exception.ResourceNotFoundException;
import com.yusufakdogan.session_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void shouldDebitFromWallet() {
        User user = new User();
        user.setId(1L);
        user.setUsername("driver");
        user.setWalletBalance(new BigDecimal("500.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        BigDecimal newBalance = walletService.debit(1L, new BigDecimal("100.00"));

        assertThat(newBalance).isEqualByComparingTo("400.00");
        assertThat(user.getWalletBalance()).isEqualByComparingTo("400.00");
        verify(userRepository).save(user);
    }

    @Test
    void shouldDebitAndAllowNegativeBalance() {
        User user = new User();
        user.setId(1L);
        user.setUsername("driver");
        user.setWalletBalance(new BigDecimal("50.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        BigDecimal newBalance = walletService.debit(1L, new BigDecimal("100.00"));

        assertThat(newBalance).isEqualByComparingTo("-50.00");
        assertThat(user.getWalletBalance()).isEqualByComparingTo("-50.00");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFoundOnDebit() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.debit(999L, new BigDecimal("100.00")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldTopUpWallet() {
        User user = new User();
        user.setId(1L);
        user.setUsername("driver");
        user.setWalletBalance(new BigDecimal("100.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        BigDecimal newBalance = walletService.topUp(1L, new BigDecimal("50.00"));

        assertThat(newBalance).isEqualByComparingTo("150.00");
        assertThat(user.getWalletBalance()).isEqualByComparingTo("150.00");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFoundOnTopUp() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.topUp(999L, new BigDecimal("50.00")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetBalance() {
        User user = new User();
        user.setId(1L);
        user.setUsername("driver");
        user.setWalletBalance(new BigDecimal("500.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BigDecimal balance = walletService.getBalance(1L);

        assertThat(balance).isEqualByComparingTo("500.00");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFoundOnGetBalance() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getBalance(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
