package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.controller.RequestController;
import com.boctool.webservice_engine.entity.Webservice;
import com.boctool.webservice_engine.entity.WebserviceDTO;
import com.boctool.webservice_engine.repository.WebserviceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.boctool.webservice_engine.utils.Utilities.*;


@Service
public class WebserviceService {

    private final WebserviceRepository webserviceRepository;

    @Autowired
    public WebserviceService(WebserviceRepository webserviceRepository) {
        this.webserviceRepository = webserviceRepository;
    }

    private static final Set<String> ALLOWED_TYPES = Set.of("char", "integer", "date", "datetime", "array_char", "array_integer", "function");
    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    public ResponseEntity<Object> saveWebservice(List<WebserviceDTO> webserviceDTOS) {
        Map<String, Object> log = new HashMap<>();
        int i = 1;

        for (WebserviceDTO webserviceDTO : webserviceDTOS) {
            String sql = webserviceDTO.getWebservice();
            Map<String, String> parameters = webserviceDTO.getParameters();
            String normalizedQueryText = normalizeSql(sql);
            String webserviceMd5 = convertTextToMd5(normalizedQueryText);

            try {
                if (existsByWebserviceMd5(webserviceMd5)) {
                    Webservice webservice = webserviceRepository.findByWebserviceMd5(webserviceMd5);
                    log.put("Warning Obj " + i++ + " already exists", webservice);
                }

                validateQuery(sql, parameters);
                validateInputTypeParameters(parameters);

                Webservice webservice = new Webservice();
                webservice.setWebserviceMd5(webserviceMd5);
                webservice.setWebserviceText(sql);
                webservice.setWebserviceParams(new ObjectMapper().writeValueAsString(parameters));
                webserviceRepository.save(webservice);

                log.put("Obj " + i++, webservice);

            } catch (JsonProcessingException e) {
                log.put("Error Obj " + i++, "Processing parameters for query {} " + e.getMessage());
            } catch (Exception e) {
                log.put("Error Obj " + i++, "Saving query {} " + e.getMessage());
            }
        }

        return new ResponseEntity<>(log, HttpStatus.OK);
    }

    public static String normalizeSql(String sql) {
        sql = sql.replace(";", "");
        String[] parts = sql.split(REGEX_BRACE_PATTER);
        StringBuilder cleanSql = new StringBuilder();

        Pattern pattern = Pattern.compile(REGEX_BRACE_PATTER);
        Matcher matcher = pattern.matcher(sql);

        int i = 0;

        while (matcher.find()) {
            cleanSql.append(parts[i++].toLowerCase());
            cleanSql.append(matcher.group());
        }

        if (i < parts.length) {
            cleanSql.append(parts[i].toLowerCase());
        }

        return cleanSql.toString();
    }

    public static void validateInputTypeParameters(Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String paramType = entry.getValue().toLowerCase();
            if (!ALLOWED_TYPES.contains(paramType)) {
                throw new IllegalArgumentException("Invalid parameter type " + paramType + " for key {{" + entry.getKey()+ "}}");
            }
        }
    }

    private void validateQuery(String sql, Map<String, String> parameters) {
        Pattern pattern = Pattern.compile(REGEX_BRACE_PATTER);
        Matcher matcher = pattern.matcher(sql);
        Set<String> sqlParams = new HashSet<>();

        while (matcher.find()) {
            sqlParams.add(matcher.group(1));
        }
        for (String param : sqlParams) {
            if (!parameters.containsKey(param)) {
                throw new IllegalArgumentException("Parameter {{" + param + "}} in the SQL is not defined in the parameters key");
            }
        }
        for (String paramKey : parameters.keySet()) {
            if (!sqlParams.contains(paramKey)) {
                throw new IllegalArgumentException("Parameter {{" + paramKey + "}} is not defined in the SQL statement");
            }
        }

        logger.info("Parameters validated successfully");
    }

    public boolean existsByWebserviceMd5(String queryMd5) {
        return webserviceRepository.findByWebserviceMd5(queryMd5) != null;
    }

    public List<Webservice> findAllWebservices() {
        return webserviceRepository.findAll();
    }

    public void deleteAllWebservices() {
        webserviceRepository.deleteAll();
    }

    public Webservice findByWebserviceId(String id) {
        return webserviceRepository.findByWebserviceId(id);
    }

    public Webservice findByWebserviceMd5(String md5) {
        return webserviceRepository.findByWebserviceMd5(md5);
    }
}
