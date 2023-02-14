package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.reporting.model.CycleClone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.isNull;

@Service
@Slf4j
public class CycleCreator {

    @Value("${zapi.dateformatter.pattern:dd/MMM/yy}")
    String formatterPattern;

    @Autowired
    protected ZephyrService zapiService;

    /**
     * Create new cycle (clone existing one). If not exist create with given name, else do nothing
     *
     * @param projectName
     * @param versionName
     * @param cycleName
     */
    public String createCycleInVersion(String projectName, String versionName, String cycleName) {
        if (zapiService instanceof ZephyrZAPIServerService) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatterPattern);
            return createCycleInVersion(projectName, versionName, cycleName, dtf.format(LocalDateTime.now()));
        } else {
            return createCycleInVersion(projectName, versionName, cycleName, String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000));
        }
    }

    private String createCycleInVersion(String projectName, String versionName, String cycleName, String date) {
        String cycleIdFound = zapiService.getCycleId(projectName, versionName, cycleName);
        if (isNull(cycleIdFound) || cycleIdFound.isEmpty()) {//Cycle not exist
            CycleClone cycleClone = new CycleClone(cycleName, date);
            zapiService.cloneCycleToVersion(projectName, versionName, cycleClone, cycleName);
            log.info("Test Cycle created with name: " + cycleName + " at version: " + versionName);
        } else {
            log.info("Test Cycle with name: " + cycleName + " at version: " + versionName + " already exist,continue!!");
        }
        return cycleName;
    }
}
