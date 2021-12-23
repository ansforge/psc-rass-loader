package fr.ans.psc.pscload.service;

import fr.ans.psc.pscload.model.MapsHandler;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class MapsManager {

    public MapsHandler loadMapsFromFile(File origin) {
        return new MapsHandler();
    }

    public void serializeMaps(String filename, MapsHandler mapsHandler) throws IOException {
        File mapsFile = new File(filename);
        FileOutputStream fileOutputStream = new FileOutputStream(mapsFile);
        ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
        mapsHandler.writeExternal(oos);
        oos.close();
    }

    public void deserializeMaps(String filename, MapsHandler mapsHandler) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fileInputStream);
        mapsHandler.readExternal(ois);
        ois.close();
    }
}
