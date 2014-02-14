/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.munit.config.spring;

import org.junit.Before;
import org.junit.Test;

import org.mule.munit.config.AssertOnEqualsMessageProcessor;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Mulesoft Inc.
 * @since 3.3.2
 */
public class MunitDefinitionParserTest
{

    public static final String A_MESSAGE = "A Message";
    public static final String EXPRESSION = "#[expression]";
    public static final String NON_EXPRESSION = "Non expression";
    Element element;
    ParserContext parserContext;


    @Before
    public void setUp()
    {
        element = mock(Element.class);
    }

    @Test
    public void test()
    {
        MunitDefinitionParser parser = new MockMunitDefinitionParser(AssertOnEqualsMessageProcessor.class, asList("message"), asList("expected", "value"));

        when(element.getAttribute("message")).thenReturn(A_MESSAGE);
        when(element.getAttribute("expected-ref")).thenReturn(NON_EXPRESSION);
        when(element.getAttribute("value-ref")).thenReturn(EXPRESSION);

        BeanDefinition beanDefinition = parser.parse(element, null);

        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();

        assertNotNull(beanDefinition);
        assertEquals(AssertOnEqualsMessageProcessor.class.getName(), beanDefinition.getBeanClassName());
        assertEquals(A_MESSAGE, propertyValues.getPropertyValue("message").getValue());
        assertEquals(EXPRESSION, propertyValues.getPropertyValue("value").getValue());
        assertTrue(propertyValues.getPropertyValue("expected").getValue() instanceof RuntimeBeanReference);

    }

    private static List<String> asList(String... attrs)
    {
        return Arrays.asList(attrs);
    }

    private class MockMunitDefinitionParser extends MunitDefinitionParser
    {

        public MockMunitDefinitionParser(Class mpClass, List<String> attributes, List<String> refAttributes)
        {
            super(mpClass, attributes, refAttributes);
        }

        @Override
        protected void attachProcessorDefinition(ParserContext parserContext, BeanDefinition definition)
        {

        }
    }
}
