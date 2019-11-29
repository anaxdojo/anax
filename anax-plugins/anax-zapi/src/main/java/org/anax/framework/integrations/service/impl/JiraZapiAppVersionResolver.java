package org.anax.framework.integrations.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.integrations.service.AnaxZapiVersionResolver;


/**
 * This resolver expects that the application version resides in the
 * testing base URL page on a locator (to be specified by properties).
 * It then matches the version found with a Cycle in Zephyr for later processing
 */
@Slf4j
public class JiraZapiAppVersionResolver implements AnaxZapiVersionResolver {

    //base url
    //locator
    @Override
    public String resolveAppVersion() {
        return null;
    }
}
