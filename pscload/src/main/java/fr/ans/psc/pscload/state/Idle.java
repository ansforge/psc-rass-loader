/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;

import fr.ans.psc.pscload.component.utils.Downloader;
import fr.ans.psc.pscload.state.exception.DownloadException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class Idle.
 */
@Slf4j
public class Idle extends ProcessState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8215232377218261245L;

	private boolean customSSLContext;
	
	private String certfile;
	
	private String keyfile;
	
	private String cafile;
	
	private transient Downloader downloader;
	
	/**
	 * Instantiates a new idle.
	 */
	public Idle() {
		super();
		this.customSSLContext = false;
		downloader = new Downloader();
	}
	
	/**
	 * Instantiates a new idle.
	 *
	 * @param extractDownloadUrl the extract download url
	 * @param filesDirectory the files directory
	 */
	public Idle(String extractDownloadUrl, String filesDirectory) {
		super();
		this.customSSLContext = false;
		downloader = new Downloader(extractDownloadUrl ,filesDirectory);
	}

	/**
	 * Instantiates a new idle.
	 *
	 * @param keyfile the keyfile
	 * @param certfile the certfile
	 * @param cafile the cafile
	 */
	public Idle(String keyfile, String certfile, String cafile ) {
		super();
		this.customSSLContext = true;
		this.certfile = certfile;
		this.keyfile = keyfile;
		this.cafile = cafile;
		downloader = new Downloader();
	}
	
	
	/**
	 * Instantiates a new idle.
	 *
	 * @param keyfile the keyfile
	 * @param certfile the certfile
	 * @param cafile the cafile
	 * @param filesDirectory the files directory
	 * @param extractDownloadUrl the extract download url
	 */
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(customSSLContext);
		out.writeObject(cafile);
		out.writeObject(certfile);
		out.writeObject(keyfile);
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		customSSLContext = in.readBoolean();
		cafile = (String) in.readObject();
		certfile = (String) in.readObject();
		keyfile = (String) in.readObject();
	}

}
