package org.apache.wicket.request.resource;

/**
 * Used to create a ResourceReference for a given request attributes
 */
public interface IResourceReferenceFactory
{
	/**
	 * Creates a new instance of ResourceReference with the given
	 * request attributes
	 *
	 * @param key
	 *      The object that brings the request attributes
	 * @return a ResourceReference or {@code null} if the factory cannot create
	 *      such with the given request attributes
	 */
	ResourceReference create(ResourceReference.Key key);
}