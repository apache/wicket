package org.apache.wicket.ng.request;


public interface CompoundRequestMapper extends RequestMapper
{

    /**
     * Registers a {@link RequestMapper}
     * 
     * @param encoder
     */
    void register(RequestMapper encoder);

    /**
     * Unregisters {@link RequestMapper}
     * 
     * @param encoder
     */
    void unregister(RequestMapper encoder);

}