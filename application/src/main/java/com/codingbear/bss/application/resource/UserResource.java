/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application.resource;

import com.codingbear.bss.application.resource.entity.UserResponse;
import com.codingbear.bss.application.service.UserService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The implementation of the User REST endpoints.
 */
@Component
@Path("/user")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class UserResource {

    private final UserService userService;

    @Inject
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns information about specified user.
     *
     * @param userId id of user.
     * @return information about user.
     */
    @GET
    @Path("{userId}")
    public UserResponse getUser(@PathParam("userId") Integer userId) {
        return userService.getUser(userId);
    }
}
