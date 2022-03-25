/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.visitor;

import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.entities.Structure;
import fr.ans.psc.pscload.model.operations.*;
import fr.ans.psc.pscload.state.exception.LockedMapException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.List;

/**
 * The Class MapsCleanerVisitorImpl.
 */
@Slf4j
public class MapsCleanerVisitorImpl implements MapsVisitor {

	private MapsHandler maps;
	
	private List<String> report;

	/**
	 * Instantiates a new maps cleaner visitor impl.
	 *
	 * @param maps the maps
	 */
	public MapsCleanerVisitorImpl(MapsHandler maps, List<String> report) {
		super();
		this.maps = maps;
		this.report = report;
	}

	@Override
	public void visit(PsCreateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			if(map.isLocked()) {
				log.info("Map is locked during shutdown");
				throw new LockedMapException();
			}
			generateReportLine(map, report, item);
			if (isInconsistentWithDatabase(item.getReturnStatus())) {
				maps.getPsMap().remove(item.getInternalId());
			}
		});
	}

	@Override
	public void visit(PsUpdateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			if(map.isLocked()) {
				log.info("Map is locked during shutdown");
				throw new LockedMapException();
			}
			generateReportLine(map, report, item);
			if (isInconsistentWithDatabase(item.getReturnStatus())) {
				maps.getPsMap().replace(item.getInternalId(), (Professionnel) map.getOldValue(item.getInternalId()));
			}
		});
	}

	@Override
	public void visit(PsDeleteMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			if(map.isLocked()) {
				log.info("Map is locked during shutdown");
				throw new LockedMapException();
			}
			generateReportLine(map, report, item);
			if (isInconsistentWithDatabase(item.getReturnStatus())) {
				maps.getPsMap().put(item.getInternalId(), (Professionnel) item);
			}
		});
	}

	private boolean isInconsistentWithDatabase(int rawReturnStatus) {
		// CONFLICT and GONE mean that the db is already in the state we want
		HttpStatus status = HttpStatus.valueOf(rawReturnStatus);

		if (status.equals(HttpStatus.CONFLICT)) {
			return false;
		} else {
			return !status.equals(HttpStatus.GONE);
		}
	}

	private void generateReportLine(OperationMap<String, RassEntity> map, List<String> report, RassEntity item) {
		String[] dataItems = new String[] { map.getOperation().toString(), item.getInternalId(),
				String.valueOf(item.getReturnStatus()), };
		report.add(String.join(";", dataItems));
	}

}
