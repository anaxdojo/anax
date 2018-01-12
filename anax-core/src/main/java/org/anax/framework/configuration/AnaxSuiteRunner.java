package org.anax.framework.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class AnaxSuiteRunner {

    List<String> suites;

    List<String> beforeTest;
    List<String> afterTest;

    List<String> tests;



    List<String> beforeTestStep;
    List<String> afterTestStep;


    /**

     AnaxSuiteRunner.runAllTests();

     AnaxSuiteRunner.withAllTestsParallel(4).runAllTests();

     AnaxSuiteRunner.withParallel({ "Suite A", "Suite B", "Suite C"}).runAllTests();

     AnaxSuiteRunner.withTestSuites({ "Suite A", "Suite B", "Suite C"}).runAllTests();


     AnaxSuiteRunner.withTests({ "test1"}).runAllTests();

     */
}
