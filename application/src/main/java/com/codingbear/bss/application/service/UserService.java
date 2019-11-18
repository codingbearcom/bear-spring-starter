/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application.service;

import com.codingbear.bss.application.entity.TestUser;
import com.codingbear.bss.application.repository.UserRepository;
import com.codingbear.bss.application.resource.entity.UserResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.Optional;

/**
 * Logic for working with users.
 */
@Component
public class UserService {
    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Find user by given id.
     *
     * @param userId id of user.
     * @return user information.
     * @throws NotFoundException when specified user id not exist.
     */
    @Transactional(readOnly = true)
    public UserResponse getUser(Integer userId) {
        final Optional<TestUser> user = userRepository.findById(userId);

        return user.map(UserService::toUserResponse).orElseThrow(NotFoundException::new);
    }

    private static UserResponse toUserResponse(TestUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getFirstName() + " " + user.getLastName());
        return response;
    }
}
