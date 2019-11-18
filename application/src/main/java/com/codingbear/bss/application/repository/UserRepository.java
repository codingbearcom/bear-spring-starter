/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application.repository;

import com.codingbear.bss.application.entity.TestUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A collection of methods for storing and querying {@link TestUser} to/from the database.
 */
@Repository
public interface UserRepository extends JpaRepository<TestUser, Integer> {

}
