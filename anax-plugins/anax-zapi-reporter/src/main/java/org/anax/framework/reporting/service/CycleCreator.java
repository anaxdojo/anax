package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.reporting.model.CycleClone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CycleCreator {

    //private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MMM/yy");

    @Value("${zapi.dateformatter.pattern:dd/MMM/yy}")
    String formatterPattern;
    
    @Autowired
    protected ZephyrZAPIService zapiService;

    /**
     * Create new cycle (clone existing one). If not exist create with given name, else append incremental number
     * @param projectName
     * @param versionName
     * @param cycleName
     */
    public String createCycleInVersion(String projectName, String versionName, String cycleName){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatterPattern);
        
        if (StringUtils.isEmpty(zapiService.getCycleId(projectName, versionName, cycleName))) {//Cycle not exist
            CycleClone cycleClone = new CycleClone(cycleName,dtf.format(LocalDateTime.now()));
            zapiService.cloneCycleToVersion(projectName, versionName, cycleClone, cycleName);
            log.info("Test Cycle created with name: " + cycleName + " at version: " + versionName);
        } else {
            log.info("Test Cycle with name: " + cycleName + " at version: " + versionName + " already exist,continue!!");
        }
        return cycleName;
    }
}
