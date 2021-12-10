package fr.ans.psc.pscload.state;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.ans.psc.pscload.component.utils.Downloader;
import fr.ans.psc.pscload.state.exception.DownloadException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Idle extends ProcessState {
	

	private static final long serialVersionUID = -2723088250644528474L;

	private boolean customSSLContext;
	
	private String certfile;
	
	private String keyfile;
	
	private String cafile;
	
	private transient Downloader downloader;
	
	public Idle() {
		super();
		this.customSSLContext = false;
		downloader = new Downloader();
	}
	
	public Idle(String extractDownloadUrl, String filesDirectory) {
		super();
		this.customSSLContext = false;
		downloader = new Downloader(extractDownloadUrl ,filesDirectory);
	}

	public Idle(String keyfile, String certfile, String cafile ) {
		super();
		this.customSSLContext = true;
		this.certfile = certfile;
		this.keyfile = keyfile;
		this.cafile = cafile;
		downloader = new Downloader();
	}
	
	
	public Idle(String keyfile , String certfile, String cafile, String filesDirectory,
			String extractDownloadUrl) {
		super();
		this.customSSLContext = true;
		this.certfile = certfile;
		this.keyfile = keyfile;
		this.cafile = cafile;
		downloader = new Downloader(extractDownloadUrl ,filesDirectory);
	}
	
	@Override
	public void runTask() throws LoadProcessException {

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
        	process.setDownloadedFilename(zipFile);
        }

	}

}
