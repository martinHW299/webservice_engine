package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.entity.Source;
import com.boctool.webservice_engine.repository.SourceRepository;
import com.boctool.webservice_engine.utils.EncryptionUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SourceService {

    @Autowired
    private SourceRepository sourceRepository;

    @PostConstruct
    public void init() {
        loadAllSources();  // Load connections
    }

    //Map of DataSource
    Map<String, DataSource> dataSourceMap = new HashMap<>();
    private static final String PASSPHRASE = "nothingIsFullySecured";
    /*
    @Value("${encryption.passphrase}")
    private String passphrase;
    */ // Load passphrase from an environment variable or secure location

    public void loadAllSources(){
        List<Source> sources = sourceRepository.findAll();

        for (Source source : sources) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("oracle.jdbc.OracleDriver");
            hikariConfig.setJdbcUrl(source.getSourceUrl());
            hikariConfig.setUsername(source.getSourceUsr());
            String decryptedPassword;
            try {
                decryptedPassword = EncryptionUtils.decrypt(source.getSourcePwd(), PASSPHRASE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            hikariConfig.setPassword(decryptedPassword);

            hikariConfig.setMaximumPoolSize(source.getSourcePool());
            hikariConfig.setMinimumIdle(source.getSourcePool() / 2);

            //hikariConfig.setConnectionTimeout(30000);  // 30 segundos
            hikariConfig.setConnectionTimeout(source.getSourceTimeout());  // 30 segundos

            //hikariConfig.setIdleTimeout(600000);       // 10 minutos
            hikariConfig.setIdleTimeout(source.getSourceIdletimeout());       // 10 minutos

            //hikariConfig.setMaxLifetime(1800000);      // 30 minutos
            hikariConfig.setMaxLifetime(source.getSourceMaxlifetime());      // 30 minutos


            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            dataSourceMap.put(source.getSourceId(), dataSource);
        }
    }

    // Get DataSource by sourceId
    public DataSource getDataSourceById(String sourceId) {
        return dataSourceMap.get(sourceId);
    }





    public void saveSource(Source source) throws Exception {
        String encryptedPassword =  EncryptionUtils.encrypt(source.getSourcePwd(), PASSPHRASE);
        source.setSourcePwd(encryptedPassword);
        sourceRepository.save(source); // Save the source with encrypted password
    }

    public void saveListOfSources(List<Source> sources) {
        for (Source source : sources) {
            try {
                saveSource(source);  // Encrypt and save each source
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

    public void deleteAllSources(){
        sourceRepository.deleteAll();
    }

    public List<Source> findSourcesByStatus(String status){
        return sourceRepository.findSourceBySourceStatus(status);
    }
}
