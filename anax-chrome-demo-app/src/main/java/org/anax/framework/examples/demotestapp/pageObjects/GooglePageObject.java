package org.anax.framework.examples.demotestapp.pageObjects;

import org.anax.framework.controllers.WebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class GooglePageObject{

    @Autowired
    protected WebController controller;

    public enum GooglePageLocators {

        BUTTON_AGREE("css=button:contains('I agree'),button:contains('Συμφωνώ')"),
        INPUT_GOOGLE_SEARCH("css=input[name='q']"),
        LABEL_CALCULATOR_RESULT("css=span#cwos"),
        BUTTON_ON_CALCULATOR("css=div[role=''button'']:contains(''{0}'')"),
        BUTTON_DIVISION("//*[@aria-label='διαίρεση' or @aria-label='divide']"),
        BTN_GOOGLE_SEARCH("css=input[aria-label*='Google']"),
        ;

        private String myLocator;

        GooglePageLocators(String locator) {
            myLocator = locator;
        }

        public String get() {
            return myLocator;
        }

        public String getWithParams(Object... params) {
            return MessageFormat.format(myLocator, params);
        }
    }

    public void pressAgree(){
        if(controller.isComponentVisible(GooglePageLocators.BUTTON_AGREE.get()))
            controller.click(GooglePageLocators.BUTTON_AGREE.get());
    }

    public void pressCalculatorDivisionButton(){
        controller.click(GooglePageLocators.BUTTON_DIVISION.get());
    }
    /*
     * Input text to search for on Google home page
     */
    public void inputSearchText(String desiredText){
        controller.input(GooglePageLocators.INPUT_GOOGLE_SEARCH.get(), desiredText);
        controller.keyPress(WebController.KeyInfo.ENTER);
    }

    public void pressCalculatorEqualButton(){
        controller.click(GooglePageLocators.BUTTON_ON_CALCULATOR.getWithParams("="));
    }

    public void pressCalculatorNumber(String number){
        controller.click(GooglePageLocators.BUTTON_ON_CALCULATOR.getWithParams(number));
    }

    /*
     * Press button Search on Google home page
     */
    public void pressGoogleSearchButton(){
        controller.press(GooglePageLocators.BTN_GOOGLE_SEARCH.get());
    }


}
