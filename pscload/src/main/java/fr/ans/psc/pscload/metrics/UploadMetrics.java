/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.metrics;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The Class UploadMetrics.
 */
public class UploadMetrics  implements Externalizable {

	private int psAdeliUploadSize = 0;
	private int psFinessUploadSize = 0;
	private int psSiretUploadSize = 0;
	private int psRppsUploadSize = 0;
	private int structureUploadSize = 0;

	/**
	 * Instantiates a new upload metrics.
	 */
	public UploadMetrics() {

	}

	/**
	 * Instantiates a new upload metrics.
	 *
	 * @param psAdeliUploadSize the ps adeli upload size
	 * @param psFinessUploadSize the ps finess upload size
	 * @param psSiretUploadSize the ps siret upload size
	 * @param psRppsUploadSize the ps rpps upload size
	 * @param structureUploadSize the structure upload size
	 */
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
		out.writeInt(psAdeliUploadSize);
		out.writeInt(psFinessUploadSize);
		out.writeInt(psRppsUploadSize);
		out.writeInt(psSiretUploadSize);
		out.writeInt(structureUploadSize);
		
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
