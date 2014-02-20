/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.munit;

import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.NestedProcessor;
import org.mule.api.annotations.Category;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Optional;
import org.mule.api.context.MuleContextAware;
import org.mule.api.schedule.Scheduler;
import org.mule.api.schedule.SchedulerFactoryPostProcessor;

import java.util.List;
import java.util.Map;

/**
 * <p>Module used to call mule transports.</p>
 *
 * @author Mulesoft Inc.
 * @since 3.4.0
 */
@Module(name = "mclient", schemaVersion = "1.0", minMuleVersion = "3.5.0", friendlyName = "Mule Client")
@Category(name = "org.mule.tooling.category.munit.utils", description = "Munit tools")
public class MClient implements MuleContextAware, SchedulerFactoryPostProcessor
{

    private MuleContext muleContext;

    /**
     * <p>
     * Define if poll has to be stopped
     * </p>
     */
    @Configurable
    private Boolean stopPollsByDefault;

    /**
     * <p>
     * The poll manager to stop and run polls
     * </p>
     */
    private MunitPollManager pollManager;


    /**
     * <p>Do a real call to your inbound flows.</p>
     * <p/>
     * {@sample.xml ../../../doc/MClient-connector.xml.sample mclient:call}
     *
     * @param path               Path to call the transport, example: http://localhost:8081/tests
     * @param payload            Message payload
     * @param parameters         Connection parameters
     * @param responseProcessing Add any processing of the client response
     * @return Response call
     * @throws Exception an Exception
     */
    @Processor
    public Object call(String path, @Optional Map<String, Object> parameters, @Optional Object payload,
                       @Optional List<NestedProcessor> responseProcessing) throws Exception
    {

        MuleMessage response = muleContext.getClient().send(path, payload, parameters);

        Object processedResponse = response;
        if (responseProcessing != null)
        {
            for (NestedProcessor processor : responseProcessing)
            {
                processedResponse = processor.process(processedResponse);
            }

        }
        return processedResponse;
    }


    /**
     * <p>Dispatches an event asynchronously to a endpointUri via a Mule server.</p>
     * <p/>
     * {@sample.xml ../../../doc/MClient-connector.xml.sample mclient:dispatch}
     *
     * @param path       Path to call the transport, example: http://localhost:8081/tests
     * @param payload    Message payload
     * @param parameters Connection parameters
     * @return Response call
     * @throws Exception an Exception
     */
    @Processor
    public void dispatch(String path, @Optional Map<String, Object> parameters, @Optional Object payload) throws Exception
    {

        muleContext.getClient().dispatch(path, payload, parameters);
    }

    /**
     * <p>Do a real call to your inbound flows.</p>
     * <p/>
     * {@sample.xml ../../../doc/MClient-connector.xml.sample mclient:request}
     *
     * @param url                Path to call the transport, example: http://localhost:8081/tests
     * @param timeout            time out processing
     * @param responseProcessing Add any processing of the client response
     * @return Response call
     * @throws Exception an Exception
     */
    @Processor
    public Object request(String url, Long timeout, @Optional List<NestedProcessor> responseProcessing) throws Exception
    {

        MuleMessage response = muleContext.getClient().request(url, timeout);

        Object processedResponse = response;
        if (responseProcessing != null)
        {
            for (NestedProcessor processor : responseProcessing)
            {
                processedResponse = processor.process(processedResponse);
            }

        }
        return processedResponse;

    }

    /**
     * <p>Do a real call to your inbound flows.</p>
     * <p/>
     * {@sample.xml ../../../doc/MClient-connector.xml.sample mclient:send}
     *
     * @param url                Path to call the transport, example: http://localhost:8081/tests
     * @param timeout            time out processing
     * @param responseProcessing Add any processing of the client response
     * @param messageProperties  The send message properties
     * @param payload            payload to send
     * @return Response call
     * @throws Exception an Exception
     */
    @Processor
    public Object send(String url, Object payload, @Optional Long timeout,
                       @Optional Map<String, Object> messageProperties,
                       @Optional List<NestedProcessor> responseProcessing) throws Exception
    {

        MuleMessage response = muleContext.getClient().send(url, payload, messageProperties, timeout);

        Object processedResponse = response;
        if (responseProcessing != null)
        {
            for (NestedProcessor processor : responseProcessing)
            {
                processedResponse = processor.process(processedResponse);
            }

        }
        return processedResponse;

    }

    /**
     * <p>Executes Polls of flows</p>
     * <p/>
     * {@sample.xml ../../../doc/MClient-connector.xml.sample mclient:schedulePoll}
     *
     * @param ofFlow The name of the flow that contains de poll
     * @throws Exception an Exception
     */
    @Processor
    public void schedulePoll(String ofFlow) throws Exception
    {

        pollManager.schedulePoll(ofFlow);
    }

    @Override
    public Scheduler process(Object o, Scheduler scheduler)
    {
        if (stopPollsByDefault ) {
           return MunitPollManager.postProcessSchedulerFactory(o, scheduler);
        }
        return scheduler;
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
        this.pollManager = new MunitPollManager(context);
    }

    public void setStopPollsByDefault(Boolean stopPollsByDefault)
    {
        this.stopPollsByDefault = stopPollsByDefault;
    }

    public Boolean getStopPollsByDefault()
    {
        return stopPollsByDefault;
    }
}
