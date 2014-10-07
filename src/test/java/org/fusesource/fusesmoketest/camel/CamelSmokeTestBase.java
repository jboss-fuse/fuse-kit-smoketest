package org.fusesource.fusesmoketest.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.fusesource.fusesmoketest.SmokeTestBase;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

/**
 * Created by kearls on 07/10/14.
 */
public class CamelSmokeTestBase extends SmokeTestBase {
    protected ConnectionFactory connectionFactory;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        JmsComponent jmsComponent = (JmsComponent) context.getComponent("jms");
        connectionFactory = new ActiveMQConnectionFactory(FUSE_USER, FUSE_PASSWORD,  AMQ_OPENWIRE_URL);
        jmsComponent.setConnectionFactory(connectionFactory);
        return context;
    }

    public String receiveJmsMessage(String queueName,  int timeout) throws Exception {
        Connection connection = connectionFactory.createConnection();
        connection.start();
        javax.jms.Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(destination);

        javax.jms.Message message = consumer.receive(timeout);

        String messageText = null;
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            messageText = textMessage.getText();

        } else if (message != null) {
            messageText = message.toString();
        }

        consumer.close();
        session.close();
        connection.close();

        return messageText;
    }
}
