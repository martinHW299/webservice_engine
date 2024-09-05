package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.entity.Source;
import com.boctool.webservice_engine.repository.SourceRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SourceService {

    @Autowired
    SourceRepository sourceRepository;

    @PostConstruct
    public void init() {
        loadAllSources();  // Load connections
    }

    //Map of DataSource
    Map<String, DataSource> dataSourceMap = new HashMap<>();

    public void loadAllSources(){
        List<Source> sources = sourceRepository.findAll();

        for (Source source : sources){
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("oracle.jdbc.OracleDriver");
            hikariConfig.setJdbcUrl(source.getSourceUrl());
            hikariConfig.setUsername(source.getSourceUsr());
            hikariConfig.setPassword(source.getSourcePwd());

            hikariConfig.setMaximumPoolSize(source.getSourcePool());
            hikariConfig.setMinimumIdle(source.getSourcePool()/2);

            hikariConfig.setConnectionTimeout(30000);  // 30 segundos
            hikariConfig.setIdleTimeout(600000);       // 10 minutos
            hikariConfig.setMaxLifetime(1800000);      // 30 minutos

            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            dataSourceMap.put(source.getSourceId(), dataSource);
        }
    }

    // Get DataSource by sourceId
    public DataSource getDataSourceById(String sourceId) {
        return dataSourceMap.get(sourceId);
    }

    public Map<String, Object> saveSources(List<Source> sources){
        List<Source> savedSources = sourceRepository.saveAll(sources);

        Map<String, Object> response = new HashMap<>();
        response.put("state", "SUCCESS");
        response.put("savedData", savedSources);

        return response;
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
