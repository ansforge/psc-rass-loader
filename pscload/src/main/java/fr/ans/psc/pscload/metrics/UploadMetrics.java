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

	private int psAdeliReferenceSize = 0;
	private int psFinessReferenceSize = 0;
	private int psSiretReferenceSize = 0;
	private int psRppsReferenceSize = 0;
	private int structureReferenceSize = 0;

	/**
	 * Instantiates a new upload metrics.
	 */
	public UploadMetrics() {

	}

	/**
	 * Instantiates a new upload metrics.
	 *
	 * @param psAdeliReferenceSize the ps adeli upload size
	 * @param psFinessReferenceSize the ps finess upload size
	 * @param psSiretReferenceSize the ps siret upload size
	 * @param psRppsReferenceSize the ps rpps upload size
	 * @param structureReferenceSize the structure upload size
	 */
	public UploadMetrics(int psAdeliReferenceSize, int psFinessReferenceSize, int psSiretReferenceSize, int psRppsReferenceSize,
						 int structureReferenceSize) {
		this.psAdeliReferenceSize = psAdeliReferenceSize;
		this.psFinessReferenceSize = psFinessReferenceSize;
		this.psSiretReferenceSize = psSiretReferenceSize;
		this.psRppsReferenceSize = psRppsReferenceSize;
		this.structureReferenceSize = structureReferenceSize;
	}

	public int getPsAdeliReferenceSize() {
		return psAdeliReferenceSize;
	}

	public void setPsAdeliReferenceSize(int psAdeliReferenceSize) {
		this.psAdeliReferenceSize = psAdeliReferenceSize;
	}

	public int getPsFinessReferenceSize() {
		return psFinessReferenceSize;
	}

	public void setPsFinessReferenceSize(int psFinessReferenceSize) {
		this.psFinessReferenceSize = psFinessReferenceSize;
	}

	public int getPsSiretReferenceSize() {
		return psSiretReferenceSize;
	}

	public void setPsSiretReferenceSize(int psSiretReferenceSize) {
		this.psSiretReferenceSize = psSiretReferenceSize;
	}

	public int getPsRppsReferenceSize() {
		return psRppsReferenceSize;
	}

	public void setPsRppsReferenceSize(int psRppsReferenceSize) {
		this.psRppsReferenceSize = psRppsReferenceSize;
	}

	public int getStructureReferenceSize() {
		return structureReferenceSize;
	}

	public void setStructureReferenceSize(int structureReferenceSize) {
		this.structureReferenceSize = structureReferenceSize;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(psAdeliReferenceSize);
		out.writeInt(psFinessReferenceSize);
		out.writeInt(psRppsReferenceSize);
		out.writeInt(psSiretReferenceSize);
		out.writeInt(structureReferenceSize);
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		psAdeliReferenceSize = in.readInt();
		psFinessReferenceSize = in.readInt();
		psRppsReferenceSize = in.readInt();
		psSiretReferenceSize = in.readInt();
		structureReferenceSize = in.readInt();
		
	}

}
