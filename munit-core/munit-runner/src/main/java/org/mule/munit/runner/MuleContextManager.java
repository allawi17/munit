/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.munit.runner;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.api.config.MuleProperties;
import org.mule.api.context.MuleContextBuilder;
import org.mule.api.context.notification.MessageProcessorNotificationListener;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.config.builders.SimpleConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.context.notification.MessageProcessorNotification;
import org.mule.munit.common.extensions.MunitPlugin;
import org.mule.munit.runner.mule.context.MockingConfiguration;
import org.mule.munit.runner.mule.context.MunitSpringXmlConfigurationBuilder;
import org.mule.munit.runner.output.DefaultOutputHandler;
import org.mule.tck.TestingWorkListener;
import org.mule.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;


/**
 * <p>Starts and stops mule</p>
 *
 * @author Mulesoft Inc.
 * @since 3.3.2
 */
public class MuleContextManager
{

    public static final String CLASSNAME_ANNOTATIONS_CONFIG_BUILDER = "org.mule.org.mule.munit.config.AnnotationsConfigurationBuilder";

    private MockingConfiguration configuration;
    private Collection<MunitPlugin> plugins;

    public MuleContextManager(MockingConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public MuleContext startMule(String resources) throws Exception
    {
        MuleContext context = createMule(resources);
        context.start();
        startPlugins();

        return context;
    }

    public void killMule(MuleContext muleContext)
    {
        try
        {
            if (muleContext != null && !muleContext.isStopped())
            {
                muleContext.stop();
                stopPlugins();
            }
        }
        catch (Throwable e1)
        {

        }
        if (muleContext != null && !muleContext.isDisposed())
        {
            muleContext.dispose();
            disposePlugins();
        }
    }

    private MuleContext createMule(String resources) throws Exception
    {
        defineLogOutput(resources);

        MuleContext context;
        org.mule.api.context.MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();

        List<ConfigurationBuilder> builders = new ArrayList<ConfigurationBuilder>();
        builders.add(new SimpleConfigurationBuilder(properties()));
        if (ClassUtils.isClassOnPath(CLASSNAME_ANNOTATIONS_CONFIG_BUILDER,
                                     getClass()))
        {
            builders.add((ConfigurationBuilder) ClassUtils.instanciateClass(
                    CLASSNAME_ANNOTATIONS_CONFIG_BUILDER, ClassUtils.NO_ARGS,
                    getClass()));
        }
        builders.add(getBuilder(resources));
        MuleContextBuilder contextBuilder = new DefaultMuleContextBuilder();
        configureMuleContext(contextBuilder);
        context = muleContextFactory
                .createMuleContext(builders, contextBuilder);
        ((DefaultMuleConfiguration) context.getConfiguration())
                .setShutdownTimeout(0);

        context.getNotificationManager().setNotificationDynamic(true);
        context.getNotificationManager().addInterfaceToType(MessageProcessorNotificationListener.class, MessageProcessorNotification.class);
        plugins = new MunitPluginFactory().loadPlugins(context);
        initialisePlugins();

        return context;
    }

    private Properties properties()
    {
        Properties properties = configuration == null ? null : configuration.getStartUpProperties();
        if (properties == null)
        {
            properties = new Properties();
        }
        if (properties.get(MuleProperties.APP_HOME_DIRECTORY_PROPERTY) == null)
        {
            properties.setProperty(MuleProperties.APP_HOME_DIRECTORY_PROPERTY, new File(getClass().getResource("/").getPath()).getAbsolutePath());
        }
        return properties;
    }

    private void defineLogOutput(String resources) throws IOException
    {
        String path = System.getProperty(DefaultOutputHandler.OUTPUT_FOLDER_PROPERTY);
        if (path != null)
        {
            String name = resources.replace(".xml", "");
            Logger logger = Logger.getRootLogger();
            logger.removeAllAppenders();
            logger.addAppender(new FileAppender(new SimpleLayout(), String.format(path, name)));
            logger.setLevel(Level.INFO);
        }
    }

    protected ConfigurationBuilder getBuilder(String resources) throws Exception
    {
        return new MunitSpringXmlConfigurationBuilder(resources, configuration);
    }

    protected void configureMuleContext(MuleContextBuilder contextBuilder)
    {
        contextBuilder.setWorkListener(new TestingWorkListener());
    }

    private void startPlugins() throws MuleException
    {
        for (MunitPlugin plugin : plugins)
        {
            plugin.start();
        }
    }

    private void disposePlugins()
    {
        for (MunitPlugin plugin : plugins)
        {
            plugin.dispose();
        }
    }

    private void stopPlugins() throws MuleException
    {
        for (MunitPlugin plugin : plugins)
        {
            plugin.stop();
        }
    }

    private void initialisePlugins() throws InitialisationException
    {
        for (MunitPlugin plugin : plugins)
        {
            plugin.initialise();
        }
    }

}
