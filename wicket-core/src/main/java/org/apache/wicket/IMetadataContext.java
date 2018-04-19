package org.apache.wicket;

/**
 * Used to unify all metadata methods across the various
 * objects that support it.
 * <p>
 * This allows for metadata to be mutated at arms length without
 * dealing with the intricacies of each type that implements it.
 * <p>
 * Due to the inability to refer to implementing types (eg, Self in Rust.) we use the
 * R parameter to return the initial Object this context originated from.
 *
 * @param <B> The base type the metadata object must extend. (eg, {@link java.io.Serializable})
 * @param <R> The initial object this context originated from.
 * @author Jezza
 * @see Application
 * @see Component
 * @see Session
 * @see org.apache.wicket.request.cycle.RequestCycle
 */
public interface IMetadataContext<B, R extends IMetadataContext<B, R>> {
	<T extends B> T getMetaData(MetaDataKey<T> key);

	<T extends B> R setMetaData(MetaDataKey<T> key, T data);
}