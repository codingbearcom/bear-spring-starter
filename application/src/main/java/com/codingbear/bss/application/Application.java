/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class.
 */
@SpringBootApplication
public class Application {
    /**
     * Main entry point.
     *
     * @param args - application run arguments
     */
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }
}
