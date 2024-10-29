package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.controller.RequestController;
import com.boctool.webservice_engine.entity.*;
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
import java.util.stream.Collectors;

import static com.boctool.webservice_engine.utils.Utilities.*;


@Service
public class WebserviceService {

    private final WebserviceRepository webserviceRepository;

    @Autowired
    public WebserviceService(WebserviceRepository webserviceRepository) {
        this.webserviceRepository = webserviceRepository;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    public ResponseEntity<Object> saveWebservices(List<WebserviceDTO> webserviceDTOS) {
        Map<String, Object> log = new HashMap<>();
        int i = 1;

        for (WebserviceDTO webserviceDTO : webserviceDTOS) {
            String sql = webserviceDTO.getWebservice();
            Map<String, String> parameters = webserviceDTO.getParameters();
            String normalizedQueryText = normalizeSql(sql);
            String webserviceMd5 = convertTextToMd5(normalizedQueryText
                    .concat(parameters.toString())
//                    .concat(webserviceDTO.getCode())
//                    .concat(webserviceDTO.getName())
//                    .concat(webserviceDTO.getDescription())
            );

            try {
                existsByWebserviceMd5(webserviceMd5);
                validateQuery(sql, parameters);
                validateInputTypeParameters(parameters);

                Webservice webservice = new Webservice();
                webservice.setWebserviceMd5(webserviceMd5);
                webservice.setWebserviceText(sql);
                webservice.setWebserviceParams(new ObjectMapper().writeValueAsString(parameters));
                webservice.setWebserviceCode(webserviceDTO.getCode());
                webservice.setWebserviceName(webserviceDTO.getName());
                webservice.setWebserviceDescription(webserviceDTO.getDescription());
                webservice.setWebserviceCreationUid(webserviceDTO.getUserCode());
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


    public ResponseEntity<Object> updateWebservice(String id, WebserviceDTO updatedWebservice) {
        Map<String, Object> log = new HashMap<>();

        String sql = updatedWebservice.getWebservice();
        Map<String, String> parameters = updatedWebservice.getParameters();
        String normalizedQueryText = normalizeSql(sql);
        String webserviceMd5 = convertTextToMd5(normalizedQueryText
                .concat(parameters.toString())
//                .concat(updatedWebservice.getCode())
//                .concat(updatedWebservice.getName())
//                .concat(updatedWebservice.getDescription())
        );

        try {
            existsByWebserviceMd5(webserviceMd5);
            validateQuery(sql, parameters);
            validateInputTypeParameters(parameters);

            Optional<Webservice> existingWebserviceOpt = webserviceRepository.findByWebserviceId(id);
            if (existingWebserviceOpt.isPresent()) {
                Webservice webservice = existingWebserviceOpt.get();

                webservice.setWebserviceMd5(webserviceMd5);
                webservice.setWebserviceText(sql);
                webservice.setWebserviceParams(objectMapper.writeValueAsString(parameters));
                webservice.setWebserviceCode(updatedWebservice.getCode());
                webservice.setWebserviceName(updatedWebservice.getName());
                webservice.setWebserviceDescription(updatedWebservice.getDescription());
                webservice.setWebserviceChangeUid(updatedWebservice.getUserCode());

                webserviceRepository.save(webservice);
                log.put("UpdatedWebservice", webservice);

                return ResponseEntity.ok(log);
            } else {
                log.put("Error", "Webservice with ID " + id + " not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(log);
            }

        } catch (JsonProcessingException e) {
            log.put("Error", "Error processing parameters for webservice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(log);
        } catch (Exception e) {
            log.put("Error", "Error updating webservice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(log);
        }
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

            // Check if the paramType exists in AllowedType
            boolean isValidType = Arrays.stream(VarTypes.values())
                    .map(VarTypes::getType)
                    .collect(Collectors.toSet())
                    .contains(paramType);

            if (!isValidType) {
                throw new IllegalArgumentException("Invalid parameter type " + paramType + " for key {{" + entry.getKey() + "}}");
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

    public void existsByWebserviceMd5(String queryMd5) {
        Optional<Webservice> webservice = webserviceRepository.findByWebserviceMd5(queryMd5);
        if (webservice.isPresent()) {
            throw new IllegalArgumentException("Webservice already exists");
        }
    }

    public List<Webservice> findAllWebservices() {
        return webserviceRepository.findAll();
    }

    public void deleteAllWebservices() {
        webserviceRepository.deleteAll();
    }

    public Optional<Webservice> findByWebserviceId(String id) {
        return webserviceRepository.findByWebserviceId(id);
    }

    public Optional<Webservice> findByWebserviceMd5(String md5) {
        return webserviceRepository.findByWebserviceMd5(md5);
    }
}
