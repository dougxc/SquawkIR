package de.kawt;

import java.awt.*;
import de.kawt.impl.WrapperInterface;


/** This is an experimental class replacing the midlet launcher and
    may be subject to significant changes. For compatibility, please add a
    main method that creates a new instance of your KAWTlet implementation
    and then calls startApp for the new instance. */

public abstract class KAWTlet { 

    /** This variable is used to hand the current MIDP or applet
        wrapper over to the KAWTlet. Please do not access or
        manipulate this variable. */
        
    public static WrapperInterface currentWrapper;


    /** The MIDP or Applet wrapper for this KAWTlet. Copied from
        currentWrapper in the constructor. */

    private WrapperInterface wrapper;

    
    protected KAWTlet () {
       wrapper = currentWrapper;
       currentWrapper = null;
    }

    
    /** Invoked by the system when the application is started. If
	the application is paused, this method may be called more
	than once. One-time initialization code should be placed
        in the constructor. Here, the application should request 
        foreground execution by calling <tt>Frame.show ()</tt>. */

    public abstract void startApp ();
    

    /** Notify the system that this application should be terminated.
	Please call this method instead of <tt>System.exit()</tt>. */

    public void notifyDestroyed () {
        if (wrapper == null) 
            System.exit (0);
        else
            wrapper.notifyDestroyed ();
    }


    /** The system wants to destroy this application.
	Please release all system ressources when
	this method is called. */

    public abstract void destroyApp (boolean forced);
}
