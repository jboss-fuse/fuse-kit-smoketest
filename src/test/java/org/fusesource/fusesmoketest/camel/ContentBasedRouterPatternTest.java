package org.fusesource.fusesmoketest.camel;

import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;

/**
 * Created by kearls on 07/10/14.
 */
public class ContentBasedRouterPatternTest extends CamelSmokeTestBase {
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:messageRouter")
                        .choice()
                            .when(header("foo").isEqualTo("bar"))
                            .transform(body().append(" bar"))
                            .to("jms:smoketest.bar")
                        .when(header("foo").isEqualTo("cheese"))
                            .transform(body().append(" cheese"))
                            .to("jms:smoketest.cheese")
                        .otherwise()
                            .transform(body().append(" otherwise"))
                            .to("jms:smoketest.otherwise")
                        .endChoice();
            }
        };
    }

    @Test(timeout = 60 * 1000)
    public void contentBasedRouterTest() throws Exception {
        template.sendBodyAndHeader("direct:messageRouter", "Hello1", "foo", "bar");
        template.sendBodyAndHeader("direct:messageRouter", "Hello2", "foo", "cheese");
        template.sendBodyAndHeader("direct:messageRouter", "Hello3", "foo", "everything");

        String bar = receiveJmsMessage("smoketest.bar", 5000);
        String cheese = receiveJmsMessage("smoketest.cheese", 5000);
        String otherwise = receiveJmsMessage("smoketest.otherwise", 5000);

        assertEquals("Hello1 bar", bar);
        assertEquals("Hello2 cheese", cheese);
        assertEquals("Hello3 otherwise", otherwise);
    }
}
