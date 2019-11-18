/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application.service;

import com.codingbear.bss.application.entity.TestUser;
import com.codingbear.bss.application.repository.UserRepository;
import com.codingbear.bss.application.resource.entity.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private UserService userService;

    @BeforeEach
    void setUp() {
        TestUser user = new TestUser();
        user.setId(2);
        user.setFirstName("John");
        user.setLastName("Doe");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        userService = new UserService(userRepository);
    }

    @Test
    void getUser() {
        // just sample test
        final UserResponse user = userService.getUser(2);
        assertEquals(Integer.valueOf(2), user.getId());
        assertEquals("John Doe", user.getName());
    }
}
