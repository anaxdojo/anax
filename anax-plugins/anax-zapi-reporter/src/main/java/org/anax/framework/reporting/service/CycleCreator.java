package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.reporting.model.CycleClone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.isNull;

@Service
@Slf4j
public class CycleCreator {

    @Value("${zapi.dateformatter.pattern:dd/MMM/yy}")
    protected String formatterPattern;

    @Value("${jira.clone.cycle:true}")//Variable for parallel tc execution not for suite level
    protected Boolean clone_cycle;

    @Autowired
    protected ZephyrService zapiService;

    /**
     * Create new cycle (clone existing one). If not exist create with given name, else do nothing
     * If test are running parallel (not on suite level)
     *
     * @param projectName
     * @param versionName
     * @param cycleName
     */
    public String createCycleInVersion(String projectName, String versionName, String cycleName) {
        String cycleIdFound = zapiService.getCycleId(projectName, versionName, cycleName, true);
        if ((isNull(cycleIdFound) || cycleIdFound.isEmpty()) && clone_cycle) {//Cycle not exist
            CycleClone cycleClone = new CycleClone(cycleName, getStartDate());
            zapiService.cloneCycleToVersion(projectName, versionName, cycleClone, cycleName);
            log.info("Test Cycle created with name: " + cycleName + " at version: " + versionName);
        } else {
            log.info("Test Cycle with name: " + cycleName + " at version: " + versionName + " already exist,continue!!");
        }
        return cycleName;
    }

    /**
     * Returns the correct cycle start date depending on the {@link ZephyrService} instance
     *
     * @return
     */
    private String getStartDate() {
        String date;
        if (zapiService instanceof ZephyrZAPIServerService) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatterPattern);
            date = dtf.format(LocalDateTime.now());
        } else {
            date = String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000);
        }
        return date;
    }
}
