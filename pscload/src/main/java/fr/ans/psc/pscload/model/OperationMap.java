package fr.ans.psc.pscload.model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import fr.ans.psc.pscload.visitor.MapsCleanerVisitor;
import fr.ans.psc.pscload.visitor.MapsUploaderVisitor;
import fr.ans.psc.pscload.visitor.OperationType;
import fr.ans.psc.pscload.visitor.Visitable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationMap<K, V> extends ConcurrentHashMap<String, RassEntity> implements Visitable {

	private OperationType operation;

	private ConcurrentMap<String, RassEntity> oldValues = new ConcurrentHashMap<>();

	
	
	public OperationMap() {
		super();
	}

	public OperationMap(OperationType operation) {
		super();
		this.operation = operation;
	}

	public void saveOldValue(String key, RassEntity value) {
		oldValues.put(key, value);
	}

	public RassEntity getOldValue(String key) {
		return oldValues.get(key);
	}

	@Override
	public void accept(MapsUploaderVisitor visitor) {
		visitor.visit((OperationMap<String, RassEntity>) this);

	}

	@Override
	public List<java.lang.String> accept(MapsCleanerVisitor visitor) {

		return visitor.visit((OperationMap<String, RassEntity>) this);
	}

}
