/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.component;

import java.util.concurrent.CountDownLatch;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;

/**
 * The Class Receiver.
 */
@RabbitListener(queues = "${queue.name}")
@Component
public class Receiver {

	//Inject by Spring
    private final ApiClient client;

    private final CountDownLatch latch = new CountDownLatch(1);

    /**
     * Instantiates a new receiver.
     *
     * @param client the client
     */
    public Receiver(ApiClient client) {
		super();
		this.client = client;
	}

	/**
	 * Receive message.
	 *
	 * @param message the message
	 */
	@RabbitHandler
    public void receiveMessage(String message) {
		PsApi psapi = new PsApi(client);
		StructureApi structureapi = new StructureApi(client);
		psapi.updatePs(buildProfessionnel(message));
		structureapi.updateStructure(buildStructure(message));
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
    
    private Professionnel buildProfessionnel(String message) {
    	String[] items = message.split("\\|", -1);
        return new Professionnel(items, true);
    }
    
    private Structure buildStructure(String message) {
    	String[] items = message.split("\\|", -1);
        return new Structure(items);
    }
}
