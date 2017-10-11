/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.time;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class DateDifferenceFunctionExtensionTestCase {

    private static final Logger log = Logger.getLogger(DateDifferenceFunctionExtensionTestCase.class);
    private volatile boolean eventArrived;
    private int waitTime = 50;
    private int timeout = 30000;
    private AtomicInteger eventCount;

    @BeforeMethod
    public void init() {
        eventArrived = false;
        eventCount = new AtomicInteger(0);
    }

    @Test
    public void dateDifferenceFunctionExtension() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionTestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,"
                        + "dateValue2 string,dateFormat2 string,"
                        + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);

                eventArrived = true;
                for (Event inEvent : inEvents) {
                    eventCount.incrementAndGet();
                    log.info("Event : " + eventCount.get() + ",dateDifference : " + inEvent.getData(1) + ","
                            + "dateDifferenceInMilliseconds : " + inEvent.getData(2));

                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] {
                "IBM", "2014-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss", "2014-11-9 13:23:44", "yyyy-MM-dd HH:mm:ss",
                1415692424000L, 1415519624000L
        });
        inputHandler.send(new Object[] {
                "IBM", "2014-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss", "2014-10-9 13:23:44", "yyyy-MM-dd HH:mm:ss",
                1415692424000L, 1412841224000L
        });
        inputHandler.send(new Object[] {
                "IBM", "2014-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss", "2013-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss",
                1415692424000L, 1384156424000L
        });

        SiddhiTestHelper.waitForEvents(waitTime, 3, eventCount, timeout);
        AssertJUnit.assertEquals(3, eventCount.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void dateDifferenceFunctionExtensionTest2() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionFirstDateInvalidFormatTestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    if (eventCount.intValue() == 1) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                    if (eventCount.intValue() == 2) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] {
                "IBM", "2014:11:11 13:23:44", "yyyy-MM-dd HH:mm:ss", "2014-11-9 13:23:44", "yyyy-MM-dd HH:mm:ss",
                1415692424000L, 1415519624000L
        });
        inputHandler.send(new Object[] {
                "IBM", "2015:11:11 13:23:44", "yyyy-MM-dd HH:mm:ss", "2014-11-9 13:23:44", "yyyy-MM-dd HH:mm:ss",
                1415692424000L, 1415519624000L
        });
        Thread.sleep(1000);
        AssertJUnit.assertEquals(2, eventCount.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void dateDifferenceFunctionExtensionTest3() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionSecondDateInvalidFormatTestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    if (eventCount.intValue() == 1) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                    if (eventCount.intValue() == 2) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] {
                "IBM", "2014-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss", "2014:11:9 13:23:44", "yyyy-MM-dd HH:mm:ss",
                1415692424000L, 1415519624000L
        });
        inputHandler.send(new Object[] {
                "IBM", "2014-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss", "2015:11:9 13:23:44", "yyyy-MM-dd HH:mm:ss",
                1415692424000L, 1415519624000L
        });
        Thread.sleep(1000);
        AssertJUnit.assertEquals(2, eventCount.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension4() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionInvalidParameterTypeInFirstArgument");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 int,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension5() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionInvalidParameterTypeInSecondArgument");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 int,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension6() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionInvalidParameterTypeInThirdArgument");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 int,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension7() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionInvalidParameterTypeInFourthArgument");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 int," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test
    public void dateDifferenceFunctionExtension8() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionFirstArgumetNullTestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    if (eventCount.intValue() == 1) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                    if (eventCount.intValue() == 2) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] {
                "IBM", null, "yyyy-MM-dd HH:mm:ss", "2014-11-9 13:23:44", "yyyy-MM-dd HH:mm:ss", 1415692424000L,
                1415519624000L
        });
        inputHandler.send(new Object[] {
                "IBM", null, "yyyy-MM-dd HH:mm:ss", "2016-11-9 13:23:44", "yyyy-MM-dd HH:mm:ss", 1415692424000L,
                1415519624000L
        });
        Thread.sleep(1000);
        AssertJUnit.assertEquals(2, eventCount.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void dateDifferenceFunctionExtension9() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionThirdArgumetNullTestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    if (eventCount.intValue() == 1) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                    if (eventCount.intValue() == 2) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                }
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] {
                "IBM", "2014-11-11 13:23:44", null, "2014-11-9 13:23:44", "yyyy-MM-dd HH:mm:ss", 1415692424000L,
                1415519624000L
        });
        inputHandler.send(new Object[] {
                "IBM", "2015-11-11 13:23:44", null, "2012-11-9 13:23:44", "yyyy-MM-dd HH:mm:ss", 1415692424000L,
                1415519624000L
        });
        Thread.sleep(1000);
        AssertJUnit.assertEquals(2, eventCount.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void dateDifferenceFunctionExtension10() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionSecondArgumetNullTestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    if (eventCount.intValue() == 1) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                    if (eventCount.intValue() == 2) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                        AssertJUnit.assertEquals("2", event.getData(2).toString());
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] {
                "IBM", "2014-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss", null, "yyyy-MM-dd HH:mm:ss", 1415692424000L,
                1415519624000L
        });
        inputHandler.send(new Object[] {
                "IBM", "2015-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss", null, "yyyy-MM-dd HH:mm:ss", 1415692424000L,
                1415519624000L
        });
        Thread.sleep(1000);
        AssertJUnit.assertEquals(2, eventCount.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension11() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionInvalid parameter in second argument for length 2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 int);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1,dateFormat2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension12() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionTestCaseInvalidNoOfArguments");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test
    public void dateDifferenceFunctionExtension13() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionTestCaseCastingDesiredFormat");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 string,"
                        + "unit string," + "timestampInMilliseconds1 string,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol ,time:dateDiff(timestampInMilliseconds1,dateFormat1) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    if (eventCount.intValue() == 1) {
                        AssertJUnit.assertEquals(null, event.getData(1));
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] {
                "IBM", "2014-11-11 13:23:44", "yyyy-MM-dd HH:mm:ss", "2014-11-9 13:23:44", "yyyy-MM-dd HH:mm:ss",
                1415692424000L, 1415519624000L
        });
        Thread.sleep(1000);
        AssertJUnit.assertEquals(1, eventCount.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension14() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionTestCaseInvalidParameterTypeFirstArgumentLengthTwo");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 int,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension15() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionTestCaseInvalidParameterTypeSecondArgumentLengthTwo");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 int,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension16() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionTestCaseInvalidParameterTypeSecondArgumentLengthTwo");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 int,dateFormat1 string,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension17() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionTestCaseInvalidParameterTypeSecondArgumentLengthTwo");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 string,dateValue2 int,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void dateDifferenceFunctionExtension18() throws InterruptedException {

        log.info("DateDifferenceFunctionExtensionTestCaseInvalidParameterTypeThirdArgumentLengthTwo");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition =
                "" + "define stream inputStream (symbol string,dateValue1 string,dateFormat1 int,dateValue2 string,"
                        + "dateFormat2 string," + "timestampInMilliseconds1 long,timestampInMilliseconds2 long);";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select symbol , time:dateDiff(dateValue1,dateValue2,dateFormat1) as dateDifference,"
                + "time:dateDiff(timestampInMilliseconds1,timestampInMilliseconds2) as dateDifferenceInMilliseconds "
                + "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }
}
