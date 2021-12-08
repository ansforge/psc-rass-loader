package fr.ans.psc.pscload.state;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.ans.psc.pscload.component.utils.Downloader;
import fr.ans.psc.pscload.state.exception.DownloadException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Idle extends ProcessState {
	
	private boolean customSSLContext;
	
	private String certfile;
	
	private String keyfile;
	
	private String cafile;

	public Idle(boolean customSSLContext, String keyfile, String certfile, String cafile ) {
		super();
		this.customSSLContext = customSSLContext;
		this.certfile = certfile;
		this.keyfile = keyfile;
		this.cafile = cafile;
	}

	@Override
	public void runTask() throws LoadProcessException {

		Downloader downloader = new Downloader();
		
		// downloads only if zip doesn't exist in our files directory
		String zipFile;
		try {
			if (customSSLContext) {
				downloader.initSSLContext(certfile, keyfile, cafile);
			}
			zipFile = downloader.downloadFile();
		} catch (GeneralSecurityException e) {
			//TODO log
			throw new DownloadException("SSL exception during downloading", e);

		} catch (IOException e) {
			// TODO log
			throw new DownloadException("Exception during downloading", e);
		}
		// If download is OK next step !
        if (zipFile != null) {
        	process.setDonwloadedFilename(zipFile);
        }

	}

}
