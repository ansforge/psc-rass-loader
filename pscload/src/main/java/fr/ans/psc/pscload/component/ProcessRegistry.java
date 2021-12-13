package fr.ans.psc.pscload.component;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.ans.psc.pscload.service.LoadProcess;

@Component
public class ProcessRegistry implements  Externalizable  {
	

    @Value("${files.directory}")
    private String filesDirectory;
	
	private Map<String, LoadProcess> registry = new HashMap<String, LoadProcess>();
	
	private int id;
	
	public ProcessRegistry() {
	}

	public ProcessRegistry(String filesDirectory) {
		this.filesDirectory = filesDirectory;
	}

	public int nextId() {
		return ++id;
	}
	
	public int currentId() {
		return id;
	}
	
	public void register(String id, LoadProcess process) throws DuplicateKeyException {
		if(registry.get(id) != null) {
			registry.put(id, process);
			process.setId(id);
		}else {
			throw new DuplicateKeyException(String.format("Process : %s is already registered" , id));
		}
	}
	
	public void unregister(String id) {
		registry.remove(id);
	}
	

	public LoadProcess getCurrentProcess() {
		List<String> sortedKeys=new ArrayList<>(registry.keySet());
		Collections.sort(sortedKeys);
		return registry.get(sortedKeys.get(sortedKeys.size()-1));
			
	}
	
	public void clear() {
		registry.clear();
	}
	
	public boolean isEmpty() {
		return registry.isEmpty();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(id);
		out.writeObject(registry);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readInt();
		registry = (Map<String,LoadProcess>) in.readObject();
	}
	
	
}
