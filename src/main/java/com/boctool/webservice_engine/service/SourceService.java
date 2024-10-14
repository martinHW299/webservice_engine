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
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SourceService {

    private final SourceRepository sourceRepository;

    @Autowired
    public SourceService(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
    @PostConstruct
    public void init() {
        loadAllSources();  // Load connections
    }

    Map<String, DataSource> dataSourceMap = new HashMap<>();
    private static final String PASSPHRASE = "nothingIsFullySecured";

    public void loadAllSources() {
        List<Source> sources = sourceRepository.findAll();
        System.out.println("Sources list: " + sources);
        for (Source source : sources) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("oracle.jdbc.OracleDriver");
            hikariConfig.setJdbcUrl(source.getSourceUrl());
            String decryptedPassword;
            String decryptedUsr;
            try {
                decryptedPassword = EncryptionUtils.decrypt(source.getSourcePwd(), PASSPHRASE);
                decryptedUsr = EncryptionUtils.decrypt(source.getSourceUsr(), PASSPHRASE);
            } catch (Exception e) {
                throw new RuntimeException("Error decrypting connection information", e);
            }
            hikariConfig.setPassword(decryptedPassword);
            hikariConfig.setUsername(decryptedUsr);
            hikariConfig.setMaximumPoolSize(source.getSourcePool());
            hikariConfig.setMinimumIdle(source.getSourcePool() / 2); //columna
            hikariConfig.setConnectionTimeout(source.getSourceTimeout());
            hikariConfig.setIdleTimeout(source.getSourceIdletimeout());
            hikariConfig.setMaxLifetime(source.getSourceMaxlifetime());
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
        String encryptedPassword = EncryptionUtils.encrypt(source.getSourcePwd(), PASSPHRASE);
        String encryptedSourceUsr = EncryptionUtils.encrypt(source.getSourceUsr(), PASSPHRASE);
        source.setSourceUsr(encryptedSourceUsr);
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

    public Source findSourceById(String sourceId) {
        return sourceRepository.findSourceBySourceId(sourceId);
    }

    public void deleteAllSources() {
        sourceRepository.deleteAll();
    }

    public List<Source> findSourcesByStatus(String status) {
        return sourceRepository.findSourceBySourceStatus(status);
    }
}
