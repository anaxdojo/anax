package org.anax.framework.controllers;

import org.anax.framework.configuration.AnaxDriver;
import org.anax.framework.util.HttpCookie;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ThreadAwareWebController implements WebController {
    private ThreadLocal<WebController> webControllers;

    public ThreadAwareWebController(Supplier<WebController> supplier) {
        this.webControllers = ThreadLocal.withInitial(supplier);
    }

    protected WebController getThreadDelegate() {
        return webControllers.get();
    }

    public void enableActionsLogging() {
        getThreadDelegate().enableActionsLogging();
    }

    public void disableActionsLogging() {
        getThreadDelegate().disableActionsLogging();
    }

    public void close() {
        getThreadDelegate().close();
    }

    public void quit() {
        getThreadDelegate().quit();
    }

    public WebElement waitForElement(String locator, long waitSeconds) {
        return getThreadDelegate().waitForElement(locator, waitSeconds);
    }

    public void waitForElementInvisibility(String locator, long waitSeconds) {
        getThreadDelegate().waitForElementInvisibility(locator, waitSeconds);
    }

    public WebElement waitForElementPresence(String locator, long waitSeconds) {
        return getThreadDelegate().waitForElementPresence(locator, waitSeconds);
    }

    public List<WebElement> findElements(String locator, long waitSeconds) {
        return getThreadDelegate().findElements(locator, waitSeconds);
    }

    public void input(String locator, String value) {
        getThreadDelegate().input(locator, value);
    }

    public void press(String locator) {
        getThreadDelegate().press(locator);
    }

    public void pressAndWaitForPageToLoad(String locator) {
        getThreadDelegate().pressAndWaitForPageToLoad(locator);
    }

    public void pressAndWaitForElement(String pressLocator, String elementToWaitLocator, long waitSeconds) {
        getThreadDelegate().pressAndWaitForElement(pressLocator, elementToWaitLocator, waitSeconds);
    }

    public void pressAndClickOkInAlert(String locator) {
        getThreadDelegate().pressAndClickOkInAlert(locator);
    }

    public void pressAndClickOkInAlertNoPageLoad(String locator) {
        getThreadDelegate().pressAndClickOkInAlertNoPageLoad(locator);
    }

    public void pressAndClickCancelInAlert(String locator) {
        getThreadDelegate().pressAndClickCancelInAlert(locator);
    }

    public void select(String locator, String option) {
        getThreadDelegate().select(locator, option);
    }

    public void selectByValue(String locator, String value) {
        getThreadDelegate().selectByValue(locator, value);
    }

    public void multiSelectAdd(String locator, String option) {
        getThreadDelegate().multiSelectAdd(locator, option);
    }

    public Object executeJavascript(String js, Object... args) {
        return getThreadDelegate().executeJavascript(js, args);
    }

    public void waitForCondition(String jscondition, long waitSeconds) {
        getThreadDelegate().waitForCondition(jscondition, waitSeconds);
    }

    public void clear(String locator) {
        getThreadDelegate().clear(locator);
    }

    public Actions getBuilder() {
        return getThreadDelegate().getBuilder();
    }

    public void mouseOver(String locator) {
        getThreadDelegate().mouseOver(locator);
    }

    public void mouseUp(String locator) {
        getThreadDelegate().mouseUp(locator);
    }

    public void mouseDown(String locator) {
        getThreadDelegate().mouseDown(locator);
    }

    public void click(String locator) {
        getThreadDelegate().click(locator);
    }

    public void doubleClick(String locator) {
        getThreadDelegate().doubleClick(locator);
    }

    public void highlight(String locator) {
        getThreadDelegate().highlight(locator);
    }

    public void highlight(String locator, String color) {
        getThreadDelegate().highlight(locator, color);
    }

    public File takeScreenShot() throws IOException {
        return getThreadDelegate().takeScreenShot();
    }

    public String getText(String locator) {
        return getThreadDelegate().getText(locator);
    }

    public void getFocus(String locator) {
        getThreadDelegate().getFocus(locator);
    }

    public String getSelectedOption(String locator) {
        return getThreadDelegate().getSelectedOption(locator);
    }

    public List<String> getSelectedOptions(String locator) {
        return getThreadDelegate().getSelectedOptions(locator);
    }

    public String getInputValue(String locator) {
        return getThreadDelegate().getInputValue(locator);
    }

    public boolean isAlertPresent() {
        return getThreadDelegate().isAlertPresent();
    }

    public boolean isTextPresent(String value) {
        return getThreadDelegate().isTextPresent(value);
    }

    public boolean isTextNotPresent(String value) {
        return getThreadDelegate().isTextNotPresent(value);
    }

    public boolean isComponentEditable(String locator) {
        return getThreadDelegate().isComponentEditable(locator);
    }

    public boolean isComponentDisabled(String locator) {
        return getThreadDelegate().isComponentDisabled(locator);
    }

    public boolean isComponentPresent(String locator) {
        return getThreadDelegate().isComponentPresent(locator);
    }

    public boolean isComponentPresent(String locator, long seconds) {
        return getThreadDelegate().isComponentPresent(locator, seconds);
    }

    public boolean isComponentNotPresent(String locator) {
        return getThreadDelegate().isComponentNotPresent(locator);
    }

    public boolean isComponentVisible(String locator) {
        return getThreadDelegate().isComponentVisible(locator);
    }

    public boolean isComponentVisible(String locator, long seconds) {
        return getThreadDelegate().isComponentVisible(locator, seconds);
    }

    public boolean isComponentNotVisible(String locator) {
        return getThreadDelegate().isComponentNotVisible(locator);
    }

    public boolean isComponentNotVisible(String locator, long seconds) {
        return getThreadDelegate().isComponentNotVisible(locator, seconds);
    }

    public boolean isComponentSelected(String locator) {
        return getThreadDelegate().isComponentSelected(locator);
    }

    public boolean isComponentNotSelected(String locator) {
        return getThreadDelegate().isComponentNotSelected(locator);
    }

    public void pressLinkName(String linkName) {
        getThreadDelegate().pressLinkName(linkName);
    }

    public void pressLinkNameAndWaitForPageToLoad(String linkName) {
        getThreadDelegate().pressLinkNameAndWaitForPageToLoad(linkName);
    }

    public void pressLinkNameAndClickOkInAlert(String linkName) {
        getThreadDelegate().pressLinkNameAndClickOkInAlert(linkName);
    }

    public void pressLinkNameAndClickOkInAlertNoPageLoad(String linkName) {
        getThreadDelegate().pressLinkNameAndClickOkInAlertNoPageLoad(linkName);
    }

    public void pressLinkNameAndClickCancelInAlert(String linkName) {
        getThreadDelegate().pressLinkNameAndClickCancelInAlert(linkName);
    }

    public void typeKeys(String locator, String value) {
        getThreadDelegate().typeKeys(locator, value);
    }

    public void keyDown(String locator, WebController.KeyInfo thekey) {
        getThreadDelegate().keyDown(locator, thekey);
    }

    public void keyUp(String locator, WebController.KeyInfo thekey) {
        getThreadDelegate().keyUp(locator, thekey);
    }

    public void keyPress(String locator, WebController.KeyInfo thekey) {
        getThreadDelegate().keyPress(locator, thekey);
    }

    public void keyDown(WebController.KeyInfo thekey) {
        getThreadDelegate().keyDown(thekey);
    }

    public void keyUp(WebController.KeyInfo thekey) {
        getThreadDelegate().keyUp(thekey);
    }

    public void keyPress(WebController.KeyInfo thekey) {
        getThreadDelegate().keyPress(thekey);
    }

    public void clickOkInAlert() {
        getThreadDelegate().clickOkInAlert();
    }

    public void promptInputPressOK(String inputMessage) {
        getThreadDelegate().promptInputPressOK(inputMessage);
    }

    public void promptInputPressCancel(String inputMessage) {
        getThreadDelegate().promptInputPressCancel(inputMessage);
    }

    public void clickCancelInAlert() {
        getThreadDelegate().clickCancelInAlert();
    }

    public void navigate(String url) {
        getThreadDelegate().navigate(url);
    }

    public void refresh() {
        getThreadDelegate().refresh();
    }

    public String getTableElementRowPosition(String locator, String elementName) {
        return getThreadDelegate().getTableElementRowPosition(locator, elementName);
    }

    public int getNumberOfTotalRows(String locator) {
        return getThreadDelegate().getNumberOfTotalRows(locator);
    }

    public int getNumberOfTotalColumns(String locator) {
        return getThreadDelegate().getNumberOfTotalColumns(locator);
    }

    public Map<String, Map<String, String>> getTableInfo(String locator, int numberOfColumns) {
        return getThreadDelegate().getTableInfo(locator, numberOfColumns);
    }

    public List<List<String>> getTableInfoAsList(String locator) {
        return getThreadDelegate().getTableInfoAsList(locator);
    }

    public String getTableElementTextUnderHeader(String locator, String elementName, String headerName) {
        return getThreadDelegate().getTableElementTextUnderHeader(locator, elementName, headerName);
    }

    public String getTableElementTextForRowAndColumn(String locator, String row, String column) {
        return getThreadDelegate().getTableElementTextForRowAndColumn(locator, row, column);
    }

    public String getTableHeaderPosition(String locator, String headerName) {
        return getThreadDelegate().getTableHeaderPosition(locator, headerName);
    }

    public String getTableElementColumnPosition(String locator, String elementName) {
        return getThreadDelegate().getTableElementColumnPosition(locator, elementName);
    }

    public List<String> getTableRecordsUnderHeader(String locator, String headerName) {
        return getThreadDelegate().getTableRecordsUnderHeader(locator, headerName);
    }

    public String[][] getTableElements2DArray(String locator) {
        return getThreadDelegate().getTableElements2DArray(locator);
    }

    public String getTableElementSpecificHeaderLocator(String locator, String elementName, String headerName) {
        return getThreadDelegate().getTableElementSpecificHeaderLocator(locator, elementName, headerName);
    }

    public String getTableElementSpecificRowAndColumnLocator(String locator, String row, String column) {
        return getThreadDelegate().getTableElementSpecificRowAndColumnLocator(locator, row, column);
    }

    public String getAttributeValue(String locator, String attribute) {
        return getThreadDelegate().getAttributeValue(locator, attribute);
    }

    public HttpCookie getCookieByName(String name) {
        return getThreadDelegate().getCookieByName(name);
    }

    public List<HttpCookie> getAllCookies() {
        return getThreadDelegate().getAllCookies();
    }

    public void dragAndDrop(String locatorFrom, String locatorTo) {
        getThreadDelegate().dragAndDrop(locatorFrom, locatorTo);
    }

    public void switchToLatestWindow() {
        getThreadDelegate().switchToLatestWindow();
    }

    public String getAlertText() {
        return getThreadDelegate().getAlertText();
    }

    public List<String> getAllListOptions(String locator) {
        return getThreadDelegate().getAllListOptions(locator);
    }

    public void selectFrame(String frameID) {
        getThreadDelegate().selectFrame(frameID);
    }

    public void selectFrameMain() {
        getThreadDelegate().selectFrameMain();
    }

    public void maximizeWindow() {
        getThreadDelegate().maximizeWindow();
    }

    public int getNumberOfElementsMatchLocator(String locator) {
        return getThreadDelegate().getNumberOfElementsMatchLocator(locator);
    }

    public void moveToElement(String locator, int x, int y) {
        getThreadDelegate().moveToElement(locator, x, y);
    }

    public void moveToElement(String locator) {
        getThreadDelegate().moveToElement(locator);
    }

    public void moveByOffset(int xOffset, int yOffset) {
        getThreadDelegate().moveByOffset(xOffset, yOffset);
    }

    public void waitForAjaxComplete(long milliseconds) {
        getThreadDelegate().waitForAjaxComplete(milliseconds);
    }

    public static void sleep(long milliseconds) {
        WebController.sleep(milliseconds);
    }

    public String getCurrentUrl() {
        return getThreadDelegate().getCurrentUrl();
    }

    public void dragAndDrop(String locatorFrom, int xOffset, int yOffset) {
        getThreadDelegate().dragAndDrop(locatorFrom, xOffset, yOffset);
    }

    public Point getElementPosition(String locator) {
        return getThreadDelegate().getElementPosition(locator);
    }

    public String getPageSource() {
        return getThreadDelegate().getPageSource();
    }


}
