/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application.service;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * A JUnit extension that clears database data after each test method.
 */
public class DbClearingExtension implements AfterEachCallback {
    private static final String TABLES_QUERY = "SELECT input_table_name AS truncate_query FROM(SELECT table_schema || '.' || table_name AS input_table_name FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog', 'information_schema') AND table_schema NOT LIKE 'pg_toast%') AS information;\n";

    @Override
    public void afterEach(ExtensionContext context) {
        clearDatabase(context);
    }

    private void clearDatabase(ExtensionContext context) {
        final ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        final JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);

        final List<String> tables = jdbcTemplate.query(TABLES_QUERY, (r, i) -> r.getString(1));

        StringBuilder queryBuilder = new StringBuilder();
        for (String table : tables) {
            queryBuilder.append("TRUNCATE ");
            queryBuilder.append(table);
            queryBuilder.append(" CASCADE; ");
        }

        jdbcTemplate.execute(queryBuilder.toString());
    }
}
