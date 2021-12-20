/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;

/**
 * The Class SerializableValueDifference.
 *
 * @param <V> the value type
 */
public class SerializableValueDifference<V> implements ValueDifference<V>, Externalizable {

	private  @Nullable V left;
	private  @Nullable V right;


	/**
	 * Instantiates a new serializable value difference.
	 *
	 * @param left the left
	 * @param right the right
	 */
	public SerializableValueDifference(@Nullable V left, @Nullable V right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public V leftValue() {

		return left;
	}

	@Override
	public V rightValue() {

		return right;
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (object instanceof MapDifference.ValueDifference) {
			MapDifference.ValueDifference<?> that = (MapDifference.ValueDifference<?>) object;
			return Objects.equal(this.left, that.leftValue()) && Objects.equal(this.right, that.rightValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(left, right);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(left);
		out.writeObject(right);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		left = (V) in.readObject();
		right = (V) in.readObject();
	}

}
