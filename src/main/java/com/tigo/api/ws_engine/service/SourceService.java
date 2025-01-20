package com.tigo.api.ws_engine.service;

import com.tigo.api.ws_engine.controller.SourceController;
import com.tigo.api.ws_engine.entity.Source;
import com.tigo.api.ws_engine.repository.SourceRepository;
import com.tigo.api.ws_engine.utils.ApiResponse;
import com.tigo.api.ws_engine.utils.EncryptionTool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;


@Service
@RequiredArgsConstructor
public class SourceService {

    private static final Logger logger = LoggerFactory.getLogger(SourceController.class);
    private final SourceRepository sourceRepository;
    private final EncryptionTool encryptionTool;
    private final Map<UUID, HikariDataSource> dataSourceMap = new HashMap<>();
    @Value("${decryption.passphrase}")
    private String PASSPHRASE;


    @PostConstruct
    public void initialize() {
        loadAllSources();
    }

    @PreDestroy
    public void cleanup() {
        closeAllSources();
    }

    private void loadAllSources() {
        List<Source> activeSources = findActiveSources();
        activeSources.forEach(this::initializeConnectionSource);
    }

    public List<Source> findActiveSources() {
        return sourceRepository.findSourceByStatus("AC");
    }

    public DataSource getDataSourceById(String sourceId) {
        return Optional.ofNullable(dataSourceMap.get(sourceId))
                .orElseThrow(() -> new NoSuchElementException("No active data source found for source ID: " + sourceId));
    }

    public Source findSourceById(UUID sourceId) {
        return sourceRepository.findById(sourceId)
                .orElseThrow(() -> new NoSuchElementException("Source not found with ID: " + sourceId));
    }

    public ResponseEntity<?> saveSource(Source source) {
        try {
            String encryptedPassword = encryptionTool.encrypt(source.getPwd(), PASSPHRASE);
            source.setPwd(encryptedPassword);
            source.setStatus("AC");
            sourceRepository.save(source);

            initializeConnectionSource(source); // Initialize connection for the new source
            return ResponseEntity.ok(new ApiResponse(200, "Source saved successfully", source));
        } catch (Exception e) {
            logger.error("Failed to save source: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(500, "Failed to save source", null));
        }
    }

    private void initializeConnectionSource(Source source) {
        try {
            HikariDataSource dataSource = createHikariDataSource(source);
            dataSourceMap.put(source.getId(), dataSource);
        } catch (Exception e) {
            logger.error("Failed to initialize connection for source ID: {}", source.getId());
        }
    }

    private HikariDataSource createHikariDataSource(Source source) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(source.getClassName());
        config.setJdbcUrl(source.getUrl());
        config.setUsername(source.getUsr());
        config.setPassword(encryptionTool.decrypt(source.getPwd(), PASSPHRASE));
        config.setMaximumPoolSize(Optional.ofNullable(source.getPoolSize()).orElse(10));
        config.setMinimumIdle(Optional.ofNullable(source.getMinIdle()).orElse(2));
        config.setConnectionTimeout(Optional.ofNullable(source.getTimeout()).orElse(30000)); // Default: 30 seconds

        Optional.ofNullable(source.getIdleTimeout()).ifPresent(config::setIdleTimeout);
        Optional.ofNullable(source.getMaxLifetime()).ifPresent(config::setMaxLifetime);

        config.setAutoCommit(false);
        return new HikariDataSource(config);
    }

    private void closeAllSources() {
        dataSourceMap.forEach((id, dataSource) -> {
            try {
                dataSource.close();
                logger.info("Closed data source for source ID: {}", id);
            } catch (Exception e) {
                logger.error("Failed to close data source for source ID: {}", id, e);
            }
        });
        dataSourceMap.clear();
    }


    public ResponseEntity<?> updateSource(UUID sourceId, Source updatedSource) {
        try {
            Source existingSource = findSourceById(sourceId);
            String encryptedPassword = encryptionTool.encrypt(updatedSource.getPwd(), PASSPHRASE);

            existingSource.setTag(updatedSource.getTag());
            existingSource.setUrl(updatedSource.getUrl());
            existingSource.setUsr(updatedSource.getUsr());;
            existingSource.setPwd(encryptedPassword);
            existingSource.setStatus(updatedSource.getStatus());
            existingSource.setPoolSize(updatedSource.getPoolSize());
            existingSource.setMinIdle(updatedSource.getMinIdle());
            existingSource.setTimeout(updatedSource.getTimeout());
            existingSource.setIdleTimeout(updatedSource.getIdleTimeout());
            existingSource.setMaxLifetime(updatedSource.getMaxLifetime());

            sourceRepository.save(existingSource);
            initializeConnectionSource(existingSource); // Reinitialize connection

            return ResponseEntity.ok(new ApiResponse(200, "Source updated successfully", existingSource));
        } catch (Exception e) {
            logger.error("Failed to update source: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(500, "Failed to update source", null));
        }
    }

    public void deleteAllSources() {
        closeAllSources();
        sourceRepository.deleteAll();
        logger.info("Deleted all sources and closed all connections.");
    }
}
