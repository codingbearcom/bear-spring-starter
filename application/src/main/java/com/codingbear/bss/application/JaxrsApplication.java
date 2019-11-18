/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application;

import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS application class.
 */
@Component
@ApplicationPath("/v1/")
public class JaxrsApplication extends javax.ws.rs.core.Application {
}
