package org.anax.framework.testing;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.controllers.WebController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class Verify {

    @Autowired
    WebController    controller;

    /** The log. */
    /** The pass color. */
    private static final String PASS_COLOR = "Lime";
    /** The fail color. */
    private static final String FAIL_COLOR = "OrangeRed";

    /*************************************Constants used for logging messages*****************************************************/

    private static final String ELEMENT_LOCATOR = "The element with locator '";

    private static final String TEXT = "The text '";

    private static final String IS_VISIBLE = "' is visible!";

    private static final String IS_NOT_VISIBLE = "' is not visible!";

    private static final String FOUND = "' was found!";

    private static final String NOT_FOUND = "' was not found!";

    private static final String FOUND_WITH_TEXT = "' was found with text '";

    private static final String NOT_FOUND_WITH_TEXT = "' was not found with text '";

    private static final String FOUND_CONTAINING_TEXT = "' was found to contain text '";

    private static final String NOT_FOUND_CONTAINING_TEXT = "' was not found to contain text '";

    private static final String FOUND_WITH_VALUE = "' was found with value '";

    private static final String NOT_FOUND_WITH_VALUE = "' was not found with value '";

    private static final String FOUND_CONTAINING_VALUE = "' was found to contain value '";

    private static final String NOT_FOUND_CONTAINING_VALUE = "' was not found to contain value '";

    private static final String FOUND_EDITABLE = "' was found editable!";

    private static final String FOUND_DISABLED = "' was found disabled!";

    private static final String FOUND_SELECTED = "' was found selected!";

    private static final String NOT_FOUND_SELECTED = "' was not found selected!";

    private static final String IS_EQUAL = "' is equal with '";

    private static final String IS_NOT_EQUAL = "' is not equal with '";

    private static final String ELEMENT_LOCATOR_ATTRIBUTE = "' attribute of element with locator '";

    private static final String CONTAINED_IN_LIST = "' is contained in the list'!";

    private static final String NOT_CONTAINED_IN_LIST = "' is not contained in the list'!";

    private static final String FOUND_WITH_OPTIONS = "' was found with options '";

    private static final String NOT_FOUND_WITH_OPTIONS = "' was not found options '";

    private static final String FOUND_WITH_SELECTED_OPTIONS = "' was found with selected options '";

    private static final String NOT_FOUND_WITH_SELECTED_OPTIONS = "' was not found with selected options '";

    private static final String FOUND_WITH_SELECTED_OPTION = "' was found with selected option '";

    private static final String NOT_FOUND_WITH_SELECTED_OPTION = "' was not found with selected option '";

    private static final String FOUND_ALERT_WITH_MESSAGE = "There was an alert with message '";

    private static final String NOT_FOUND_ALERT_WITH_MESSAGE = "There was an alert with message '";

    private static final String TABLE_ELEMENT = "The table element '";


    /** Instantiates a new verify.*/
    public Verify() {
        System.setProperty("org.uncommons.reportng.escape-output", "false");
    }

    /**
     * Info.
     *
     * @param message the message
     */
    public void info(String message) {
        log.info(message);
    }

    /**
     * Warn.
     *
     * @param message the message
     */
    public void warn(String message) {
        log.warn(message);
    }

    /**
     * Error.
     *
     * @param message the message
     */
    public void error(String message) {
        log.error(message);
        try {
            controller.takeScreenShot();
        } catch (IOException ioe ) {
            log.info("Failed to create screenshot : "+ ioe.getMessage());
        }
    }

    /**
     * Check that text is present.
     *
     * @param text the text
     */
    public void textPresent(String text) {
        try {
            Assert.isTrue(controller.isTextPresent(text),"Assertion failed, condition is not true.Check...");
            info(TEXT + text + FOUND);
        } catch (IllegalArgumentException e) {
            error(TEXT + text + NOT_FOUND);
            throw e;
        }
    }

    /**
     * Check that text is not present.
     *
     * @param text the text under examination
     */
    public void textNotPresent(String text) {
        try {
            Assert.isTrue(controller.isTextNotPresent(text),"Assertion failed, condition is not true.Check...");
            info(TEXT + text + NOT_FOUND);
        } catch (IllegalArgumentException e) {
            error(TEXT + text + FOUND);
            throw e;
        }
    }

    /**
     * Check that an Element is present
     *
     * @param locator the locator of an element
     */
    public void elementPresent(String locator) {
        try {
            Assert.isTrue(controller.isComponentPresent(locator),"Assertion failed, condition is not true.Check...");
            info(ELEMENT_LOCATOR + locator + FOUND);
        } catch (IllegalArgumentException e) {
            error(ELEMENT_LOCATOR + locator + NOT_FOUND);
            throw e;
        }
    }

    /**
     * Check that an Element is present for a specific timeframe.
     *
     * @param locator the locator Check that an Element is present
     * @param seconds the time in seconds that the element must remain present
     */
    public void elementPresent(String locator,long seconds) {
        try {
            Assert.isTrue(controller.isComponentPresent(locator,seconds),"Assertion failed, condition is not true.Check...");
            info(ELEMENT_LOCATOR + locator + FOUND);
        } catch (IllegalArgumentException e) {
            error(ELEMENT_LOCATOR + locator + NOT_FOUND);
            throw e;
        }
    }

    /**
     * Check that an Element is not present.
     *
     * @param locator the locator of the element
     */
    public void elementNotPresent(String locator) {
        try {
            Assert.isTrue(controller.isComponentNotPresent(locator),"Assertion failed, condition is not true.Check...");
            info(ELEMENT_LOCATOR + locator + NOT_FOUND);
        } catch (IllegalArgumentException e) {
            error(ELEMENT_LOCATOR + locator + FOUND);
            throw e;
        }
    }

    /**
     * Check that an Element is visible.
     *
     * @param locator the locator of an element
     */
    public void elementVisible(String locator) {
        try {
            Assert.isTrue(controller.isComponentVisible(locator),"Assertion failed, condition is not true.Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + IS_VISIBLE);
        } catch (IllegalArgumentException e) {
            error(ELEMENT_LOCATOR + locator + IS_NOT_VISIBLE);
            throw e;
        }
    }

    /**
     * Check that an Element is visible.
     *
     * @param locator the locator of the element
     * @param seconds the least time in seconds that element must remain visible
     */
    public void elementVisible(String locator,long seconds) {
        try {
            Assert.isTrue(controller.isComponentVisible(locator,seconds),"Assertion failed, condition is not true.Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + IS_VISIBLE);
        } catch (IllegalArgumentException e) {
            error(ELEMENT_LOCATOR + locator + IS_NOT_VISIBLE);
            throw e;
        }
    }

    /**
     * Check that an Element is not visible.
     *
     * @param locator the locator of the element
     */
    public void elementNotVisible(String locator) {
        try {
            Assert.isTrue(controller.isComponentNotVisible(locator),"Assertion failed, condition is not true.Check...");
            info(ELEMENT_LOCATOR + locator + IS_NOT_VISIBLE);
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + IS_VISIBLE);
            throw e;
        }
    }

    /**
     * Check the value of an element .
     *
     * @param locator the element locator
     * @param expectedValue the expected value
     */
    public void value(String locator, String expectedValue) {
        try {
            Assert.isTrue(controller.getInputValue(locator).equals(expectedValue),"Assertion failed, Values are not equals.Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_WITH_VALUE + expectedValue + "'!");
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + NOT_FOUND_WITH_VALUE + expectedValue + "'!");
            throw e;
        }
    }

    /**
     * Check text in an element.
     *
     * @param locator the locator of the element
     * @param expectedText the expected text
     */
    public void text(String locator, String expectedText) {
        try {
            Assert.isTrue(controller.getText(locator).equals(expectedText),"Assertion failed, Texts are not equals.Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_WITH_TEXT+ expectedText + "'!");
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + NOT_FOUND_WITH_TEXT+ expectedText + "'!");
            throw e;
        }
    }

    /**
     * Check that an element contains a specific text.
     *
     * @param locator the locator of the element
     * @param expectedText the expected text
     */
    public void containsText(String locator, String expectedText) {
        if (controller.getText(locator).contains(expectedText)) {
            controller.highlight(locator, PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_CONTAINING_TEXT + expectedText + "'!");
        } else {
            controller.highlight(locator, FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + NOT_FOUND_CONTAINING_TEXT + expectedText + "'!");
            throw new AssertionError(ELEMENT_LOCATOR + locator + NOT_FOUND_CONTAINING_TEXT + expectedText + "'!");
        }
    }

    /**
     * Checks if the specified input element is editable
     *
     * @param locator the locator of the element
     */
    public void editable(String locator) {
        try {
            Assert.isTrue(controller.isComponentEditable(locator),"Assertion failed, condition is not true.Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_EDITABLE);
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + FOUND_DISABLED);
            throw e;
        }
    }

    /**
     * Checks if a component disabled. This means that no write/edit actions are allowed
     *
     * @param locator the locator of the component
     */
    public void disabled(String locator) {
        try {
            Assert.isTrue(controller.isComponentDisabled(locator),"Assertion failed, condition is not true.Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_DISABLED);
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + FOUND_EDITABLE);
            throw e;
        }
    }

    /**
     * Check that an element (check box, radio button etc) is selected.
     *
     * @param locator the locator of the element
     */
    public void selected(String locator) {
        try {
            Assert.isTrue(controller.isComponentSelected(locator),"Assertion failed, condition is not true.Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_SELECTED);
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + NOT_FOUND_SELECTED);
            throw e;
        }
    }

    /**
     * Check that a check box, field, radio button is not selected.
     *
     * @param locator the locator of the field you want to find unchecked
     */
    public void notSelected(String locator) {
        try {
            Assert.isTrue(controller.isComponentNotSelected(locator),"Assertion failed, condition is not true.Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + NOT_FOUND_SELECTED);
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + FOUND_SELECTED);
            throw e;
        }
    }

    /**
     * Text is contained in a table, under a specific table header, in the row of a specific element
     *
     * @param locator the locator of the table
     * @param elementName The element that is situated in the same row with the expected text
     * @param headerName the header name under which the element is expected
     * @param expectedText the expected text
     */
    public void tableElementTextUnderHeader(String locator, String elementName, String headerName, String expectedText) {
        try {
            Assert.isTrue(controller.getTableElementTextUnderHeader(locator, elementName,headerName).equals(expectedText),"Assertion failed, Texts are not equals.Check...");
            controller.highlight(controller.getTableElementSpecificHeaderLocator(locator, elementName, headerName),PASS_COLOR);
            info(TABLE_ELEMENT + elementName + FOUND_WITH_TEXT+ expectedText + "' for header '" + headerName + "'!");
        } catch (IllegalArgumentException e) {
            controller.highlight(controller.getTableElementSpecificHeaderLocator(locator, elementName, headerName),FAIL_COLOR);
            error(TABLE_ELEMENT + elementName + NOT_FOUND_WITH_TEXT+ expectedText + "' for header '" + headerName + "'!");
            throw e;
        }
    }

    /**
     * Text is contained in table in specific row and column.
     *
     * @param locator the locator of the table
     * @param row the row of the table where text is expected
     * @param column the column of the table where text is expected
     * @param expectedText the expected text for the specified row and column of the table
     */
    public void tableElementTextForSpecificRowAndColumn(String locator,String row, String column, String expectedText) {
        try {
            Assert.isTrue(controller.getTableElementTextForRowAndColumn(locator, row, column).equals(expectedText),"Assertion failed, Check...");
            controller.highlight(controller.getTableElementSpecificRowAndColumnLocator(locator, row, column),PASS_COLOR);
            info("The table element in row '" + row + "' and column '" + column + FOUND_WITH_TEXT + expectedText + "'!");
        } catch (IllegalArgumentException e) {
            controller.highlight(controller.getTableElementSpecificRowAndColumnLocator(locator, row, column),FAIL_COLOR);
            error("The table element in row '" + row + "' and column '" + column + NOT_FOUND_WITH_TEXT + expectedText + "'!");
            throw e;
        }
    }

    /**
     * Table element row. Check that an element can be found at a specific table row
     *
     * @param locator the locator
     * @param elementName the element name you expect to find in table
     * @param expectedRow the expected row for your element
     */
    public void tableElementAtRowPosition(String locator,String elementName, String expectedRow) {
        try {
            Assert.isTrue(controller.getTableElementRowPosition(locator, elementName).equals(expectedRow),"Assertion failed, Check...");
            info(TABLE_ELEMENT + elementName + "' was found in row '"+ expectedRow + "'!");
        } catch (IllegalArgumentException e) {
            error(TABLE_ELEMENT + elementName + "' was not found in row '"+ expectedRow + "'!");
            throw e;
        }
    }

    /**
     * Table elements. Check that current table elements are the expected ones
     *
     * @param locator the locator of the table
     * @param expectedArray the expected array for comparison with the actual array
     */
    public void tableElements(String locator,String[][] expectedArray){
        String[][] actualArray = controller.getTableElements2DArray(locator);
        for (int i = 0; i < expectedArray.length; i++) {
            for (int j = 0; j < expectedArray[i].length; j++) {
                if (expectedArray[i][j].equals(actualArray[i][j])){
                    controller.highlight(controller.getTableElementSpecificRowAndColumnLocator(locator, String.valueOf(i+1), String.valueOf(j+1)),PASS_COLOR);
                }
                else{
                    controller.highlight(controller.getTableElementSpecificRowAndColumnLocator(locator, String.valueOf(i+1), String.valueOf(j+1)),FAIL_COLOR);
                    error("The table elements are not equal! EXPECTED VALUE: " + expectedArray[i][j] + " - ACTUAL VALUE: " + actualArray[i][j]);
                    throw new AssertionError();
                }
            }
        }
        info("The table elements are equal");
    }

    /**
     * Text is contained in table, in a column with a specific header.
     *
     * @param locator the locator of the table
     * @param headerName the header name of the column, where the text is expected
     * @param expectedText the expected text in the specified location
     */
    public void textIsContainedInTableRecordsUnderHeader(String locator,String headerName, String expectedText) {
        List<String> records = controller.getTableRecordsUnderHeader(locator, headerName);
        if (records.contains(expectedText)){
            controller.highlight(controller.getTableElementSpecificHeaderLocator(locator, expectedText, headerName),PASS_COLOR);
            info("The '" + expectedText + "' was found with in table with locator '" + locator + "' under header '" + headerName +"'");
            return;
        }
        controller.highlight(locator,FAIL_COLOR);
        error("The '" + expectedText + "' was not found with in table with locator '" + locator + "' under header '" + headerName +"'");
        throw new AssertionError();
    }

    /**
     * Text is not contained in a table, in a column with a specific header
     *
     * @param locator the locator of the table
     * @param headerName the header name of the column, where the text is not expected
     * @param expectedText the text that is not expected in the specified location
     */
    public void textIsNotContainedInTableRecordsUnderHeader(String locator,String headerName, String expectedText) {
        List<String> records = controller.getTableRecordsUnderHeader(locator, headerName);
        if (!records.contains(expectedText)){
            controller.highlight(locator,PASS_COLOR);
            info("The '" + expectedText + "' was not found not with in table with locator '" + locator + "' under header '" + headerName +"'");
            return;
        }
        controller.highlight(controller.getTableElementSpecificHeaderLocator(locator, expectedText, headerName),FAIL_COLOR);
        error("The '" + expectedText + "' was found with in table with locator '" + locator + "' under header '" + headerName +"'");
        throw new AssertionError();
    }

    /**
     * List options. Check all options available in a list
     *
     * @param locator the locator of the list
     * @param expectedOptions the expected available options to found in a list
     */
    public void allListOptions(String locator, List<String> expectedOptions){
        try {
            Assert.isTrue(controller.getAllListOptions(locator).equals(expectedOptions),"Assertion failed, Lists are not equal, Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_WITH_OPTIONS  + expectedOptions  + "'!");
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + NOT_FOUND_WITH_OPTIONS  + expectedOptions  + "'!");
            throw e;
        }
    }

    /**
     * List options. Checks if some options are  selected in a list
     *
     * @param locator the locator of the list
     * @param expectedOptions the expected options in the specified list
     */
    public void selectedListOptions(String locator,List<String> expectedOptions){
        try {
            Assert.isTrue(controller.getSelectedOptions(locator).equals(expectedOptions),"Assertion failed, Lists are not equal, Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_WITH_SELECTED_OPTIONS + expectedOptions + "'!");
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + NOT_FOUND_WITH_SELECTED_OPTIONS + expectedOptions + "'!");
            throw e;
        }
    }

    /**
     * Selected list option. Checks if an option is selected in a list
     *
     * @param locator the locator of the list
     * @param expectedOption the expected option selected in the list
     */
    public void selectedListOption(String locator, String expectedOption){
        try {
            Assert.isTrue(controller.getSelectedOption(locator).equals(expectedOption),"Assertion failed, List option is not the correct, Check...");
            controller.highlight(locator,PASS_COLOR);
            info(ELEMENT_LOCATOR + locator + FOUND_WITH_SELECTED_OPTION + expectedOption + "'!");
        } catch (IllegalArgumentException e) {
            controller.highlight(locator,FAIL_COLOR);
            error(ELEMENT_LOCATOR + locator + NOT_FOUND_WITH_SELECTED_OPTION + expectedOption + "'!");
            throw e;
        }
    }

    /**
     * Attribute value.
     *
     * @param locator the locator of the element
     * @param attribute the attribute of the element under examination
     * @param desiredValue the desired value that the attribute must have
     */
    public void attributeValue (String locator,String attribute,String desiredValue){
        try {
            Assert.isTrue(controller.getAttributeValue(locator, attribute).equals(desiredValue),"Assertion failed, Attribute value is not the correct, Check...");
            controller.highlight(locator,PASS_COLOR);
            info("The '" + attribute + ELEMENT_LOCATOR_ATTRIBUTE + locator +  FOUND_WITH_VALUE + desiredValue+ "'!");
        }
        catch(IllegalArgumentException e){
            controller.highlight(locator,FAIL_COLOR);
            error("The '" + attribute + ELEMENT_LOCATOR_ATTRIBUTE + locator +  NOT_FOUND_WITH_VALUE + desiredValue+ "'!");
            throw e;
        }
    }

    /**
     * Attribute contains value.
     *
     * @param locator the locator of the element
     * @param attribute the attribute of the element under examination
     * @param desiredValue the desired value to be contained in attribute
     */
    public void attributeContainsValue (String locator,String attribute,String desiredValue){
        if(	controller.getAttributeValue(locator, attribute).contains(desiredValue)){
            controller.highlight(locator,PASS_COLOR);
            info("The '" + attribute + ELEMENT_LOCATOR_ATTRIBUTE + locator + FOUND_CONTAINING_VALUE + desiredValue+ "'!");
        }
        else{
            controller.highlight(locator,FAIL_COLOR);
            error("The '" + attribute + ELEMENT_LOCATOR_ATTRIBUTE + locator + NOT_FOUND_CONTAINING_VALUE + desiredValue+ "'!");
            throw new AssertionError("The '" + attribute + ELEMENT_LOCATOR_ATTRIBUTE + locator + NOT_FOUND_CONTAINING_VALUE + desiredValue+ "'!");
        }
    }

    /**
     * Element contained in list.
     *
     * @param list the list you want to search into
     * @param element the element you want to be present in the list
     */
    public void elementContainedInList(List<?> list,Object element){
        if(list.contains(element)){
            info("The '" + element + CONTAINED_IN_LIST);
        }
        else{
            error("The '" + element + NOT_CONTAINED_IN_LIST);
            throw new AssertionError("The '" + element + NOT_CONTAINED_IN_LIST);
        }
    }

    /**
     * Element is not contained in a list.
     *
     * @param list the list you want to search into
     * @param element the element you want to be not present in the list
     */
    public void elementNotContainedInList(List<?> list,Object element){
        if(!list.contains(element)){
            info("The '" + element + NOT_CONTAINED_IN_LIST);
        }
        else{
            error("The '" + element + CONTAINED_IN_LIST);
            throw new AssertionError("The '" + element + CONTAINED_IN_LIST);
        }
    }

    /**
     * Alert text.
     *
     * @param alertText the alert text
     */
    public void alertText(String alertText){
        try {
            Assert.isTrue(controller.getAlertText().equals(alertText),"Assertion failed, Text is not the correct, Check...");
            info(FOUND_ALERT_WITH_MESSAGE + alertText +"'!");
        }
        catch(AssertionError e){
            error(NOT_FOUND_ALERT_WITH_MESSAGE + alertText +"'!");
            throw e;
        }
    }

}
