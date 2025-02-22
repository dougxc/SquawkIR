/*
 * Copyright 1994-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

package java.lang;

/**
 * Instances of the class <code>Class</code> represent classes and interfaces
 * in a running Java application.  Every array also belongs to a class that is
 * reflected as a <code>Class</code> object that is shared by all arrays with
 * the same element type and number of dimensions.
 *
 * <p> <code>Class</code> has no public constructor. Instead <code>Class</code>
 * objects are constructed automatically by the Java Virtual Machine as classes
 * are loaded.
 *
 * <p> The following example uses a <code>Class</code> object to print the
 * class name of an object:
 *
 * <p> <blockquote><pre>
 *     void printClassName(Object obj) {
 *         System.out.println("The class of " + obj +
 *                            " is " + obj.getClass().getName());
 *     }
 * </pre></blockquote>
 *
 * @author  unascribed
 * @version 1.106, 12/04/99 (CLDC 1.0, Spring 2000)
 * @since   JDK1.0
 */
public final class Class {

    /*
     * Constructor. Only the Java Virtual Machine creates Class
     * objects.
     */
    private Class() {}

    /**
     * Converts the object to a string. The string representation is the
     * string "class" or "interface", followed by a space, and then by the
     * fully qualified name of the class in the format returned by
     * <code>getName</code>.  If this <code>Class</code> object represents a
     * primitive type, this method returns the name of the primitive type.  If
     * this <code>Class</code> object represents void this method returns
     * "void".
     *
     * @return a string representation of this class object.
     */
    public String toString() {
        return (isInterface() ? "interface " :  "class ") + getName();
    }

    /**
     * Returns the <code>Class</code> object associated with the class
     * with the given string name.
     * Given the fully-qualified name for a class or interface, this
     * method attempts to locate, load and link the class.  If it
     * succeeds, returns the Class object representing the class.  If
     * it fails, the method throws a ClassNotFoundException.
     * <p>
     * For example, the following code fragment returns the runtime
     * <code>Class</code> descriptor for the class named
     * <code>java.lang.Thread</code>:
     * <ul><code>
     *   Class&nbsp;t&nbsp;= Class.forName("java.lang.Thread")
     * </code></ul>
     *
     * @param      className   the fully qualified name of the desired class.
     * @return     the <code>Class</code> descriptor for the class with the
     *             specified name.
     * @exception  ClassNotFoundException  if the class could not be found.
     * @since      JDK1.0
     */

    public static Class forName(String className) throws ClassNotFoundException {
        try {
            return forName0(className);
        } catch (Exception ex) {
            throw new ClassNotFoundException();
        }
    }
    private static native Class forName0(String className) throws ClassNotFoundException;

    /**
     * Creates a new instance of a class.
     *
     * @return     a newly allocated instance of the class represented by this
     *             object. This is done exactly as if by a <code>new</code>
     *             expression with an empty argument list.
     * @exception  IllegalAccessException  if the class or initializer is
     *               not accessible.
     * @exception  InstantiationException  if an application tries to
     *               instantiate an abstract class or an interface, or if the
     *               instantiation fails for some other reason.
     * @since     JDK1.0
     */
    public Object newInstance() throws InstantiationException, IllegalAccessException {
        if (isArray() || isInterface() || isAbstract()) {
            throw new InstantiationException();
        }
        if (!callerHasAccessToClass()) {
            throw new IllegalAccessException();
        }
        return newInstance0();
    }

    public  native Object newInstance0() throws InstantiationException, IllegalAccessException;
    private native boolean isAbstract();
    private native boolean callerHasAccessToClass();


    /**
     * Determines if the specified <code>Object</code> is assignment-compatible
     * with the object represented by this <code>Class</code>.  This method is
     * the dynamic equivalent of the Java language <code>instanceof</code>
     * operator. The method returns <code>true</code> if the specified
     * <code>Object</code> argument is non-null and can be cast to the
     * reference type represented by this <code>Class</code> object without
     * raising a <code>ClassCastException.</code> It returns <code>false</code>
     * otherwise.
     *
     * <p> Specifically, if this <code>Class</code> object represents a
     * declared class, this method returns <code>true</code> if the specified
     * <code>Object</code> argument is an instance of the represented class (or
     * of any of its subclasses); it returns <code>false</code> otherwise. If
     * this <code>Class</code> object represents an array class, this method
     * returns <code>true</code> if the specified <code>Object</code> argument
     * can be converted to an object of the array class by an identity
     * conversion or by a widening reference conversion; it returns
     * <code>false</code> otherwise. If this <code>Class</code> object
     * represents an interface, this method returns <code>true</code> if the
     * class or any superclass of the specified <code>Object</code> argument
     * implements this interface; it returns <code>false</code> otherwise. If
     * this <code>Class</code> object represents a primitive type, this method
     * returns <code>false</code>.
     *
     * @param   obj the object to check
     * @return  true if <code>obj</code> is an instance of this class
     *
     * @since JDK1.1
     */
    public boolean isInstance(Object obj) {
        return obj != null && obj.getClass().isAssignableTo(this);
    }

    /**
     * Determines if the class or interface represented by this
     * <code>Class</code> object is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the specified
     * <code>Class</code> parameter. It returns <code>true</code> if so;
     * otherwise it returns <code>false</code>. If this <code>Class</code>
     * object represents a primitive type, this method returns
     * <code>true</code> if the specified <code>Class</code> parameter is
     * exactly this <code>Class</code> object; otherwise it returns
     * <code>false</code>.
     *
     * <p> Specifically, this method tests whether the type represented by the
     * specified <code>Class</code> parameter can be converted to the type
     * represented by this <code>Class</code> object via an identity conversion
     * or via a widening reference conversion. See <em>The Java Language
     * Specification</em>, sections 5.1.1 and 5.1.4 , for details.
     *
     * @param cls the <code>Class</code> object to be checked
     * @return the <code>boolean</code> value indicating whether objects of the
     * type <code>cls</code> can be assigned to objects of this class
     * @exception NullPointerException if the specified Class parameter is
     *            null.
     * @since JDK1.1
     */
    public boolean isAssignableFrom(Class cls) {
        if (cls == null) {
            throw new NullPointerException();
        }
        boolean res = cls.isAssignableTo(this);
//System.out.println("isAssignableFrom "+this+" to "+cls+" = "+res);
        return res;
    }

    /**
     * Determines if the specified <code>Class</code> object represents an
     * interface type.
     *
     * @return  <code>true</code> if this object represents an interface;
     *          <code>false</code> otherwise.
     */
    public native boolean isInterface();

    /**
     * Determines if this <code>Class</code> object represents an array class.
     *
     * @return  <code>true</code> if this object represents an array class;
     *          <code>false</code> otherwise.
     * @since   JDK1.1
     */
    public native boolean isArray();

    /**
     * Returns the fully-qualified name of the entity (class, interface, array
     * class, primitive type, or void) represented by this <code>Class</code>
     * object, as a <code>String</code>.
     *
     * <p> If this <code>Class</code> object represents a class of arrays, then
     * the internal form of the name consists of the name of the element type
     * in Java signature format, preceded by one or more "<tt>[</tt>"
     * characters representing the depth of array nesting. Thus:
     *
     * <blockquote><pre>
     * (new Object[3]).getClass().getName()
     * </pre></blockquote>
     *
     * returns "<code>[Ljava.lang.Object;</code>" and:
     *
     * <blockquote><pre>
     * (new int[3][4][5][6][7][8][9]).getClass().getName()
     * </pre></blockquote>
     *
     * returns "<code>[[[[[[[I</code>". The encoding of element type names
     * is as follows:
     *
     * <blockquote><pre>
     * B            byte
     * C            char
     * D            double
     * F            float
     * I            int
     * J            long
     * L<i>classname;</i>  class or interface
     * S            short
     * Z            boolean
     * </pre></blockquote>
     *
     * The class or interface name <tt><i>classname</i></tt> is given in fully
     * qualified form as shown in the example above.
     *
     * @return  the fully qualified name of the class or interface
     *          represented by this object.
     */
    public native String getName();

    /**
     * Finds a resource with a given name.  This method returns null if no
     * resource with this name is found.  The rules for searching
     * resources associated with a given class are profile
     * specific.
     *
     * @param name  name of the desired resource
     * @return      a <code>java.io.InputStream</code> object.
     * @since JDK1.1
     */

    public java.io.InputStream getResourceAsStream(String name) {
        try {
            if (name.length() > 0 && name.charAt(0) == '/') {
                name = name.substring(1);
            } else {
                String className = this.getName();
                int dotIndex = className.lastIndexOf('.');
                if (dotIndex >= 0) {
                    name = className.substring(0, dotIndex + 1).replace('.', '/') + name;
                }
            }
            return javax.microedition.io.Connector.openInputStream("resource:"+name);
        } catch (java.io.IOException x) {
            return null;
        }
    }

    private native boolean isAssignableTo(Class aClass);


   /* ------------------------------------------------------------------------ *\
    *                            Class Initialization                          *
   \* ------------------------------------------------------------------------ */

    private final static int INITIALIZING    = 6; //
    private final static int INITIALIZED     = 7; // See com.sun.squawk.runtime.Type
    private final static int FAILED          = 8; //

   /**
    * The internal class initializion function. (See page 53 of the VM Spec.)
    */
    private static void initialize(Class clazz) throws Throwable {

       /*
        * Step 1
        */
        synchronized(clazz) {

           /*
            * Step 2
            */
            int state = clazz.getState();
            if (state == INITIALIZING) {
                if (getInitalizingThread(clazz) != Thread.currentThread()) {
                    do {
                        try {
                            clazz.wait();
                        } catch (InterruptedException e) {}
                    } while ((state = clazz.getState()) == INITIALIZING);
                } else {
                   /*
                    * Step 3
                    */
                    return;
                }
            }

           /*
            * Step 4
            */
            if (state == INITIALIZED) {
                return;
            }

           /*
            * Step 5
            */
            if (state == FAILED) {
                throw new NoClassDefFoundError();
            }

           /*
            * Step 6
            */
            setInitalizingThread(clazz, Thread.currentThread());
            clazz.setState(INITIALIZING);
        }

       /*
        * Step 7
        */
        if (!clazz.isInterface()) {
            Class superClass = clazz.getSuperclass();
            if (superClass != null && superClass.getState() != INITIALIZED) {
                try {
                    initialize(superClass);
                } catch(Throwable ex) {
                    synchronized(clazz) {
                        setInitalizingThread(clazz, null);
                        clazz.setState(FAILED);
                        clazz.notifyAll();
                    }
                    throw ex;
                }
            }
        }

       /*
        * Step 8
        */
        try {
            runClinit(clazz);

           /*
            * Step 9
            */
            synchronized(clazz) {
                setInitalizingThread(clazz, null);
                clazz.setState(INITIALIZED);
                clazz.notifyAll();
                return;
            }
        } catch(Throwable ex) {
           /*
            * Step 10
            */
            if (!(ex instanceof Error)) {
                try {
                    ex = new ExceptionInInitializerError(ex);
                } catch (OutOfMemoryError oom) {
                    ex = oom;
                }
            }

           /*
            * Step 11
            */
            synchronized(clazz) {
                setInitalizingThread(clazz, null);
                clazz.setState(FAILED);
                clazz.notifyAll();
            }
            throw ex;
        }
    }


    private native static Object getInitalizingThread(Class clazz);
    private native static void setInitalizingThread(Class clazz, Thread thread);

    public  native Class getSuperclass();
    private native int getState();
    private native void setState(int state);
    private native static void runClinit(Class clazz);

}
