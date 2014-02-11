/*
 *
 * (c) 2003-2012 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master
 * Subscription Agreement (or other Terms of Service) separately entered
 * into between you and MuleSoft. If such an agreement is not in
 * place, you may not use the software.
 */
package org.mule.munit.common.mocking;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.api.MuleContext;
import org.mule.api.registry.MuleRegistry;
import org.mule.modules.interceptor.processors.MessageProcessorCall;
import org.mule.modules.interceptor.processors.MessageProcessorId;
import org.mule.munit.common.mp.MockedMessageProcessorManager;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.AssertionFailedError;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mulesoft Inc.
 * @since 3.3.2
 */
public class MunitVerifierTest
{

    private MuleContext muleContext;
    private MuleRegistry muleRegistry;
    private MockedMessageProcessorManager manager;

    @Before
    public void setUp()
    {
        muleContext = mock(MuleContext.class);
        muleRegistry = mock(MuleRegistry.class);
        manager = mock(MockedMessageProcessorManager.class);

        when(muleContext.getRegistry()).thenReturn(muleRegistry);
        when(muleRegistry.lookupObject(MockedMessageProcessorManager.ID)).thenReturn(manager);

    }

    @Test(expected = AssertionFailedError.class)
    public void withNoCallFailAtLeast()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(new ArrayList<MessageProcessorCall>());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .atLeast(1);

    }

    @Test(expected = AssertionFailedError.class)
    public void withNoCallFailAtLeastOne()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(new ArrayList<MessageProcessorCall>());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .atLeastOnce();

    }

    @Test(expected = AssertionFailedError.class)
    public void withNoCallFailTimes()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(new ArrayList<MessageProcessorCall>());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .times(2);

    }

    @Test(expected = AssertionFailedError.class)
    public void withCallsFailAtMost()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(createCalls());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .times(2);

    }


    @Test
    public void withCallsOkTimes()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(createCalls());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .times(3);

    }

    @Test
    public void withCallsOkAtLeastOnce()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(createCalls());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .atLeastOnce();

    }


    @Test
    public void withCallsOkAtLeast()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(createCalls());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .atLeast(1);

    }

    @Test
    public void withCallsOkAtMost()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(createCalls());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .atMost(4);

    }

    @Test
    public void verifyWithAttributes()
    {

        when(manager.findCallsFor(any(MessageProcessorId.class), any(Map.class)))
                .thenReturn(createCalls());

        new MunitVerifier(muleContext).verifyCallOfMessageProcessor("testName")
                .ofNamespace("testNamespace")
                .withAttributes(Attribute.attribute("test").withValue("anything"))
                .atMost(4);

    }

    private ArrayList<MessageProcessorCall> createCalls()
    {
        ArrayList<MessageProcessorCall> calls = new ArrayList<MessageProcessorCall>();
        calls.add(new MessageProcessorCall(null));
        calls.add(new MessageProcessorCall(null));
        calls.add(new MessageProcessorCall(null));
        return calls;
    }
}
