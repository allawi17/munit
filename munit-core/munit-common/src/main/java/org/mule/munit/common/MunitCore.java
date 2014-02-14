/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.munit.common;


import org.mule.api.MuleContext;
import org.mule.api.config.MuleProperties;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.registry.MuleRegistry;
import org.mule.api.registry.RegistrationException;
import org.mule.modules.interceptor.processors.MessageProcessorCall;
import org.mule.munit.common.endpoint.MockEndpointManager;
import org.mule.munit.common.mp.MockedMessageProcessorManager;
import org.mule.munit.common.mp.MunitMessageProcessorCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Util class to manage Munit tests.
 * <p/>
 * This class should have minimal functionality as any Util class.
 * </p>
 *
 * @author Mulesoft Inc.
 * @since 3.3.2
 */
public class MunitCore
{

    public static final String LINE_NUMBER_ELEMENT_ATTRIBUTE = "__MUNIT_LINE_NUMBER";

    /**
     * <p>
     * Resets the status of Munit. Used after each test.
     * </p>
     *
     * @param muleContext The Mule context
     */
    public static void reset(MuleContext muleContext)
    {
        MockEndpointManager endpointFactory = (MockEndpointManager) muleContext.getRegistry().lookupObject(MuleProperties.OBJECT_MULE_ENDPOINT_FACTORY);
        endpointFactory.resetBehaviors();

        MockedMessageProcessorManager mpManager = (MockedMessageProcessorManager) muleContext.getRegistry().lookupObject(MockedMessageProcessorManager.ID);

        if (mpManager != null)
        {
            mpManager.reset();
        }
    }

    /**
     * <p>
     * Adds the {@link MockedMessageProcessorManager} to the {@link MuleRegistry}
     * </p>
     *
     * @param muleContext <p>
     *                    The mule context where the manager must be registered.
     *                    </p>
     */
    public static void registerManager(MuleContext muleContext)
    {
        try
        {
            MuleRegistry registry = muleContext.getRegistry();
            if (registry.lookupObject(MockedMessageProcessorManager.ID) == null)
            {
                registry.registerObject(MockedMessageProcessorManager.ID, new MockedMessageProcessorManager());
            }
        }
        catch (RegistrationException e)
        {
            // Very uncommon scenario.
            throw new RuntimeException(e);
        }
    }


    /**
     * <p>
     * Builds the mule Stack Trace based on the Munit registered calls.
     * </p>
     * <p/>
     * <p>
     * The Mule stack trace contains the executed {@link org.mule.api.processor.MessageProcessor} in the test in the
     * same format as JAVA.
     * </p>
     *
     * @param muleContext <p>
     *                    The mule context
     *                    </p>
     * @return <p>
     *         A list of JAVA stack trace elements.
     *         </p>
     * @since 3.4
     */
    public static List<StackTraceElement> buildMuleStackTrace(MuleContext muleContext)
    {
        MockedMessageProcessorManager manager = (MockedMessageProcessorManager) muleContext.getRegistry().lookupObject(MockedMessageProcessorManager.ID);
        List<MunitMessageProcessorCall> calls = manager.getCalls();

        List<StackTraceElement> stackTraceElements = new ArrayList<StackTraceElement>();

        StringBuffer stackTrace = new StringBuffer();
        for (MunitMessageProcessorCall call : calls)
        {
            stackTraceElements.add(0, new StackTraceElement(getFlowConstructName(call), getFullName(call), call.getFileName(), lineNumber(call)));
            stackTrace.insert(0, call.getMessageProcessorId().getFullName());
        }
        return stackTraceElements;
    }

    private static Integer lineNumber(MunitMessageProcessorCall call)
    {
        String lineNumber = call.getLineNumber();
        if (lineNumber == null)
        {
            return 0;
        }
        return Integer.valueOf(lineNumber);
    }

    private static String getFullName(MessageProcessorCall call)
    {
        String fullName = call.getMessageProcessorId().getFullName();
        Map<String, Object> attributes = call.getAttributes();
        attributes.toString();
        attributes.remove("name");
        attributes.remove(LINE_NUMBER_ELEMENT_ATTRIBUTE);


        return fullName + attributes.toString();
    }


    private static String getFlowConstructName(MessageProcessorCall call)
    {
        FlowConstruct flowConstruct = call.getFlowConstruct();
        if (flowConstruct == null)
        {
            return "";
        }
        return flowConstruct.getName();
    }
}
