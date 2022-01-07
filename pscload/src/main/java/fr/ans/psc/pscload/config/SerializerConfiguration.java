/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.esotericsoftware.kryo.Kryo;

import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.model.entities.ExerciceProfessionnel;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.RefStructure;
import fr.ans.psc.pscload.model.entities.SavoirFaire;
import fr.ans.psc.pscload.model.entities.SituationExercice;
import fr.ans.psc.pscload.model.entities.Structure;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.PsCreateMap;
import fr.ans.psc.pscload.model.operations.PsDeleteMap;
import fr.ans.psc.pscload.model.operations.PsUpdateMap;
import fr.ans.psc.pscload.model.operations.StructureCreateMap;
import fr.ans.psc.pscload.model.operations.StructureUpdateMap;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.ReadyToComputeDiff;
import fr.ans.psc.pscload.state.ReadyToExtract;
import fr.ans.psc.pscload.state.SerializationInterrupted;
import fr.ans.psc.pscload.state.Submitted;
import fr.ans.psc.pscload.state.UploadInterrupted;
import fr.ans.psc.pscload.state.UploadingChanges;

/**
 * The Class SerializerConfiguration.
 */
@Configuration
public class SerializerConfiguration {

	@Bean
	public Kryo getKryo() {
		Kryo kryo = new Kryo();
		kryo.register(HashMap.class, 9);
		kryo.register(ArrayList.class, 10);
		kryo.register(Professionnel.class, 11);
		kryo.register(ExerciceProfessionnel.class, 12);
		kryo.register(SavoirFaire.class, 13);
		kryo.register(SituationExercice.class, 14);
		kryo.register(RefStructure.class, 15);
		kryo.register(Structure.class, 16);
		kryo.register(ProcessRegistry.class, 17);
		kryo.register(LoadProcess.class, 18);
		kryo.register(ProcessState.class, 19);
		kryo.register(Submitted.class, 20);
		kryo.register(DiffComputed.class, 21);
		kryo.register(ReadyToComputeDiff.class, 22);
		kryo.register(ReadyToExtract.class, 23);
		kryo.register(UploadingChanges.class, 24);
		kryo.register(ChangesApplied.class, 25);
		kryo.register(String[].class, 27);
		kryo.register(ConcurrentHashMap.class, 28);
		kryo.register(UploadInterrupted.class, 29);
		kryo.register(SerializationInterrupted.class, 30);
		kryo.register(OperationMap.class, 31);
		kryo.register(PsCreateMap.class, 32);
		kryo.register(PsUpdateMap.class, 33);
		kryo.register(PsDeleteMap.class, 34);
		kryo.register(StructureCreateMap.class, 35);
		kryo.register(StructureUpdateMap.class, 36);
		return kryo;
	}
}
