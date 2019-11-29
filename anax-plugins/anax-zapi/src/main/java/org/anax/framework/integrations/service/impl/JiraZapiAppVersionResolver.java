package org.anax.framework.integrations.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.integrations.service.AnaxZapiVersionResolver;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;


/**
 * This resolver expects that the application version resides in the
 * testing base URL page on a locator (to be specified by properties).
 * It then matches the version found with a Cycle in Zephyr for later processing
 */
@Slf4j
public class JiraZapiAppVersionResolver implements AnaxZapiVersionResolver {

    @Value("${jira.project:NOT_CONFIGURED}") private String webPage;
    @Value("${anax.target.url:NOT_CONFIGURED}") private String url;
    @Value("${jira.project.prefix:NOT_CONFIGURED}") private String projectPrefix;
    @Value("${project.version.selector:body div footer div div div div}") private String selector;
    @Value("${project.version.prefix.replacement:Version: }") private String versionPrefix;
    @Value("${project.version.suffix.replacement:-SNAPSHOT}") private String versionSuffix;



    @Override
    public String resolveAppVersion(){

        try {
            StringBuffer strBuilder = new StringBuffer(Jsoup.connect(url).get().selectFirst(selector).text().replace(versionPrefix,"").replace(versionSuffix,""))
                    .insert(0,projectPrefix+" ");
            return  strBuilder.toString();
        }catch (Exception e) {
            return null;
        }
    }
}
