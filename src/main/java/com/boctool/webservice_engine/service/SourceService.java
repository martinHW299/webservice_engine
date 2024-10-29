package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.controller.RequestController;
import com.boctool.webservice_engine.entity.Source;
import com.boctool.webservice_engine.repository.SourceRepository;
import com.boctool.webservice_engine.utils.EncryptionUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;

@Service
public class SourceService {

    private final SourceRepository sourceRepository;
    private final EncryptionUtils encryptionUtils;

    @Autowired
    public SourceService(SourceRepository sourceRepository, EncryptionUtils encryptionUtils) {
        this.sourceRepository = sourceRepository;
        this.encryptionUtils = encryptionUtils;
    }

    Map<String, DataSource> dataSourceMap = new HashMap<>();

    @Value("${decryption.passphrase}")
    private String PASSPHRASE;

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
    @PostConstruct
    public void init() {
        loadAllSources();  // Load connections
    }

    public void loadAllSources() {
        List<Source> sources = sourceRepository.findAll();
        System.out.println("Sources list: " + sources);
        for (Source source : sources) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("oracle.jdbc.OracleDriver");
            hikariConfig.setJdbcUrl(source.getSourceUrl());
            String decryptedPassword;
            try {
                decryptedPassword = encryptionUtils.decrypt(source.getSourcePwd(), PASSPHRASE);
            } catch (Exception e) {
                throw new RuntimeException("Error decrypting connection information", e);
            }
            hikariConfig.setPassword(decryptedPassword);
            hikariConfig.setUsername(source.getSourceUsr());
            hikariConfig.setMaximumPoolSize(source.getSourcePool());
            hikariConfig.setMinimumIdle(source.getSourceMinIdle());
            hikariConfig.setConnectionTimeout(source.getSourceTimeout());
            hikariConfig.setIdleTimeout(source.getSourceIdleTimeout());
            hikariConfig.setMaxLifetime(source.getSourceMaxLifetime());
            hikariConfig.setAutoCommit(false);

            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            logger.info("Connection up: ",source.getSourceUrl());
            dataSourceMap.put(source.getSourceId(), dataSource);
        }
    }

    public DataSource getDataSourceById(String sourceId) {
        return dataSourceMap.get(sourceId);
    }

    public void saveSource(Source source) throws Exception {
        String encryptedPassword = encryptionUtils.encrypt(source.getSourcePwd(), PASSPHRASE);
        source.setSourceId(UUID.randomUUID().toString());
        source.setSourceUsr(source.getSourceUsr());
        source.setSourcePwd(encryptedPassword);
        sourceRepository.save(source); // Save the source with encrypted password
    }

    public void saveListOfSources(List<Source> sources) {
        for (Source source : sources) {
            try {
                saveSource(source);
            } catch (Exception e) {
                System.err.println("Failed to save source with ID: " + source.getSourceId() + ". Error: " + e.getMessage());
            }
        }
    }

    public List<Source> findAllSources() {
        return sourceRepository.findAll();
    }

    public Optional<Source> findSourceById(String sourceId) {
        return sourceRepository.findSourceBySourceId(sourceId);
    }

    public void deleteAllSources() {
        sourceRepository.deleteAll();
    }

    public List<Source> findSourcesByStatus(String status) {
        return sourceRepository.findSourceBySourceStatus(status);
    }
}
