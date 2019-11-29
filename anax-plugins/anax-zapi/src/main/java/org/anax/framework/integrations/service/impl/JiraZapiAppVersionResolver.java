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


    //base url
    //locator
    @Override
    public String resolveAppVersion(){

        try {
            return  Jsoup.connect(webPage).get().html();
        }catch (Exception e) {
            return null;
        }
    }
}
