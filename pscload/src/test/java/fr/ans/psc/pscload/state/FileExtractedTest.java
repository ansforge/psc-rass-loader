package fr.ans.psc.pscload.state;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.ans.psc.pscload.service.LoadProcess;

class FileExtractedTest {

	@Test
	@DisplayName("Initial diff with no old ser file and 5  ps")
	void initialDiffTaskTest() throws Exception {
		String rootpath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new FileExtracted());
		p.setExtractedFilename(Thread.currentThread().getContextClassLoader()
				.getResource("Extraction_ProSanteConnect_Personne_activite_202112120512.txt").getPath());
		p.runtask();
		assertEquals(5,p.getPsMap().entriesOnlyOnRight().size());
		assertEquals(0,p.getPsMap().entriesOnlyOnLeft().size());
	}

}
