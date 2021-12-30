package fr.ans.psc.pscload.model;

import org.checkerframework.checker.nullness.qual.Nullable;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * The Class SerializableValueDifference.
 *
 * @param <V> the value type
 */
public class SerializableValueDifference<V> implements ValueDifference<V>, Serializable {
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
        if (object instanceof ValueDifference) {
            ValueDifference<?> that = (ValueDifference<?>) object;
            return Objects.equal(this.left, that.leftValue()) && Objects.equal(this.right, that.rightValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(left, right);
    }
}
