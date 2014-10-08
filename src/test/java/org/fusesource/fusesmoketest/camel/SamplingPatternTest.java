package org.fusesource.fusesmoketest.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.junit.Ignore;

import javax.jms.TextMessage;
import java.util.concurrent.TimeUnit;

/**
 * Created by kearls on 07/10/14.
 */
public class SamplingPatternTest extends CamelSmokeTestBase {
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:sample")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                Message in = exchange.getIn();
                                LOG.info("BODY [" + in.getBody().toString() + "]");
                            }
                        })
                        .sample(2, TimeUnit.SECONDS)
                        .to("jms:sampling1");

                from("direct:sample-messageFrequency")
                        .sample().sampleMessageFrequency(3)
                        .to("jms:queue:sampling2");
            }
        };
    }

    @Test(timeout = 30 * 1000)
    public void samplingMessageFrequencyTest() throws Exception {
        template.sendBody("direct:sample-messageFrequency", "test1");
        template.sendBody("direct:sample-messageFrequency", "test2");
        template.sendBody("direct:sample-messageFrequency", "test3");
        template.sendBody("direct:sample-messageFrequency", "test4");
        template.sendBody("direct:sample-messageFrequency", "test5");
        template.sendBody("direct:sample-messageFrequency", "test6");
        template.sendBody("direct:sample-messageFrequency", "test7");

        String message1 = receiveJmsMessage("sampling2", 500);
        String message2 = receiveJmsMessage("sampling2", 500);
        String message3 = receiveJmsMessage("sampling2", 500);

        LOG.info(">>>> Message 1: " + message1);
        LOG.info(">>>> Message 2: " + message2);
        LOG.info(">>>> Message 3: " + message3);

        assertEquals("test3", message1);
        assertEquals("test6", message2);
        assertNull(message3);
    }

    @Test(timeout = 30 * 1000)
    public void samplingPeriodTest() throws Exception {
        template.sendBody("direct:sample", "test1");
        Thread.sleep(2000);
        template.sendBody("direct:sample", "test2");
        template.sendBody("direct:sample", "test3");
        template.sendBody("direct:sample", "test4");
        template.sendBody("direct:sample", "test5");
        Thread.sleep(2000);
        template.sendBody("direct:sample", "test6");
        template.sendBody("direct:sample", "test7");

        String message1 = receiveJmsMessage("sampling1", 500);
        String message2 = receiveJmsMessage("sampling1", 500);
        String message3 = receiveJmsMessage("sampling1", 500);
        String message4 = receiveJmsMessage("sampling2", 500);

        LOG.info(">>>> Message 1: " + message1);
        LOG.info(">>>> Message 2: " + message2);
        LOG.info(">>>> Message 3: " + message3);
        LOG.info(">>>> Message 4: " + message4);

        assertEquals("test1", message1);
        assertEquals("test2", message2);
        assertEquals("test6", message3);
        assertNull(message4);


    }
}
