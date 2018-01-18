package org.anax.framework.controllers;


import org.anax.framework.configuration.AnaxDriver;

/**
 * A WebControllerFactory is the factory that makes parallel testing possible.
 * Placeholder - to be used in future versions
 */
public interface WebControllerFactory {

    WebController getWebController(AnaxDriver anaxDriver, Integer defaultWaitSeconds) throws Exception;

}
