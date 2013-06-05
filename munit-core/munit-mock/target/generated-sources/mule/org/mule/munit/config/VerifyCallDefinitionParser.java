
package org.mule.munit.config;

import javax.annotation.Generated;
import org.mule.munit.config.AbstractDefinitionParser.ParseDelegate;
import org.mule.munit.holders.AttributeExpressionHolder;
import org.mule.munit.processors.VerifyCallMessageProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

@Generated(value = "Mule DevKit Version 3.4.0", date = "2013-06-05T08:16:12-03:00", comments = "Build 3.4.0.1555.8df15c1")
public class VerifyCallDefinitionParser
    extends AbstractDefinitionParser
{


    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(VerifyCallMessageProcessor.class.getName());
        builder.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        parseConfigRef(element, builder);
        parseProperty(builder, element, "messageProcessor", "messageProcessor");
        parseListAndSetProperty(element, builder, "attributes", "attributes", "attribute", new ParseDelegate<BeanDefinition>() {


            public BeanDefinition parse(Element element) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(AttributeExpressionHolder.class);
                parseProperty(builder, element, "name", "name");
                if (hasAttribute(element, "whereValue-ref")) {
                    if (element.getAttribute("whereValue-ref").startsWith("#")) {
                        builder.addPropertyValue("whereValue", element.getAttribute("whereValue-ref"));
                    } else {
                        builder.addPropertyValue("whereValue", (("#[registry:"+ element.getAttribute("whereValue-ref"))+"]"));
                    }
                }
                return builder.getBeanDefinition();
            }

        }
        );
        parseProperty(builder, element, "times", "times");
        parseProperty(builder, element, "atLeast", "atLeast");
        parseProperty(builder, element, "atMost", "atMost");
        BeanDefinition definition = builder.getBeanDefinition();
        setNoRecurseOnDefinition(definition);
        attachProcessorDefinition(parserContext, definition);
        return definition;
    }

}