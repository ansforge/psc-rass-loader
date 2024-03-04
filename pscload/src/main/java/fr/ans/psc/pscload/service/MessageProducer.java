/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
///*
// * Copyright A.N.S 2021
// */

package fr.ans.psc.pscload.service;

import com.google.gson.Gson;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.operations.OperationType;
import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPsMessage(Professionnel professionnel, OperationType operation) {
        log.debug("Sending message for Ps {}", professionnel.getNationalId());

        String routingKey;
        switch (operation) {
            case CREATE:
                routingKey = PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY;
                break;
            case DELETE:
                routingKey = PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY;
                break;
            case UPDATE:
                routingKey = PS_UPDATE_MESSAGES_QUEUE_ROUTING_KEY;
                break;
            default:
                routingKey = "";
                break;
        }

        Gson json = new Gson();
        try {
            rabbitTemplate.convertAndSend(EXCHANGE_MESSAGES, routingKey, json.toJson(professionnel));
        } catch (AmqpException e) {
            log.error("Error occurred when sending Ps {} informations to queue manager", professionnel.getNationalId());
            e.printStackTrace();
        }
    }
}
