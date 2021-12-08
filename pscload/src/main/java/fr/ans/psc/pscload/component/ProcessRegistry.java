package fr.ans.psc.pscload.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.service.LoadProcess;

@Component
public class ProcessRegistry implements Serializable {
	

	private static final long serialVersionUID = -8371349854297886378L;

    @Value("${files.directory}")
    private String filesDirectory;
	
	private Map<String, LoadProcess> registry = new HashMap<String, LoadProcess>();
	
	private int id;
	
	public int nextId() {
		return ++id;
	}
	
	public int currentId() {
		return id;
	}
	
	/**
	 * Call this method in the shutdownHook to save the state of the process
	 * @throws FileNotFoundException
	 */
    public void serialize() throws FileNotFoundException {
    	Kryo kryo = new Kryo();
    	kryo.register(getClass());
        Output output = new Output(new FileOutputStream(filesDirectory + File.separator + "registry.ser"));
        kryo.writeClassAndObject(output, this);
        output.close();
    }
	
    public void deserialize() throws FileNotFoundException {
    	Kryo kryo = new Kryo();
    	kryo.register(getClass());
        Input input = new Input(new FileInputStream(filesDirectory + File.separator + "registry.ser"));
        kryo.readClassAndObject(input);
        input.close();
    }
    
    
	public void register(String id, LoadProcess process) {
		registry.put(id, process);
	}
	
	public void unregister(String id) {
		registry.remove(id);
	}
	
	public void clear() {
		registry.clear();
	}
	
	public boolean isEmpty() {
		return registry.isEmpty();
	}
	
}
