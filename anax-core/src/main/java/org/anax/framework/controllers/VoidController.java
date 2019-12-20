package org.anax.framework.controllers;


import org.anax.framework.util.HttpCookie;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class VoidController implements WebController {

    @Override
    public void enableActionsLogging() {
        // Auto-generated method stub

    }

    @Override
    public void disableActionsLogging(){
        // Auto-generated method stub

    }

    @Override
    public void close() {
        // Auto-generated method stub

    }

    @Override
    public void quit() {
        // Auto-generated method stub

    }


    @Override
    public WebElement waitForElement(String locator, long waitSeconds) {
        // Auto-generated method stub
        return null;
    }



    @Override
    public void waitForElementInvisibility(String locator, long waitSeconds) {
        // Auto-generated method stub

    }


    @Override
    public WebElement waitForElementPresence(String locator, long waitSeconds) {
        // Auto-generated method stub
        return null;
    }


    @Override
    public void input(String locator, String value) {
        // Auto-generated method stub

    }

    @Override
    public void press(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void pressAndWaitForPageToLoad(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void pressAndWaitForElement(String pressLocator,
                                       String elementToWaitLocator, long waitSeconds) {
        // Auto-generated method stub

    }


    @Override
    public void pressAndClickOkInAlert(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void pressAndClickOkInAlertNoPageLoad(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void pressAndClickCancelInAlert(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void select(String locator, String option) {
        // Auto-generated method stub

    }

    @Override
    public void selectByValue(String locator, String value) {
        // Auto-generated method stub

    }

    @Override
    public void multiSelectAdd(String locator, String option) {
        // Auto-generated method stub

    }

    @Override
    public Object executeJavascript(String js, Object... args) {
        // Auto-generated method stub
        return null;
    }


    @Override
    public void waitForCondition(String jscondition, long waitSeconds) {
        // Auto-generated method stub

    }

    @Override
    public void clear(String locator) {
        // Auto-generated method stub

    }

    @Override
    public Actions getBuilder() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public void mouseOver(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void mouseUp(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void mouseDown(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void click(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void doubleClick(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void highlight(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void highlight(String locator, String color) {
        // Auto-generated method stub

    }

    @Override
    public File takeScreenShot() throws IOException {
        // Auto-generated method stub
        return null;
    }

    @Override
    public byte[] takeScreenShotAsBytes() throws IOException {
        return new byte[0];
    }

    @Override
    public String getText(String locator) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public void getFocus(String locator) {
        // Auto-generated method stub

    }

    @Override
    public String getSelectedOption(String locator) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getSelectedOptions(String locator) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getInputValue(String locator) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAlertPresent() {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTextPresent(String value) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTextNotPresent(String value) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentEditable(String locator) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentDisabled(String locator) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentPresent(String locator) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentPresent(String locator, long seconds) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentNotPresent(String locator) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentVisible(String locator) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentVisible(String locator, long seconds) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentNotVisible(String locator) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentNotVisible(String locator, long seconds) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentSelected(String locator) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponentNotSelected(String locator) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public void pressLinkName(String linkName) {
        // Auto-generated method stub

    }

    @Override
    public void pressLinkNameAndWaitForPageToLoad(String linkName) {
        // Auto-generated method stub

    }

    @Override
    public void pressLinkNameAndClickOkInAlert(String linkName) {
        // Auto-generated method stub

    }

    @Override
    public void pressLinkNameAndClickOkInAlertNoPageLoad(String linkName) {
        // Auto-generated method stub

    }

    @Override
    public void pressLinkNameAndClickCancelInAlert(String linkName) {
        // Auto-generated method stub

    }

    @Override
    public void typeKeys(String locator, String value) {
        // Auto-generated method stub

    }

    @Override
    public void keyDown(String locator, KeyInfo thekey) {
        // Auto-generated method stub

    }

    @Override
    public void keyUp(String locator, KeyInfo thekey) {
        // Auto-generated method stub

    }

    @Override
    public void keyPress(String locator, KeyInfo thekey) {
        // Auto-generated method stub

    }

    @Override
    public void keyDown(KeyInfo thekey) {
        // Auto-generated method stub

    }

    @Override
    public void keyUp(KeyInfo thekey) {
        // Auto-generated method stub

    }

    @Override
    public void keyPress(KeyInfo thekey) {
        // Auto-generated method stub

    }

    @Override
    public void clickOkInAlert() {
        // Auto-generated method stub

    }

    @Override
    public void promptInputPressOK(String inputMessage) {
        // Auto-generated method stub

    }

    @Override
    public void promptInputPressCancel(String inputMessage) {
        // Auto-generated method stub

    }

    @Override
    public void clickCancelInAlert() {
        // Auto-generated method stub

    }

    @Override
    public void navigate(String url) {
        // Auto-generated method stub

    }

    @Override
    public void refresh() {
        // Auto-generated method stub

    }

    @Override
    public String getTableElementRowPosition(String locator, String elementName) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public int getNumberOfTotalRows(String locator) {
        // Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfTotalColumns(String locator) {
        // Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Map<String, String>> getTableInfo(String locator,
                                                         int numberOfColumns) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public List<List<String>> getTableInfoAsList(String locator) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getTableElementTextUnderHeader(String locator,
                                                 String elementName, String headerName) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getTableElementTextForRowAndColumn(String locator,
                                                     String row, String column) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getTableHeaderPosition(String locator, String headerName) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getTableElementColumnPosition(String locator,
                                                String elementName) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getTableRecordsUnderHeader(String locator,
                                                   String headerName) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String[][] getTableElements2DArray(String locator) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getTableElementSpecificHeaderLocator(String locator,
                                                       String elementName, String headerName) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getTableElementSpecificRowAndColumnLocator(String locator,
                                                             String row, String column) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getAttributeValue(String locator, String attribute) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public HttpCookie getCookieByName(String name) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public List<HttpCookie> getAllCookies() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public void dragAndDrop(String locatorFrom, String locatorTo) {
        // Auto-generated method stub

    }

    @Override
    public void switchToLatestWindow() {
        // Auto-generated method stub

    }

    @Override
    public String getAlertText() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getAllListOptions(String locator) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public void selectFrame(String frameID) {
        // Auto-generated method stub

    }

    @Override
    public void selectFrameMain() {
        // Auto-generated method stub

    }

    @Override
    public void maximizeWindow() {
        // Auto-generated method stub

    }

    @Override
    public int getNumberOfElementsMatchLocator(String locator) {
        // Auto-generated method stub
        return 0;
    }

    @Override
    public void moveToElement(String locator, int x, int y) {
        // Auto-generated method stub

    }

    @Override
    public void moveToElement(String locator) {
        // Auto-generated method stub

    }

    @Override
    public void moveByOffset(int xOffset, int yOffset) {
        // Auto-generated method stub

    }

    @Override
    public void waitForAjaxComplete(long milliseconds) {
        // Auto-generated method stub

    }



    @Override
    public String getCurrentUrl() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public void dragAndDrop(String locatorFrom, int xOffset, int yOffset) {
        // Auto-generated method stub

    }

    @Override
    public Point getElementPosition(String locator) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getPageSource() {
        // Auto-generated method stub
        return null;
    }
    @Override
    public List<WebElement> findElements(String locator, long waitSeconds) {
        return null;
    }
}