/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.munit.common.mp;

import net.sf.cglib.proxy.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.modules.interceptor.processors.MessageProcessorId;
import org.mule.modules.interceptor.spring.BeanFactoryMethodBuilder;
import org.mule.modules.interceptor.spring.MethodInterceptorFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


/**
 * <p>
 * This is the Message processor interceptor factory.
 * </p>
 *
 * @author Mulesoft Inc.
 * @since 3.3.2
 */
public class MunitMessageProcessorInterceptorFactory extends MethodInterceptorFactory {

    protected transient Log logger = LogFactory.getLog(getClass());

    /**
     * <p>
     * For operations that are not {@link org.mule.api.processor.MessageProcessor#process(org.mule.api.MuleEvent)} just do
     * nothing
     * </p>
     */
    private static Callback NULL_METHOD_INTERCEPTOR = new NoOp() {
    };

    private static CallbackFilter FACTORY_BEAN_FILTER = new CallbackFilter() {

        @Override
        public int accept(Method method) {
            if ("getObject".equals(method.getName())) {
                return 0;
            }
            return 1;
        }
    };

    private static CallbackFilter MESSAGE_PROCESSOR_FILTER = new CallbackFilter() {

        @Override
        public int accept(Method method) {
            if ("process".equals(method.getName())) {
                return 0;
            }
            return 1;
        }
    };

    /**
     * <p>
     * The Id in the spring registry of Mule
     * </p>
     */
    public static final String ID = "__messageProcessorEnhancerFactory";

    /**
     * <p>
     * Util method that creates a @see #BeanFactoryMethodBuilder based on an abstract bean definition
     * </p>
     * <p/>
     * <p>The usage:</p>
     * <p/>
     * <code>
     * addFactoryDefinitionTo(beanDefinition).withConstructorArguments(beanDefinition.getBeanClass());
     * </code>
     *
     * @param beanDefinition <p>
     *                       The bean definition that we want to modify
     *                       </p>
     * @return
     */
    public static BeanFactoryMethodBuilder addFactoryDefinitionTo(AbstractBeanDefinition beanDefinition) {
        return new BeanFactoryMethodBuilder(beanDefinition, "create", ID);
    }


    // TODO: Find a cleaner way to make spring find a constructor with dynamic size parameters. Now Munit allows mocking MP with 2 or less constructors

    /**
     * <p>
     * Factory Method to create Message processors with a constructor with one parameter ( {@param constructorArgument} )
     * </p>
     */
    public Object create(Class realMpClass, MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber,
                         Object constructorArgument) {
        return create(realMpClass, id, attributes, fileName, lineNumber, new Object[]{constructorArgument});
    }

    /**
     * <p>
     * Factory Method to create Message processors with a constructor with two parameters ( {@param constructorArgument} )
     * </p>
     */
    public Object create(Class realMpClass, MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber,
                         Object constructorArgument1, Object constructorArgument2) {
        return create(realMpClass, id, attributes, fileName, lineNumber, new Object[]{constructorArgument1, constructorArgument2});
    }

    public Object create(Class realMpClass, MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber,
                         Object constructorArgument1, Object constructorArgument2, Object constructorArgument3) {
        return create(realMpClass, id, attributes, fileName, lineNumber, new Object[]{constructorArgument1, constructorArgument2, constructorArgument3});
    }

    public Object create(Class realMpClass, MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber,
                         Object constructorArgument1, Object constructorArgument2, Object constructorArgument3, Object constructorArgument4) {
        return create(realMpClass, id, attributes, fileName, lineNumber, new Object[]{constructorArgument1, constructorArgument2, constructorArgument3, constructorArgument4});
    }

    public Object create(Class realMpClass, MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber,
                         Object constructorArgument1, Object constructorArgument2, Object constructorArgument3, Object constructorArgument4, Object constructorArgument5) {
        return create(realMpClass, id, attributes, fileName, lineNumber, new Object[]{constructorArgument1, constructorArgument2, constructorArgument3, constructorArgument4, constructorArgument5});
    }


    /**
     * <p>
     * Factory method used to create Message Processors without constructor parameters.
     * </p>
     *
     * @param realMpClass The class that we want to mock
     * @param id          The {@link MessageProcessorId} that identifies the message processor
     * @param attributes  The Message Processor attributes used to identify the mock
     * @param fileName    The name of the file where the message processor is written down
     * @param lineNumber  The line number where the message processor is written down
     * @return The Mocked object, if it fails mocking then the real object.
     */
    public Object create(Class realMpClass, MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber) {
        try {
            Enhancer e = createEnhancer(realMpClass, id, attributes, fileName, lineNumber);
            return e.create();
        } catch (Throwable e) {
            logger.warn("The message processor " + id.getFullName() + " could not be mocked");
            try {
                return realMpClass.newInstance();
            } catch (Throwable e1) {
                throw new Error("The message processor " + id.getFullName() + " could not be created", e);
            }
        }
    }

    /**
     * <p>
     * Factory method used to create Message Processors with constructor parameters.
     * </p>
     *
     * @param realMpClass          The class that we want to mock
     * @param id                   The {@link MessageProcessorId} that identifies the message processor
     * @param attributes           The Message Processor attributes used to identify the mock
     * @param fileName             The name of the file where the message processor is written down
     * @param lineNumber           The line number where the message processor is written down
     * @param constructorArguments The Array of constructor arguments of the message processor
     * @return The Mocked object, if it fails mocking then the real object.
     */
    public Object create(Class realMpClass, MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber,
                         Object[] constructorArguments) {
        try {
            Enhancer e = createEnhancer(realMpClass, id, attributes, fileName, lineNumber);
            if (constructorArguments != null && constructorArguments.length != 0) {
                Class[] classes = findConstructorArgumentTypes(realMpClass, constructorArguments);
                if (classes != null) {
                    return e.create(classes, constructorArguments);
                } else {
                    throw new Error("The message processor " + id.getFullName() + " could not be created, because " +
                            "there is no matching constructor");
                }
            } else {
                return e.create();
            }
        } catch (Throwable e) {
            logger.warn("The message processor " + id.getFullName() + " could not be mocked");
            try {
                return createRealMpInstance(realMpClass, id, constructorArguments);
            } catch (Throwable e1) {
                throw new Error("The message processor " + id.getFullName() + " could not be created", e1);
            }
        }
    }

    private Object createRealMpInstance(Class realMpClass, MessageProcessorId id, Object[] constructorArguments) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        if (constructorArguments != null && constructorArguments.length != 0) {
            Class[] classes = findConstructorArgumentTypes(realMpClass, constructorArguments);
            if (classes != null) {
                Constructor constructor = realMpClass.getConstructor(classes);
                return constructor.newInstance(constructorArguments);
            } else {
                logger.warn("The message processor " + id.getFullName() + " has no matching constructor for the offered parameters creating it with default constructor");
                return realMpClass.newInstance();
            }

        } else {
            return realMpClass.newInstance();
        }
    }

    private Class getPrimitiveWrapperClass(Class clazz) {

        String primitiveName = clazz.toString();

        if ("boolean".equals(primitiveName)) {
            return Boolean.class;
        }

        if ("byte".equals(primitiveName)) {
            return Byte.class;
        }
        if ("char".equals(primitiveName)) {
            return Character.class;
        }
        if ("double".equals(primitiveName)) {
            return Double.class;

        }
        if ("float".equals(primitiveName)) {
            return Float.class;
        }

        if ("int".equals(primitiveName)) {
            return Integer.class;
        }

        if ("long".equals(primitiveName)) {
            return Long.class;

        }
        if ("short".equals(primitiveName)) {
            return Short.class;

        }
        if ("void".equals(primitiveName)) {
            return Void.class;

        }
        return null;
    }

    private Class[] findConstructorArgumentTypes(Class realMpClass, Object[] constructorArguments) {
        Constructor[] declaredConstructors = realMpClass.getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == constructorArguments.length) {
                boolean mapsCorrectly = true;
                for (int i = 0; i < parameterTypes.length; i++) {

                    Class parameterClass = parameterTypes[i].isPrimitive() ? getPrimitiveWrapperClass(parameterTypes[i]) : parameterTypes[i];
                    Class constructorArgumentClass = constructorArguments[i].getClass().isPrimitive() ? getPrimitiveWrapperClass(constructorArguments[i].getClass()) : constructorArguments[i].getClass();

                    mapsCorrectly &= parameterClass.isAssignableFrom(constructorArgumentClass);
                }
                if (mapsCorrectly) {
                    return parameterTypes;
                }
            }
        }
        return null;
    }

    protected Enhancer createEnhancer(Class realMpClass, MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber) {

        Enhancer e = new Enhancer();
        e.setSuperclass(realMpClass);
        e.setUseCache(false);
        e.setAttemptLoad(true);
        e.setInterceptDuringConstruction(true);
        e.setNamingPolicy(new MunitNamingPolicy());

        if (FactoryBean.class.isAssignableFrom(realMpClass)) {
            createFactoryBeanCallback(id, attributes, fileName, lineNumber, e);
        } else {
            createMessageProcessorCallback(id, attributes, fileName, lineNumber, e);
        }
        return e;
    }


    private void createMessageProcessorCallback(MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber, Enhancer e) {
        MunitMessageProcessorInterceptor callback = new MunitMessageProcessorInterceptor();
        callback.setId(id);
        callback.setAttributes(attributes);
        callback.setFileName(fileName);
        callback.setLineNumber(lineNumber);
        e.setCallbacks(new Callback[]{callback, NULL_METHOD_INTERCEPTOR});
        e.setCallbackFilter(MESSAGE_PROCESSOR_FILTER);
    }

    private void createFactoryBeanCallback(MessageProcessorId id, Map<String, String> attributes, String fileName, String lineNumber, Enhancer e) {
        MessageProcessorFactoryBeanInterceptor callback = new MessageProcessorFactoryBeanInterceptor();
        callback.setId(id);
        callback.setAttributes(attributes);
        callback.setFileName(fileName);
        callback.setLineNumber(lineNumber);
        e.setCallbacks(new Callback[]{callback, NULL_METHOD_INTERCEPTOR});
        e.setCallbackFilter(FACTORY_BEAN_FILTER);
    }

    /**
     * <p>
     * Actual implementation of the interceptor creation
     * </p>
     *
     * @return <p>
     * A {@link MunitMessageProcessorInterceptor} object
     * </p>
     */
    @Override
    protected MethodInterceptor createInterceptor() {
        return new MunitMessageProcessorInterceptor();
    }


    @Override
    public Object create(Class realMpClass, Object... objects) {
        return super.create(realMpClass, objects);
    }

}
