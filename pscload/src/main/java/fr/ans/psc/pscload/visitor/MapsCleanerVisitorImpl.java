/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.visitor;

import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpStatus;

import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.entities.Structure;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.PsCreateMap;
import fr.ans.psc.pscload.model.operations.PsDeleteMap;
import fr.ans.psc.pscload.model.operations.PsUpdateMap;
import fr.ans.psc.pscload.model.operations.StructureCreateMap;
import fr.ans.psc.pscload.model.operations.StructureUpdateMap;

/**
 * The Class MapsCleanerVisitorImpl.
 */
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
			if (is5xxError(item.getReturnStatus())) {
				generateReportLine(map, report, item);
				maps.getPsMap().remove(item.getInternalId());
			}
		});
	}

	@Override
	public void visit(PsUpdateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			if (is5xxError(item.getReturnStatus())) {
				generateReportLine(map, report, item);
				maps.getPsMap().replace(item.getInternalId(), (Professionnel) map.getOldValue(item.getInternalId()));
			}
		});
	}

	@Override
	public void visit(PsDeleteMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			if (is5xxError(item.getReturnStatus())) {
				generateReportLine(map, report, item);
				maps.getPsMap().put(item.getInternalId(), (Professionnel) item);
			}
		});
	}

	@Override
	public void visit(StructureCreateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			if (is5xxError(item.getReturnStatus())) {
				generateReportLine(map, report, item);
				maps.getStructureMap().remove(item.getInternalId());
			}
		});

	}

	@Override
	public void visit(StructureUpdateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			if (is5xxError(item.getReturnStatus())) {
				generateReportLine(map, report, item);
				maps.getStructureMap().replace(item.getInternalId(), (Structure) map.getOldValue(item.getInternalId()));
			}
		});
	}

	private boolean is5xxError(int rawReturnStatus) {
		return HttpStatus.valueOf(rawReturnStatus).is5xxServerError();
	}

	private void generateReportLine(OperationMap<String, RassEntity> map, List<String> report, RassEntity item) {
		String[] dataItems = new String[] { map.getOperation().toString(), item.getInternalId(),
				String.valueOf(item.getReturnStatus()), };
		report.add(String.join(";", dataItems));
	}

}
