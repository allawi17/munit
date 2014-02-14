/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.munit.config;

import org.junit.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Mulesoft Inc.
 * @since 3.3.2
 */
public class AssertNotSameMessageProcessorTest extends AbstractMessageProcessorTest
{

    public static final String TEST_MESSAGE = "testMessage";
    public static final String RETURN_VALUE1 = "r1";
    public static final String RETURN_VALUE2 = "r2";
    public static final String EXPECTED = "#[expr1]";
    public static final String VALUE = "#[expr2]";


    @Test
    public void callModuleCorrectly()
    {
        AssertNotSameMessageProcessor mp = (AssertNotSameMessageProcessor) buildMp();

        mp.setExpected(EXPECTED);
        mp.setValue(VALUE);

        when(expressionManager.evaluate(EXPECTED, muleMessage)).thenReturn(RETURN_VALUE1);
        when(expressionManager.evaluate(VALUE, muleMessage)).thenReturn(RETURN_VALUE2);


        mp.doProcess(muleMessage, module);

        verify(module).assertNotSame(TEST_MESSAGE, RETURN_VALUE1, RETURN_VALUE2);
    }

    @Override
    protected MunitMessageProcessor doBuildMp()
    {
        AssertNotSameMessageProcessor mp = new AssertNotSameMessageProcessor();
        mp.setMessage(TEST_MESSAGE);
        return mp;
    }

    @Override
    protected String getExpectedName()
    {
        return "assertNotSame";
    }
}
