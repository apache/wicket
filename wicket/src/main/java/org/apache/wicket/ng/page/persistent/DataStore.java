package org.apache.wicket.ng.page.persistent;

public interface DataStore
{	
	public byte[] getData(String sessionId, int id);

	public void removeData(String sessionId, int id);

	public void removeData(String sessionId);

	public void storeData(String sessionId, int id, byte[] data);

	public void destroy();	
	
	public boolean isReplicated();
}
