package fr.ans.psc.pscload.service;

import com.google.gson.Gson;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.operations.OperationType;
import fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPsMessage(Professionnel professionnel, OperationType operation) {
        log.debug("Sending message for Ps {}", professionnel.getNationalId());

        String routingKey = String.join("_", "PS", operation.name(), "QUEUE_ROUTING_KEY");
        Gson json = new Gson();
        try {
            rabbitTemplate.convertAndSend(PscRabbitMqConfiguration.EXCHANGE_MESSAGES, routingKey, json.toJson(professionnel));
        } catch (AmqpException e) {
            log.error("Error occurred when sending Ps {} informations to queue manager", professionnel.getNationalId());
            e.printStackTrace();
        }

    }
}
