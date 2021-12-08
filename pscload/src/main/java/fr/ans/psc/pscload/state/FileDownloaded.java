package fr.ans.psc.pscload.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ans.psc.pscload.state.exception.ExtractException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

public class FileDownloaded extends ProcessState {

	
	/**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileDownloaded.class);
    
    
	@Override
	public void runTask() throws LoadProcessException {
		try {
			unzip(process.getDonwloadedFilename());
		} catch (IOException e) {
			// TODO log
			throw new ExtractException(e);
		}

	}
	
	   /**
     * Unzip.
     *
     * @param zipFilePath the zip file path
     * @return true if a new file is found and unzipped successfully
     * @throws IOException io exception
     */
    private boolean unzip(String zipFilePath) throws IOException {
        return unzip(zipFilePath, false);
    }

    /**
     * Unzip.
     *
     * @param zipFilePath the zip file path
     * @param clean      set to true to delete the zip file after unzipping
     * @return true if a new file is found and unzipped successfully
     * @throws IOException io exception
     */
    private boolean unzip(String zipFilePath, boolean clean) throws IOException {
        File zip = new File(zipFilePath);
        File destDir = zip.getParentFile();
        File[] existingFiles = zipsTextsNSers(destDir.listFiles()).get("txts").toArray(new File[0]);

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry zipEntry = zis.getNextEntry();

        boolean goAhead = false;
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            // check only entries that are files
            if (!zipEntry.isDirectory()) {
                // check if newer than what exists, otherwise go to next entry
                if (isNew(newFile, existingFiles)) {
                    goAhead = true;
                } else {
                    log.info("{} is not new, will not be extracted", newFile.getName());
                    zipEntry = zis.getNextEntry();
                    continue;
                }
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                // write file content
                log.info("unzipping into {}", newFile.getName());
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                log.info("unzip complete!");
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        if (clean) {
            log.info("clean set to true, deleting {}", zip.getName());
            zip.delete();
        }
        return goAhead;
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }




    /**
     * Zips and texts map.
     *
     * @param listOfFiles the list of files
     * @return the map
     * @throws IOException io exception
     */
    private Map<String, List<File>> zipsTextsNSers(File[] listOfFiles) throws IOException {
        Map<String, List<File>> filesMap = new HashMap<>();
        filesMap.put("zips", new ArrayList<>());
        filesMap.put("txts", new ArrayList<>());
        filesMap.put("sers", new ArrayList<>());

        for (File file : listOfFiles != null ? listOfFiles : new File[0]) {
            String type = Files.probeContentType(file.toPath());
            if (file.getName().endsWith(".ser")) {
                filesMap.get("sers").add(file);
            } else if (type != null && type.contains("zip")) {
                filesMap.get("zips").add(file);
            } else if (type != null && type.contains("text")) {
                filesMap.get("txts").add(file);
            }
        }
        return filesMap;
    }

    private int compare(File f1, File f2) {
        try {
            return getDateFromFileName(f1).compareTo(getDateFromFileName(f2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean isNew(File f1, File[] listF2) {
        if (listF2.length == 0) {
            return true;
        }
        for (File f2 : listF2) {
            if (compare(f1, f2) > 0) {
                return true;
            }
        }
        return false;
    }

    private Date getDateFromFileName(File file) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmm");

        String regex = ".*(\\d{12}).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(file.getName());
        if (m.find()) {
            return dateFormatter.parse(m.group(1));
        }
        return new Date(0);
    }


}
