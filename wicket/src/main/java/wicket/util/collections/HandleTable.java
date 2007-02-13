package wicket.util.collections;

import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * Lightweight identity hash table which maps objects to integer handles,
 * assigned in ascending order (comes from {@link ObjectOutputStream}).
 */
public final class HandleTable
{
	/* number of mappings in table/next available handle */
	private int size;
	/* size threshold determining when to expand hash spine */
	private int threshold;
	/* factor for computing size threshold */
	private final float loadFactor;
	/* maps hash value -> candidate handle value */
	private int[] spine;
	/* maps handle value -> next candidate handle value */
	private int[] next;
	/* maps handle value -> associated object */
	private Object[] objs;
	
	public HandleTable()
	{
		this(16,0.75f);
	}

	public HandleTable(int initialCapacity, float loadFactor)
	{
		this.loadFactor = loadFactor;
		spine = new int[initialCapacity];
		next = new int[initialCapacity];
		objs = new Object[initialCapacity];
		threshold = (int)(initialCapacity * loadFactor);
		clear();
	}

	private void growEntries()
	{
		int newLength = (next.length << 1) + 1;
		int[] newNext = new int[newLength];
		System.arraycopy(next, 0, newNext, 0, size);
		next = newNext;

		Object[] newObjs = new Object[newLength];
		System.arraycopy(objs, 0, newObjs, 0, size);
		objs = newObjs;
	}

	private void growSpine()
	{
		spine = new int[(spine.length << 1) + 1];
		threshold = (int)(spine.length * loadFactor);
		Arrays.fill(spine, -1);
		for (int i = 0; i < size; i++)
		{
			insert(objs[i], i);
		}
	}

	private int hash(Object obj)
	{
		return System.identityHashCode(obj) & 0x7FFFFFFF;
	}

	private void insert(Object obj, int handle)
	{
		int index = hash(obj) % spine.length;
		objs[handle] = obj;
		next[handle] = spine[index];
		spine[index] = handle;
	}

	/**
	 * Assigns next available handle to given object, and returns handle
	 * value. Handles are assigned in ascending order starting at 0.
	 * 
	 * @param obj
	 * @return
	 */
	public int assign(Object obj)
	{
		if (size >= next.length)
		{
			growEntries();
		}
		if (size >= threshold)
		{
			growSpine();
		}
		insert(obj, size);
		return size++;
	}

	public void clear()
	{
		Arrays.fill(spine, -1);
		Arrays.fill(objs, 0, size, null);
		size = 0;
	}

	public boolean contains(Object obj)
	{
		return lookup(obj) != -1;
	}

	/**
	 * Looks up and returns handle associated with given object, or -1 if no
	 * mapping found.
	 * 
	 * @param obj
	 * @return
	 */
	public int lookup(Object obj)
	{
		if (size == 0)
		{
			return -1;
		}
		int index = hash(obj) % spine.length;
		for (int i = spine[index]; i >= 0; i = next[i])
		{
			if (objs[i] == obj)
			{
				return i;
			}
		}
		return -1;
	}

	int size()
	{
		return size;
	}
}