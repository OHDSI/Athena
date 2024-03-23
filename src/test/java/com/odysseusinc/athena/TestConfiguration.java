package com.odysseusinc.athena;


import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.util.CDMVersion;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testcontainers.shaded.com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;



@SpringBootConfiguration
@EnableAutoConfiguration
@Configuration
@Profile("test")
@ComponentScan(
        basePackageClasses = TestConfiguration.class
)
public class TestConfiguration {


    @Bean
    public Executor asyncExecutor() {
        return MoreExecutors.newDirectExecutorService();
    }

    @Primary
    @Bean(name = "deltaExecutor")
    public Executor deltaExecutor() {
        return MoreExecutors.newDirectExecutorService();
    }

}
