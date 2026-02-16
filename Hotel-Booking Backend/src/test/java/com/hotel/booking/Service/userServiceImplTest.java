package com.hotel.booking.Service;

import com.hotel.booking.Entity.User;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class userServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private userServiceImpl userService;

    // --------------------------------------
    // getUserByid
    // --------------------------------------
    @Test
    void getUserByid_success() {

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByid(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserByid_notFound() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserByid(99L)
        );
    }

    // --------------------------------------
    // loadUserByUsername
    // --------------------------------------
    @Test
    void loadUserByUsername_success() {

        User user = User.builder()
                .id(2L)
                .email("user@test.com")
                .password("secret")
                .build();

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails =
                userService.loadUserByUsername("user@test.com");

        assertNotNull(userDetails);
        assertEquals("user@test.com", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_notFound() {

        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.loadUserByUsername("missing@test.com")
        );
    }
}
