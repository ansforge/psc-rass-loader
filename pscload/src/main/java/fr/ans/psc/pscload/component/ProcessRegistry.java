package fr.ans.psc.pscload.component;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import fr.ans.psc.pscload.service.LoadProcess;

@Component
public class ProcessRegistry {
	
	private Map<String, LoadProcess> registry = new HashMap<String, LoadProcess>();
	
	public void register(String id, LoadProcess process) {
		registry.put(id, process);
	}
	
	public void unregister(String id) {
		registry.remove(id);
	}
	
	public void clear() {
		registry.clear();
	}
}
