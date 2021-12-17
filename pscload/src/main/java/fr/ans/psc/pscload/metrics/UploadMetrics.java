package fr.ans.psc.pscload.metrics;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class UploadMetrics  implements Externalizable {

	private int psAdeliUploadSize;
	private int psFinessUploadSize;
	private int psSiretUploadSize;
	private int psRppsUploadSize;
	private int structureUploadSize;

	public UploadMetrics() {

	}

	public UploadMetrics(int psAdeliUploadSize, int psFinessUploadSize, int psSiretUploadSize, int psRppsUploadSize,
			int structureUploadSize) {
		this.psAdeliUploadSize = psAdeliUploadSize;
		this.psFinessUploadSize = psFinessUploadSize;
		this.psSiretUploadSize = psSiretUploadSize;
		this.psRppsUploadSize = psRppsUploadSize;
		this.structureUploadSize = structureUploadSize;
	}

	public int getPsAdeliUploadSize() {
		return psAdeliUploadSize;
	}

	public void setPsAdeliUploadSize(int psAdeliUploadSize) {
		this.psAdeliUploadSize = psAdeliUploadSize;
	}

	public int getPsFinessUploadSize() {
		return psFinessUploadSize;
	}

	public void setPsFinessUploadSize(int psFinessUploadSize) {
		this.psFinessUploadSize = psFinessUploadSize;
	}

	public int getPsSiretUploadSize() {
		return psSiretUploadSize;
	}

	public void setPsSiretUploadSize(int psSiretUploadSize) {
		this.psSiretUploadSize = psSiretUploadSize;
	}

	public int getPsRppsUploadSize() {
		return psRppsUploadSize;
	}

	public void setPsRppsUploadSize(int psRppsUploadSize) {
		this.psRppsUploadSize = psRppsUploadSize;
	}

	public int getStructureUploadSize() {
		return structureUploadSize;
	}

	public void setStructureUploadSize(int structureUploadSize) {
		this.structureUploadSize = structureUploadSize;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.write(psAdeliUploadSize);
		out.write(psFinessUploadSize);
		out.write(psRppsUploadSize);
		out.write(psSiretUploadSize);
		out.write(structureUploadSize);
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		psAdeliUploadSize = in.readInt();
		psFinessUploadSize = in.readInt();
		psRppsUploadSize = in.readInt();
		psSiretUploadSize = in.readInt();
		structureUploadSize = in.readInt();
		
	}

}
