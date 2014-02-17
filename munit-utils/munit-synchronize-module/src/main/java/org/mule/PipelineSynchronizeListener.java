/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule;

import org.mule.api.context.notification.PipelineMessageNotificationListener;
import org.mule.context.notification.PipelineMessageNotification;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Listener for the pipeline Notifications
 * </p>
 *
 * @author Mulesoft Inc.
 * @since 3.4
 */
public class PipelineSynchronizeListener implements PipelineMessageNotificationListener<PipelineMessageNotification>, Synchronize
{

    AtomicInteger count = new AtomicInteger(0);


    @Override
    public void onNotification(PipelineMessageNotification notification)
    {
        if (notification.getAction() == PipelineMessageNotification.PROCESS_START)
        {
            count.incrementAndGet();
        }
        if (notification.getAction() == PipelineMessageNotification.PROCESS_END)
        {
            count.decrementAndGet();
        }
    }

    @Override
    public synchronized boolean readyToContinue()
    {
        return count.get() <= 0;
    }
}
