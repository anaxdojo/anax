package org.anax.framework.reporting.service;

import org.anax.framework.reporting.model.Version;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AnaxZapiVersionResolver {

    /**
     * Get an app version from the application itself. This version is used later by the Cycle creator to
     * clone the version.
     *
     * @return
     */
    String resolveAppVersion();

    /**
     * Resolve the versionName (found from above) against the list of versions as returned from the ZAPI
     * call.
     *
     * @param versionName
     * @param versions
     * @return
     */
    default String getVersionFromJIRA(String versionName, ResponseEntity<List<Version>> versions) {
        Version version = versions.getBody().stream().filter(data -> data.getName().equals(versionName)).findFirst().orElse(null);
        return (version != null) ? version.getId() : "";
    }


}
