package io.github.artsok;


import org.junit.jupiter.api.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.mockito.internal.matchers.Null;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;



/**
 * Examples how to use @RepeatedIfExceptionsTest
 *
 * @author Artem Sokovets
 */
public class ReRunnerTest {
    private ThreadLocalRandom random = ThreadLocalRandom.current();
    private static int counter;

    @ProgrammaticTest
    @RepeatedIfExceptionsTest(repeats = 2)
    public void runTest() {
        assertTrue(true, "No exception, repeat one time");
    }

    @Test
    void runRunTest() throws Exception {
        assertTestResults("runTest", true, 1, 0, 0);
    }

    /**
     * Repeated three times if test failed.
     * By default Exception.class will be handled in test
     */
    @ProgrammaticTest
    @RepeatedIfExceptionsTest(repeats = 3)
    public void reRunTest() throws IOException {
        throw new IOException("Error in Test");
    }

    @Test
    void runReRunTest() throws Exception {
        assertTestResults("reRunTest", false, 4, 3, 0);
    }

    /**
     * Repeated two times if test failed. Set IOException.class that will be handled in test
     *
     * @throws IOException - error if occurred
     */
    @ProgrammaticTest
    @RepeatedIfExceptionsTest(repeats = 2, exceptions = IOException.class)
    public void reRunTest2() throws IOException {
        throw new IOException("Exception in I/O operation");
    }

    @Test
    void runReRun2Test() throws Exception {
        assertTestResults("reRunTest2", false, 3, 2, 0);
    }

    /**
     * Repeated ten times if test failed. Set IOException.class that will be handled in test
     * Set formatter for test. Like behavior as at {@link org.junit.jupiter.api.RepeatedTest}
     *
     * @throws IOException - error if occurred
     */
    @ProgrammaticTest
    @RepeatedIfExceptionsTest(repeats = 10, exceptions = IOException.class,
            name = "Rerun failed test. Attempt {currentRepetition} of {totalRepetitions}")
    public void reRunTest3() throws IOException {
        throw new IOException("Exception in I/O operation");
    }

    @Test
    void runReRun3Test() throws Exception {
        assertTestResults("reRunTest3", false, 11, 10, 0);
    }

    @DisplayName("Name for our test")
    @RepeatedIfExceptionsTest(repeats = 105, exceptions = RuntimeException.class,
            name = "Rerun failed Test. Repetition {currentRepetition} of {totalRepetitions}")
    void reRunTest4() throws IOException {
        if (random.nextInt() % 2 == 0) { //Исключение бросается рандомно
            throw new RuntimeException("Error in Test");
        }
    }

    /**
     * Repeated 100 times with minimum success four times, then disabled all remaining repeats.
     * See image below how it works. Default exception is Exception.class
     */
    @ProgrammaticTest
    @DisplayName("Test Case Name")
    @RepeatedIfExceptionsTest(repeats = 100, minSuccess = 4)
    public void reRunTest5() {
        if (random.nextInt() % 2 == 0) {
            throw new RuntimeException("Error in Test");
        }
    }

    @ProgrammaticTest
    @DisplayName("Do not ultimately fail a test if there are still enough repetitions possible.")
    @RepeatedIfExceptionsTest(repeats = 2)
    public void reRunTest6() {
        throw new RuntimeException("Error in Test");
    }

    @Test
    void runReRunTest6() throws Exception {
        assertTestResults("reRunTest6", false, 3, 2, 0);
    }

    @ProgrammaticTest
    @DisplayName("Stop repetitions if 'minSuccess' cannot be reached anymore")
    @RepeatedIfExceptionsTest(repeats = 10, minSuccess = 4)
    public void reRunTest7() {
        throw new RuntimeException("Error in Test");
    }

    @Test
    void runReRunTest7() throws Exception {
        assertTestResults("reRunTest7", false, 8, 7, 3);
    }

    @ProgrammaticTest
    @DisplayName("Ultimately fail a test as soon as an unrepeatable exception occurs.")
    @RepeatedIfExceptionsTest(repeats = 2, exceptions = NumberFormatException.class)
    public void reRunTest8() {
        throw new RuntimeException("Error in Test");
    }

    @Test
    void runReRunTest8() throws Exception {
        assertTestResults("reRunTest8", false, 1, 0, 0);
    }

    @Disabled
    @RepeatedIfExceptionsTest(repeats = 3, exceptions = IOException.class, suspend = 5000L)
    void reRunTestWithSuspendOption() throws IOException {
        throw new IOException("Exception in I/O operation");
    }

    @ProgrammaticTest
    @DisplayName("fail at all without error message")
    @RepeatedIfExceptionsTest(repeats = 1,maxExceptions = 1)
    public void reRunTestStopRetryIfSameExceptions1() {
        throw new RuntimeException();
    }

    @Test
    void reRunTestStopRetryIfSame1() throws Exception {
        assertTestResults("reRunTestStopRetryIfSameExceptions1", false, 2, 1, 0);
    }

    @ProgrammaticTest
    @DisplayName("fail but retry should be stop at Repetition 2 of 3")
    @RepeatedIfExceptionsTest(repeats = 3,maxExceptions = 2)
    public void reRunTestStopRetryIfSameExceptions2() {
        throw new RuntimeException("same error message");
    }

    @Test
    void reRunTestStopRetryIfSame2() throws Exception {
        assertTestResults("reRunTestStopRetryIfSameExceptions2", false, 4, 3, 0);
    }

    @ProgrammaticTest
    @DisplayName("fail at Repetition 4 of 4")
    @RepeatedIfExceptionsTest(repeats = 4,maxExceptions = 2)
    public void reRunTestStopRetryIfSameExceptions3() {
        throw new RuntimeException("dynamic error message: " + random.nextInt());
    }

    @Test
    void reRunTestStopRetryIfSame3() throws Exception {
        assertTestResults("reRunTestStopRetryIfSameExceptions3", false, 5, 4, 0);
    }

    @ProgrammaticTest
    @DisplayName("fail at Repetition 3 of 3(different exception every time)")
    @RepeatedIfExceptionsTest(repeats = 3,maxExceptions = 2)
    public void reRunTestStopRetryIfSameExceptions4() {
        counter ++;
        if (counter % 2 == 0) {
            throw new NullPointerException("same error message");
        } else {
            throw new RuntimeException("same error message");
        }
    }

    @Test
    void reRunTestStopRetryIfSame4() throws Exception {
        assertTestResults("reRunTestStopRetryIfSameExceptions4", false, 4, 3, 0);
    }

    /**
     * By default total repeats = 1 andI minimum success = 1.
     * If the test failed by this way start to repeat it by one time with one minimum success.
     *
     * This example without exceptions.
     */
    @Disabled
    @ParameterizedRepeatedIfExceptionsTest
    @ValueSource(ints = {14, 15, 100, -10})
    void successfulParameterizedTest(int argument) {
        System.out.println(argument);
    }

    /**
     * By default total repeats = 1 and minimum success = 1.
     * If the test failed by this way start to repeat it by one time with one minimum success.
     * This example with display name but without exceptions
     */
    @Disabled
    @DisplayName("Example of parameterized repeated without exception")
    @ParameterizedRepeatedIfExceptionsTest
    @ValueSource(ints = {1, 2, 3, 1001})
    void successfulParameterizedTestWithDisplayName(int argument) {
        System.out.println(argument);
    }

    /**
     * By default total repeats = 1 and minimum success = 1.
     * If the test failed by this way start to repeat it by one time with one minimum success.
     *
     * This example with display name but with exception. Exception depends on random number generation.
     */
    @Disabled
    @DisplayName("Example of parameterized repeated with exception")
    @ParameterizedRepeatedIfExceptionsTest
    @ValueSource(strings = {"Hi", "Hello", "Bonjour", "Privet"})
    void errorParameterizedTestWithDisplayName(String argument) {
        if (random.nextInt() % 2 == 0) {
            throw new RuntimeException("Exception " + argument);
        }
    }

    /**
     * By default total repeats = 1 and minimum success = 1.
     * If the test failed by this way start to repeat it by one time with one minimum success.
     *
     * This example with display name, repeated display name, 10 repeats and 2 minimum success with exceptions.
     * Exception depends on random number generation.
     */
    @Disabled
    @ParameterizedRepeatedIfExceptionsTest(name = "Argument was {0}",
            repeatedName = " (Repeat {currentRepetition} of {totalRepetitions})",
            repeats = 10, exceptions = RuntimeException.class, minSuccess = 2)
    @ValueSource(ints = {4, 5, 6, 7})
    void errorParameterizedTestWithDisplayNameAndRepeatedName(int argument) {
        if (random.nextInt() % 2 == 0) {
            throw new RuntimeException("Exception in Test " + argument);
        }
    }

    /**
     * By default total repeats = 1 and minimum success = 1.
     * If the test failed by this way start to repeat it by one time with one minimum success.
     *
     * This example with display name, implicitly repeated display name, 4 repeats and 2 minimum success with exceptions.
     * Exception depends on random number generation. Also use {@link MethodSource}
     */
    @Disabled
    @DisplayName("Display name of container")
    @ParameterizedRepeatedIfExceptionsTest(name = "Year {0} is a leap year.",
            repeats = 4, exceptions = RuntimeException.class, minSuccess = 2)
    @MethodSource("stringIntAndListProvider")
    void errorParameterizedTestWithMultiArgMethodSource(String str, int num, List<String> list)  {
        assertEquals(5, str.length());
        assertTrue(num >= 1 && num <= 2);
        assertEquals(2, list.size());
        if (random.nextInt() % 2 == 0) {
            throw new RuntimeException("Exception in Test");
        }
    }


    /**
     * Example with suspend option for Parameterized Test
     * It matters, when you get some infrastructure problems and you want to run your tests through timeout.
     *
     * Set break to 5 seconds. If exception appeared for any arguments, repeating extension would runs tests with break.
     * If one result failed and other passed, does not matter we would wait 5 seconds throught each arguments of the repeated tests.
     *
     */
    @Disabled
    @DisplayName("Example of parameterized repeated with exception")
    @ParameterizedRepeatedIfExceptionsTest(suspend = 5000L, minSuccess = 2, repeats = 3)
    @ValueSource(strings = {"Hi", "Hello", "Bonjour", "Privet"})
    void errorParameterizedTestWithSuspendOption(String argument) {
        if (random.nextInt() % 2 == 0) {
            throw new RuntimeException(argument);
        }
    }

    /**
     * Parameterized Test with the wrong exception.
     * Test throws AssertionError.class, but we wait for Exception.class.
     * In this case test with argument "1" runs ones without repeats.
     *
     * If you change exceptions = AssertionError.class, repeats will appear.
     *
     */
    @Disabled
    @ValueSource(ints = {1, 2})
    @ParameterizedRepeatedIfExceptionsTest(repeats = 2, exceptions = Exception.class)
    void testParameterizedRepeaterAssertionsFailure(int value) {
        assertThat(value, equalTo(2));
    }

    static Stream<Arguments> stringIntAndListProvider() {
        return Stream.of(
                arguments("apple", 1, Arrays.asList("a", "b")),
                arguments("lemon", 2, Arrays.asList("x", "y"))
        );
    }

    @ProgrammaticTest
    @ParameterizedRepeatedIfExceptionsTest(repeats = 2)
    @ValueSource(ints = {1})
    public void reRunTestParameterized1(int number) {
        assertThat(number, equalTo(5));
    }

    @Test
    void runReRunParameterized1Test() throws Exception {
        assertTestResults("reRunTestParameterized1", 0, 1, 3, 2, 0, int.class);
    }

    @ProgrammaticTest
    @ParameterizedRepeatedIfExceptionsTest(repeats = 2)
    @ValueSource(ints = {1, 3, 2})
    public void reRunTestParameterized2(int number) {
        assertThat(number, equalTo(3));
    }

    @Test
    void runReRunParameterized2Test() throws Exception {
        assertTestResults("reRunTestParameterized2", 1, 2, 7, 4, 0, int.class);
    }

    @ProgrammaticTest
    @ParameterizedRepeatedIfExceptionsTest(repeats = 30)
    @ValueSource(ints = {1, 3, 2})
    public void reRunTestParameterized3(int number) {
        assertThat(number, equalTo(new Random().nextInt(500) % 3));
    }

    @Test
    void runReRunParameterized3Test() throws NoSuchMethodException {
        SummaryGeneratingListener testListener = runTest("reRunTestParameterized3", int.class);

        assertEquals(2, testListener.getSummary().getTestsSucceededCount(), "successful test runs");
        assertEquals(1, testListener.getSummary().getTestsFailedCount(), "failed test runs");
        assertGreaterThan(31, testListener.getSummary().getTestsStartedCount(), "started test runs");
        assertGreaterThan(29, testListener.getSummary().getTestsAbortedCount(), "aborted test runs");
        assertEquals(0, testListener.getSummary().getTestsSkippedCount(), "skipped test runs");
    }

    private void assertGreaterThan(int expected, long actual, String message) {
        assertTrue(expected < actual, String.format("expected %s to be greater than %s,%nbut was %s", message, expected, actual));
    }

    private void assertTestResults(String methodName, boolean successfulTestRun, int startedTests, int abortedTests,
                                   int skippedTests) throws Exception {
        assertTestResults(methodName,
                successfulTestRun ? 1 : 0,
                !successfulTestRun ? 1 : 0,
                startedTests, abortedTests, skippedTests);
    }

    private void assertTestResults(String methodName, int successfulTestRuns, int failedTestRuns, int startedTests, int abortedTests,
                                   int skippedTests, Class<?>... parameterTypes) throws Exception {
        SummaryGeneratingListener listener = runTest(methodName, parameterTypes);

        assertEquals(successfulTestRuns, listener.getSummary().getTestsSucceededCount(), "successful test runs");
        assertEquals(failedTestRuns, listener.getSummary().getTestsFailedCount(), "failed test runs");
        assertEquals(startedTests, listener.getSummary().getTestsStartedCount(), "started test runs");
        assertEquals(abortedTests, listener.getSummary().getTestsAbortedCount(), "aborted test runs");
        assertEquals(skippedTests, listener.getSummary().getTestsSkippedCount(), "skipped test runs");
    }

    private SummaryGeneratingListener runTest(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectMethod(getClass(), getClass().getMethod(methodName, parameterTypes)))
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        return listener;
    }

    @Tag("programmatic-tests")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ProgrammaticTest {
    }
}
