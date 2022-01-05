package fr.ans.psc.pscload.visitor;

import java.util.List;

public interface Visitable {

	public List<String> accept(MapsCleanerVisitor visitor);
	
	public void accept(MapsUploaderVisitor visitor);

}