package fr.ans.psc.pscload.state;

import java.io.IOException;

import fr.ans.psc.pscload.component.utils.FilesUtils;
import fr.ans.psc.pscload.state.exception.ExtractException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

public class FileDownloaded extends ProcessState {

	@Override
	public void runTask() throws LoadProcessException {
		try {
			FilesUtils.unzip(process.getDonwloadedFilename());
		} catch (IOException e) {
			// TODO log
			throw new ExtractException(e);
		}

	}

}
