/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http.request.urlcompressing;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import wicket.Component;
import wicket.request.compound.CompoundRequestCycleProcessor;
import wicket.util.collections.IntHashMap;
import wicket.util.collections.IntHashMap.Entry;

/**
 * This class generates UID for Component/Interface combinations when used in
 * conjunction with {@link WebURLCompressingCodingStrategy} and
 * {@link WebURLCompressingTargetResolverStrategy}
 * 
 * To use the 2 strategies you have to create your own
 * {@link CompoundRequestCycleProcessor} in your application's
 * newRequestCycleProcessor() method, which should be overridden and implemented
 * like this:
 * 
 * <pre>
 * protected IRequestCycleProcessor newRequestCycleProcessor()
 * {
 * 	return new CompoundRequestCycleProcessor(new WebURLCompressingCodingStrategy(),
 * 			new WebURLCompressingTargetResolverStrategy(), null, null, null);
 * }
 * </pre>
 * 
 * @since 1.2
 * 
 * @see WebURLCompressingCodingStrategy
 * @see WebURLCompressingTargetResolverStrategy
 * 
 * @author jcompagner
 */
public class URLCompressor implements Serializable
{
	private static final long serialVersionUID = 1L;

	private transient ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

	private transient IntHashMap<ComponentAndInterface> directComponentRefs = new IntHashMap<ComponentAndInterface>(); // uid->component/interface

	private int uid = 1;

	private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException
	{
		s.defaultReadObject();

		int size = s.readInt();
		queue = new ReferenceQueue<Object>();
		directComponentRefs = new IntHashMap<ComponentAndInterface>((int)(size * 1.25));

		while (--size >= 0)
		{
			int uid = s.readInt();
			Component component = (Component)s.readObject();
			String interfaceName = s.readUTF();

			IntKeyWeakReference ref = new IntKeyWeakReference(uid, component, queue);
			directComponentRefs.put(uid, new ComponentAndInterface(ref, interfaceName));
		}

	}

	private void writeObject(java.io.ObjectOutputStream s) throws IOException
	{
		Object ref = null;
		while ((ref = queue.poll()) != null)
		{
			directComponentRefs.remove(((IntKeyWeakReference)ref).uid);
		}

		s.defaultWriteObject();

		s.writeInt(directComponentRefs.size());

		Iterator it = directComponentRefs.entrySet().iterator();
		while (it.hasNext())
		{
			IntHashMap.Entry entry = (Entry)it.next();

			s.writeInt(entry.getKey());
			ComponentAndInterface cai = (ComponentAndInterface)entry.getValue();
			s.writeObject(cai.getComponent());
			s.writeUTF(cai.getInterfaceName());
		}
	}

	/**
	 * @return the next uid for this url compressor
	 */
	public int getNewUID()
	{
		return uid++;
	}

	/**
	 * Returns a uid for the combination component and the to call interface.
	 * Will return the same uid if it was already called for this specific
	 * combination.
	 * 
	 * @param component
	 *            The Component
	 * @param interfaceName
	 *            The interface name
	 * @return int The uid for the component/interfaceName combination
	 */
	public int getUIDForComponentAndInterface(Component component, String interfaceName)
	{
		int uid = 0;
		Iterator it = directComponentRefs.entrySet().iterator();
		while (it.hasNext())
		{
			IntHashMap.Entry entry = (IntHashMap.Entry)it.next();
			ComponentAndInterface cai = (ComponentAndInterface)entry.getValue();
			if (cai.getInterfaceName().equals(interfaceName) && cai.getComponent() == component)
			{
				uid = entry.getKey();
				break;
			}
		}
		if (uid == 0)
		{
			uid = getNewUID();
			IntKeyWeakReference ref = new IntKeyWeakReference(uid, component, queue);
			directComponentRefs.put(uid, new ComponentAndInterface(ref, interfaceName));
		}
		return uid;
	}

	/**
	 * Gets the combination
	 * 
	 * @param uidString
	 * @return ComponentAndInterface
	 */
	public ComponentAndInterface getComponentAndInterfaceForUID(String uidString)
	{
		Object ref;
		while ((ref = queue.poll()) != null)
		{
			directComponentRefs.remove(((IntKeyWeakReference)ref).uid);
		}
		int uid = Integer.parseInt(uidString);
		ComponentAndInterface cai = directComponentRefs.get(uid);
		return cai;
	}

	/**
	 * @author jcompagner
	 */
	public static class ComponentAndInterface
	{
		private static final long serialVersionUID = 1L;

		private final IntKeyWeakReference ref;
		private final String interfaceName;

		private ComponentAndInterface(IntKeyWeakReference ref, String interfaceName)
		{
			this.ref = ref;
			this.interfaceName = interfaceName;
		}

		/**
		 * @return Component The component that should be used to call the
		 *         interface
		 */
		public Component getComponent()
		{
			return (Component)ref.get();
		}

		/**
		 * @return String The interface name which should be called on the
		 *         component
		 */
		public String getInterfaceName()
		{
			return interfaceName;
		}
	}

	private static class IntKeyWeakReference extends WeakReference<Object>
	{
		private int uid;

		/**
		 * @param uid
		 * @param referent
		 * @param q
		 */
		public IntKeyWeakReference(int uid, Object referent, ReferenceQueue<Object> q)
		{
			super(referent, q);
			this.uid = uid;
		}
	}
}
