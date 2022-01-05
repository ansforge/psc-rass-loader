package fr.ans.psc.pscload.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpStatus;

import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.OperationMap;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.RassEntity;
import fr.ans.psc.pscload.model.Structure;

public class MapsCleanerVisitorImpl implements MapsCleanerVisitor {

	private MapsHandler maps;

	public MapsCleanerVisitorImpl(MapsHandler maps) {
		super();
		this.maps = maps;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> visit(OperationMap<String, RassEntity> map) {
		List<String> report = new ArrayList<>();
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			if (is5xxError(item.getReturnStatus())) {
				String[] dataItems = new String[] { map.getOperation().toString(), item.getInternalId(),
						String.valueOf(item.getReturnStatus()), };
				report.add(String.join(";", dataItems));
				switch (map.getOperation()) {
				case PS_CREATE:
					maps.getPsMap().remove(item.getInternalId());
					break;
				case PS_UPDATE:
					maps.getPsMap().replace(item.getInternalId(), (Professionnel) map.getOldValue(item.getInternalId()));
					break;
				case PS_DELETE:
					maps.getPsMap().put(item.getInternalId(), (Professionnel) item);
					break;
				case STRUCTURE_CREATE:
					maps.getStructureMap().remove(item.getInternalId());
					break;
				case STRUCTURE_UPDATE:
					maps.getStructureMap().replace(item.getInternalId(), (Structure) map.getOldValue(item.getInternalId()));
					break;
				default:
					break;
				}
			}
		});
		return report;
	}

	private boolean is5xxError(int rawReturnStatus) {
		return HttpStatus.valueOf(rawReturnStatus).is5xxServerError();
	}

}
