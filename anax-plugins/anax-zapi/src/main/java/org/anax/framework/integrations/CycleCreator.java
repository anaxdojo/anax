package org.anax.framework.integrations;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.integrations.pojo.CycleClone;
import org.anax.framework.integrations.pojo.CycleInfo;
import org.anax.framework.integrations.service.ZapiServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CycleCreator {

    @Autowired
    protected ZapiServiceImpl zapiService;

    /**
     * Create new cycle (clone existing one). If not exist create with given name, else append incremental number
     * @param projectName
     * @param versionName
     * @param cycleName
     */
    public String createCycleInVersion(String projectName, String versionName, String cycleName, CycleInfo cycleInfo) throws NoSuchFieldException {
        if (StringUtils.isEmpty(zapiService.getCycleId(projectName, versionName, cycleName))) {//Cycle not exist
            CycleClone cycleClone = new CycleClone(cycleName);
            zapiService.cloneCycleToVersion(projectName, versionName, cycleClone, cycleName);
            log.info("Test Cycle created with name: " + cycleName + " at version: " + versionName);
        } else {
            log.info("Test Cycle with name: " + cycleName + " at version: " + versionName + " already exist,continue!!");
        }
        return cycleName;
    }

    /**
     * Find all cycle names containing as keyword the cycle name
     *
     * @param avalableNames
     * @param cycleName
     * @return
     */
    public List<String> createSubListMatchingName(List<String> avalableNames, String cycleName) {
        return avalableNames.stream().filter(data->data.contains(cycleName)).collect(Collectors.toList());
    }
}