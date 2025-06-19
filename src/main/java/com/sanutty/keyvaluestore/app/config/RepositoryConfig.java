package com.sanutty.keyvaluestore.app.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanutty.keyvaluestore.app.repositories.KeyValueRepository;
import com.sanutty.keyvaluestore.app.repositories.MemStoreKeyValueRepository;
import com.sanutty.keyvaluestore.app.repositories.RDBMSKeyValueRepository;

@Configuration
public class RepositoryConfig
{
    @Bean
    @ConditionalOnProperty(name = "repository.implementaion.class", havingValue = "RDBM")
    public KeyValueRepository getRDBMSKeyValueRepository() {
        return new RDBMSKeyValueRepository();
    }
  
    @Bean
    @ConditionalOnProperty(name = "repository.implementaion.class", havingValue = "MemStore")
    public KeyValueRepository getMemStoreKeyValueRepository() {
        return new MemStoreKeyValueRepository();
    }

}
