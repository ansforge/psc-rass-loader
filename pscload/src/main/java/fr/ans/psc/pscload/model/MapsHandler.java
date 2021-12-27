package fr.ans.psc.pscload.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class MapsHandler implements Externalizable {

    private Map<String, Professionnel> psMap = new HashMap<>();

    private Map<String, Structure> structureMap = new HashMap<>();
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(psMap);
        out.writeObject(structureMap);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        psMap = (Map<String, Professionnel>) in.readObject();
        structureMap = (Map<String, Structure>) in.readObject();
    }
}
