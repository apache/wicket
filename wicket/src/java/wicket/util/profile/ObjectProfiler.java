/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.profile;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

// ----------------------------------------------------------------------------

/**
 * This non-instantiable class presents an API for object sizing and profiling as
 * described in the article. See individual methods for details.
 * <P>
 * This implementation is J2SE 1.4+ only. You would need to code your own identity hashmap
 * to port this to earlier Java versions.
 * <P>
 * Security: this implementation uses AccessController.doPrivileged() so it could be
 * granted privileges to access non-public class fields separately from your main
 * application code. The minimum set of persmissions necessary for this class to function
 * correctly follows:
 * 
 * <pre>
 * 
 * 
 * 
 *         permission java.lang.RuntimePermission &quot;accessDeclaredMembers&quot;;
 *         permission java.lang.reflect.ReflectPermission &quot;suppressAccessChecks&quot;;
 * 
 * 
 *  
 * </pre>
 * 
 * @see IObjectProfileNode
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov
 *         </a>, 2003
 */
public abstract class ObjectProfiler
{
    // public: ................................................................
    // the following constants are physical sizes (in bytes) and are
    // JVM-dependent:
    // [the current values are Ok for most 32-bit JVMs]
    static final int OBJECT_SHELL_SIZE = 8; // java.lang.Object shell

    /** size in bytes. **/
    static final int OBJREF_SIZE = 4;

    static final int LONG_FIELD_SIZE = 8;

    static final int INT_FIELD_SIZE = 4;

    static final int SHORT_FIELD_SIZE = 2;

    static final int CHAR_FIELD_SIZE = 2;

    static final int BYTE_FIELD_SIZE = 1;

    static final int BOOLEAN_FIELD_SIZE = 1;

    static final int DOUBLE_FIELD_SIZE = 8;

    static final int FLOAT_FIELD_SIZE = 4;

    /**
     * Set this to 'true' to make the node names default to using class names without
     * package prefixing for more compact dumps
     */
    public static final boolean SHORT_TYPE_NAMES = false;

    /**
     * If this is 'true', node names will use short class names for common classes in
     * java.lang.* and java.util.*, regardless of {@link #SHORT_TYPE_NAMES}setting.
     */
    public static final boolean SHORT_COMMON_TYPE_NAMES = true;

    // protected: .............................................................
    // package: ...............................................................
    static final String INPUT_OBJECT_NAME = "<INPUT>"; // root node name

    // class metadata cache:
    private static final Map CLASS_METADATA_CACHE = new WeakHashMap(101);

    private ObjectProfiler()
    {
    } // this class is not extendible

    /**
     * Estimates the full size of the object graph rooted at 'obj'. Duplicate data
     * instances are correctly accounted for. The implementation is not recursive.
     * <P>
     * Invariant: sizeof(obj) == profile(obj).size() if 'obj' is not null
     * @param obj input object instance to be measured
     * @return 'obj' size [0 if 'obj' is null']
     */
    public static int sizeof(final Object obj)
    {
        if (obj == null)
        {
            return 0;
        }

        final IdentityHashMap visited = new IdentityHashMap();

        return computeSizeof(obj, visited, CLASS_METADATA_CACHE);
    }

    /**
     * Estimates the full size of the object graph rooted at 'obj' by pre-populating the
     * "visited" set with the object graph rooted at 'base'. The net effect is to compute
     * the size of 'obj' by summing over all instance data contained in 'obj' but not in
     * 'base'.
     * @param base graph boundary [may not be null]
     * @param obj input object instance to be measured
     * @return 'obj' size [0 if 'obj' is null']
     */
    public static int sizedelta(final Object base, final Object obj)
    {
        if (obj == null)
        {
            return 0;
        }

        if (base == null)
        {
            throw new IllegalArgumentException("null input: base");
        }

        final IdentityHashMap visited = new IdentityHashMap();

        computeSizeof(base, visited, CLASS_METADATA_CACHE);

        return visited.containsKey(obj) ? 0 : computeSizeof(obj, visited, CLASS_METADATA_CACHE);
    }

    /**
     * Creates a spanning tree representation for instance data contained in 'obj'. The
     * tree is produced using bread-first traversal over the full object graph implied by
     * non-null instance and array references originating in 'obj'.
     * @see IObjectProfileNode
     * @param obj input object instance to be profiled [may not be null]
     * @return the profile tree root node [never null]
     */
    public static IObjectProfileNode profile(final Object obj)
    {
        if (obj == null)
        {
            throw new IllegalArgumentException("null input: obj");
        }

        final IdentityHashMap visited = new IdentityHashMap();

        final ObjectProfileNode root = createProfileTree(obj, visited, CLASS_METADATA_CACHE);

        finishProfileTree(root);

        return root;
    }

    // convenience methods:
    /**
     * gets path as a string.
     * @param path
     * @return path as a string
     */
    public static String pathName(final IObjectProfileNode[] path)
    {
        final StringBuffer s = new StringBuffer();

        for (int i = 0; i < path.length; ++i)
        {
            if (i != 0)
            {
                s.append('/');
            }

            s.append(path[i].name());
        }

        return s.toString();
    }

    /**
     * @param field
     * @param shortClassNames
     * @return field name
     */
    public static String fieldName(final Field field, final boolean shortClassNames)
    {
        return typeName(field.getDeclaringClass(), shortClassNames).concat("#").concat(
                field.getName());
    }

    /**
     * @param cls
     * @param shortClassNames
     * @return type name
     */
    public static String typeName(Class cls, final boolean shortClassNames)
    {
        int dims = 0;

        for (; cls.isArray(); ++dims)
        {
            cls = cls.getComponentType();
        }

        String clsName = cls.getName();

        if (shortClassNames)
        {
            final int lastDot = clsName.lastIndexOf('.');

            if (lastDot >= 0)
            {
                clsName = clsName.substring(lastDot + 1);
            }
        }
        else if (SHORT_COMMON_TYPE_NAMES)
        {
            if (clsName.startsWith("java.lang."))
            {
                clsName = clsName.substring(10);
            }
            else if (clsName.startsWith("java.util."))
            {
                clsName = clsName.substring(10);
            }
        }

        for (int i = 0; i < dims; ++i)
        {
            clsName = clsName.concat("[]");
        }

        return clsName;
    }

    /*
     * The main worker method for sizeof() and sizedelta().
     */
    private static int computeSizeof(Object obj, final IdentityHashMap visited,
            final Map /* Class->ClassMetadata */metadataMap)
    {
        // this uses depth-first traversal; the exact graph traversal algorithm
        // does not matter for computing the total size and this method could be
        // easily adjusted to do breadth-first instead (addLast() instead of
        // addFirst()),
        // however, dfs/bfs require max queue length to be the length of the
        // longest
        // graph path/width of traversal front correspondingly, so I expect
        // dfs to use fewer resources than bfs for most Java objects;
        if (obj == null)
        {
            return 0;
        }

        final LinkedList queue = new LinkedList();

        visited.put(obj, obj);
        queue.add(obj);

        int result = 0;

        final ClassAccessPrivilegedAction caAction = new ClassAccessPrivilegedAction();
        final FieldAccessPrivilegedAction faAction = new FieldAccessPrivilegedAction();

        while (!queue.isEmpty())
        {
            obj = queue.removeFirst();

            final Class objClass = obj.getClass();

            if (objClass.isArray())
            {
                final int arrayLength = Array.getLength(obj);
                final Class componentType = objClass.getComponentType();

                result += sizeofArrayShell(arrayLength, componentType);

                if (!componentType.isPrimitive())
                {
                    // traverse each array slot:
                    for (int i = 0; i < arrayLength; ++i)
                    {
                        final Object ref = Array.get(obj, i);

                        if ((ref != null) && !visited.containsKey(ref))
                        {
                            visited.put(ref, ref);
                            queue.addFirst(ref);
                        }
                    }
                }
            }
            else
            // the object is of a non-array type
            {
                final ClassMetadata metadata = getClassMetadata(objClass, metadataMap, caAction,
                        faAction);
                final Field[] fields = metadata.m_refFields;

                result += metadata.m_shellSize;

                // traverse all non-null ref fields:
                for (int f = 0, fLimit = fields.length; f < fLimit; ++f)
                {
                    final Field field = fields[f];

                    final Object ref;

                    try
                    // to get the field value:
                    {
                        ref = field.get(obj);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("cannot get field ["
                                + field.getName() + "] of class ["
                                + field.getDeclaringClass().getName() + "]: " + e.toString());
                    }

                    if ((ref != null) && !visited.containsKey(ref))
                    {
                        visited.put(ref, ref);
                        queue.addFirst(ref);
                    }
                }
            }
        }

        return result;
    }

    /*
     * Performs phase 1 of profile creation: bread-first traversal and node creation.
     */
    private static ObjectProfileNode createProfileTree(Object obj, final IdentityHashMap visited,
            final Map /* Class->ClassMetadata */metadataMap)
    {
        final ObjectProfileNode root = new ObjectProfileNode(null, obj, null);

        final LinkedList queue = new LinkedList();

        queue.addFirst(root);
        visited.put(obj, root);

        final ClassAccessPrivilegedAction caAction = new ClassAccessPrivilegedAction();
        final FieldAccessPrivilegedAction faAction = new FieldAccessPrivilegedAction();

        while (!queue.isEmpty())
        {
            final ObjectProfileNode node = (ObjectProfileNode) queue.removeFirst();

            obj = node.m_obj;

            final Class objClass = obj.getClass();

            if (objClass.isArray())
            {
                final int arrayLength = Array.getLength(obj);
                final Class componentType = objClass.getComponentType();

                // add shell pseudo-node:
                final AbstractShellProfileNode shell = new ArrayShellProfileNode(node, objClass,
                        arrayLength);

                shell.m_size = sizeofArrayShell(arrayLength, componentType);

                node.m_shell = shell;
                node.addFieldRef(shell);

                if (!componentType.isPrimitive())
                {
                    // traverse each array slot:
                    for (int i = 0; i < arrayLength; ++i)
                    {
                        final Object ref = Array.get(obj, i);

                        if (ref != null)
                        {
                            ObjectProfileNode child = (ObjectProfileNode) visited.get(ref);

                            if (child != null)
                            {
                                ++child.m_refcount;
                            }
                            else
                            {
                                child = new ObjectProfileNode(node, ref, new ArrayIndexLink(
                                        node.m_link, i));
                                node.addFieldRef(child);

                                queue.addLast(child);
                                visited.put(ref, child);
                            }
                        }
                    }
                }
            }
            else
            // the object is of a non-array type
            {
                final ClassMetadata metadata = getClassMetadata(objClass, metadataMap, caAction,
                        faAction);
                final Field[] fields = metadata.m_refFields;

                // add shell pseudo-node:
                final AbstractShellProfileNode shell = new ObjectShellProfileNode(node,
                        metadata.m_primitiveFieldCount, metadata.m_refFields.length);

                shell.m_size = metadata.m_shellSize;

                node.m_shell = shell;
                node.addFieldRef(shell);

                // traverse all non-null ref fields:
                for (int f = 0, fLimit = fields.length; f < fLimit; ++f)
                {
                    final Field field = fields[f];

                    final Object ref;

                    try
                    // to get the field value:
                    {
                        ref = field.get(obj);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("cannot get field ["
                                + field.getName() + "] of class ["
                                + field.getDeclaringClass().getName() + "]: " + e.toString());
                    }

                    if (ref != null)
                    {
                        ObjectProfileNode child = (ObjectProfileNode) visited.get(ref);

                        if (child != null)
                        {
                            ++child.m_refcount;
                        }
                        else
                        {
                            child = new ObjectProfileNode(node, ref, new ClassFieldLink(field));
                            node.addFieldRef(child);

                            queue.addLast(child);
                            visited.put(ref, child);
                        }
                    }
                }
            }
        }

        return root;
    }

    /*
     * Performs phase 2 of profile creation: totalling of node sizes (via non-recursive
     * post-order traversal of the tree created in phase 1) and 'locking down' of profile
     * nodes into their most compact form.
     */
    private static void finishProfileTree(ObjectProfileNode node)
    {
        final LinkedList queue = new LinkedList();
        IObjectProfileNode lastFinished = null;

        while (node != null)
        {
            // note that an unfinished non-shell node has its child count
            // in m_size and m_children[0] is its shell node:
            if ((node.m_size == 1) || (lastFinished == node.m_children[1]))
            {
                node.finish();
                lastFinished = node;
            }
            else
            {
                queue.addFirst(node);

                for (int i = 1; i < node.m_size; ++i)
                {
                    final IObjectProfileNode child = node.m_children[i];

                    queue.addFirst(child);
                }
            }

            if (queue.isEmpty())
            {
                return;
            }
            else
            {
                node = (ObjectProfileNode) queue.removeFirst();
            }
        }
    }

    /*
     * A helper method for manipulating a class metadata cache.
     */
    private static ClassMetadata getClassMetadata(final Class cls,
            final Map /* Class->ClassMetadata */metadataMap,
            final ClassAccessPrivilegedAction caAction, final FieldAccessPrivilegedAction faAction)
    {
        if (cls == null)
        {
            return null;
        }

        ClassMetadata result;

        synchronized (metadataMap)
        {
            result = (ClassMetadata) metadataMap.get(cls);
        }

        if (result != null)
        {
            return result;
        }

        int primitiveFieldCount = 0;
        int shellSize = OBJECT_SHELL_SIZE; // java.lang.Object shell
        final List /* Field */refFields = new LinkedList();

        final Field[] declaredFields;

        try
        {
            caAction.setContext(cls);
            declaredFields = (Field[]) AccessController.doPrivileged(caAction);
        }
        catch (PrivilegedActionException pae)
        {
            throw new RuntimeException("could not access declared fields of class "
                    + cls.getName() + ": " + pae.getException());
        }

        for (int f = 0; f < declaredFields.length; ++f)
        {
            final Field field = declaredFields[f];

            if ((Modifier.STATIC & field.getModifiers()) != 0)
            {
                continue;
            }

            final Class fieldType = field.getType();

            if (fieldType.isPrimitive())
            {
                // memory alignment ignored:
                shellSize += sizeofPrimitiveType(fieldType);
                ++primitiveFieldCount;
            }
            else
            {
                // prepare for graph traversal later:
                if (!field.isAccessible())
                {
                    try
                    {
                        faAction.setContext(field);
                        AccessController.doPrivileged(faAction);
                    }
                    catch (PrivilegedActionException pae)
                    {
                        throw new RuntimeException("could not make field "
                                + field + " accessible: " + pae.getException());
                    }
                }

                // memory alignment ignored:
                shellSize += OBJREF_SIZE;
                refFields.add(field);
            }
        }

        // recurse into superclass:
        final ClassMetadata superMetadata = getClassMetadata(cls.getSuperclass(), metadataMap,
                caAction, faAction);

        if (superMetadata != null)
        {
            primitiveFieldCount += superMetadata.m_primitiveFieldCount;
            shellSize += (superMetadata.m_shellSize - OBJECT_SHELL_SIZE);
            refFields.addAll(Arrays.asList(superMetadata.m_refFields));
        }

        final Field[] _refFields = new Field[refFields.size()];

        refFields.toArray(_refFields);

        result = new ClassMetadata(primitiveFieldCount, shellSize, _refFields);

        synchronized (metadataMap)
        {
            metadataMap.put(cls, result);
        }

        return result;
    }

    /*
     * Computes the "shallow" size of an array instance.
     */
    private static int sizeofArrayShell(final int length, final Class componentType)
    {
        // this ignores memory alignment issues by design:
        final int slotSize = componentType.isPrimitive() ? sizeofPrimitiveType(componentType)
                : OBJREF_SIZE;

        return OBJECT_SHELL_SIZE + INT_FIELD_SIZE + OBJREF_SIZE + (length * slotSize);
    }

    /*
     * Returns the JVM-specific size of a primitive type.
     */
    private static int sizeofPrimitiveType(final Class type)
    {
        if (type == int.class)
        {
            return INT_FIELD_SIZE;
        }
        else if (type == long.class)
        {
            return LONG_FIELD_SIZE;
        }
        else if (type == short.class)
        {
            return SHORT_FIELD_SIZE;
        }
        else if (type == byte.class)
        {
            return BYTE_FIELD_SIZE;
        }
        else if (type == boolean.class)
        {
            return BOOLEAN_FIELD_SIZE;
        }
        else if (type == char.class)
        {
            return CHAR_FIELD_SIZE;
        }
        else if (type == double.class)
        {
            return DOUBLE_FIELD_SIZE;
        }
        else if (type == float.class)
        {
            return FLOAT_FIELD_SIZE;
        }
        else
        {
            throw new IllegalArgumentException("not primitive: " + type);
        }
    }

    // private: ...............................................................

    /*
     * Internal class used to cache class metadata information.
     */
    private static final class ClassMetadata
    {
        // all fields are inclusive of superclasses:
        final int m_primitiveFieldCount;

        final int m_shellSize; // class shell size

        final Field[] m_refFields; // cached non-static fields (made accessible)

        ClassMetadata(final int primitiveFieldCount, final int shellSize, final Field[] refFields)
        {
            m_primitiveFieldCount = primitiveFieldCount;
            m_shellSize = shellSize;
            m_refFields = refFields;
        }
    } // end of nested class

    private static final class ClassAccessPrivilegedAction implements PrivilegedExceptionAction
    {
        private Class m_cls;

        /**
         * @see java.security.PrivilegedExceptionAction#run()
         */
        public Object run() throws Exception
        {
            return m_cls.getDeclaredFields();
        }

        void setContext(final Class cls)
        {
            m_cls = cls;
        }
    } // end of nested class

    private static final class FieldAccessPrivilegedAction implements PrivilegedExceptionAction
    {
        private Field m_field;

        /**
         * @see java.security.PrivilegedExceptionAction#run()
         */
        public Object run() throws Exception
        {
            m_field.setAccessible(true);

            return null;
        }

        void setContext(final Field field)
        {
            m_field = field;
        }
    } // end of nested class
} // end of class
// ----------------------------------------------------------------------------
