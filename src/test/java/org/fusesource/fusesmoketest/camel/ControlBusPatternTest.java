package org.fusesource.fusesmoketest.camel;

import org.apache.camel.builder.RouteBuilder;
import org.fusesource.fusesmoketest.camel.CamelSmokeTestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Created by kearls on 07/10/14.
 */
public class ControlBusPatternTest extends CamelSmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(ControlBusPatternTest.class);

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .routeId("foo")
                        .to("jms:queue:resultHeader");
            }
        };
    }

    @Test(timeout = 60 * 1000)
    public void statusTest() throws Exception {
        String status = template.requestBody("controlbus:route?routeId=foo&action=status", null, String.class);
        LOG.info("STATUS " + status);
        assertEquals("Started", status);
    }
    
    @Test(timeout = 60 * 1000)
    public void simpleCommandTest() throws Exception {
        String status = template.requestBody("controlbus:route?routeId=foo&action=status", null, String.class);
        assertEquals("Started", status);

        template.sendBody("controlbus:language:simple", "${camelContext.stopRoute('foo')}");
        status = template.requestBody("controlbus:route?routeId=foo&action=status", null, String.class);
        assertEquals("Stopped", status);

        template.sendBody("controlbus:language:simple", "${camelContext.startRoute('foo')}");
        status = template.requestBody("controlbus:route?routeId=foo&action=status", null, String.class);
        assertEquals("Started", status);    
    }


    // FIXME how to enable JMX?
    @Test(timeout = 60 * 1000)
    public void statsCommandTest() throws Exception {
        String xml = template.requestBody("controlbus:route?action=stats", null, String.class);
        LOG.info("XML: " + xml);
        assertTrue(xml.contains("<processorStat id=\"to1\" exchangesCompleted=\"0\" exchangesFailed=\"0\" failuresHandled=\"0\""));

        template.sendBody("direct:start", "msg");
        template.sendBody("direct:start", "msg");

        xml = template.requestBody("controlbus:route?action=stats", null, String.class);
        assertTrue(xml.contains("<processorStat id=\"to1\" exchangesCompleted=\"2\" exchangesFailed=\"0\" failuresHandled=\"0\""));
    }

}
