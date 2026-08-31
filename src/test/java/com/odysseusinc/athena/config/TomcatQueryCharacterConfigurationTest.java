/*
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.odysseusinc.athena.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;

class TomcatQueryCharacterConfigurationTest {

    @Test
    void squareBracketConfigurationBindsAsTwoRelaxedQueryCharacters() {

        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                "server.tomcat.relaxed-query-chars", "[,]")));

        TomcatServerProperties properties = binder.bind(
                "server.tomcat", Bindable.of(TomcatServerProperties.class))
                .orElseThrow(() -> new AssertionError("Tomcat properties did not bind"));

        assertEquals(List.of('[', ']'), properties.getRelaxedQueryChars());
    }
}
