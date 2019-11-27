package org.anax.framework.integrations;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.integrations.pojo.CycleClone;
import org.anax.framework.integrations.pojo.CycleInfo;
import org.anax.framework.integrations.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CycleCreator {

    @Autowired
    protected ServiceImpl zapiService;

    /**
     * Create new cycle (clone existing one). If not exist create with given name, else append incremental number
     * @param projectName
     * @param versionName
     * @param cycleName
     */
    public String createCycleInVersion(String projectName, String versionName, String cycleName, CycleInfo cycleInfo) throws NoSuchFieldException {
        if(StringUtils.isEmpty(zapiService.getCycleId(projectName,versionName,cycleName))){
            CycleClone cycleClone = CycleClone.builder().name(cycleName).build();
            zapiService.cloneCycleToVersion(projectName, versionName, cycleClone,cycleName);
            zapiService.updateCycleInfo(projectName, versionName,cycleName,cycleInfo);
            log.info("Test Cycle created with name: " + cycleName + " at version: " + versionName);
            return cycleName;
        }
        else{
            if(zapiService.getCycleBuildNumber(projectName,versionName,zapiService.getLatestCycleName(projectName, versionName)).equals(cycleInfo.getJiraBuildNo())){
                log.info("Test Cycle with name: " + zapiService.getLatestCycleName(projectName, versionName) + " at version: " + versionName+" already exist,continue!!");
                return zapiService.getLatestCycleName(projectName, versionName);
            }
            List<String> names = createSubListMatchingName(getAvailableCyclesNames(projectName, versionName), cycleName);
            String newCycleName = cycleName + " " + names.size();
            CycleClone cycleClone = CycleClone.builder().name(newCycleName).build();
            zapiService.cloneCycleToVersion(projectName, versionName, cycleClone,cycleName);
            zapiService.updateCycleInfo(projectName, versionName,newCycleName,cycleInfo);
            return newCycleName;
        }
    }

    /**
     * Find all cycle names containing as keyword the cycle name
     * @param avalableNames
     * @param cycleName
     * @return
     */
    public List<String> createSubListMatchingName(List<String> avalableNames, String cycleName){
        return avalableNames.stream().filter(data->data.contains(cycleName)).collect(Collectors.toList());
    }


    /**
     * Get available cycles names of a specific version
     * @param projectName
     * @param versionName
     * @return
     */
    public List<String> getAvailableCyclesNames(String projectName,String versionName){
        return zapiService.getVersionCycleNames(projectName,versionName);
    }

}