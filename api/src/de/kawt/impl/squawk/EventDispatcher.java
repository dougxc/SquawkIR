
package de.kawt.impl.squawk;
import  java.awt.*;
import  java.awt.event.*;

public class EventDispatcher extends Thread {

    private static void post(AWTEvent e) {
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
    }

    public void run() {
        while(true) {
            long key = getKeystroke(Thread.currentThread());

            int key1 = (int)(key >> 32);
            int key2 = (int)(key);

            int key1_H = (key1 >> 16) & 0xFFFF;
            int key1_L =  key1 & 0xFFFF;
            int key2_H = (key2 >> 16) & 0xFFFF;
            int key2_L =  key2 & 0xFFFF;

//System.out.println("Got event "+key1_H+":"+key1_L+":"+key2_H+":"+key2_L);

            if (key1_H == 0) {
                if(Toolkit.getTopWindow() != null) {
                    Toolkit.getTopWindow().repaint();
                }
            } else if (key1_H == 1) {
                post(new KeyEvent(null, key1_L, 0, 0, key2_H, (char)key2_L));
            } else if (key1_H == 2) {
                post(new MouseEvent (null, key1_L, 0, 0, key2_H, key2_L, 0, false));
            } else {
                System.out.println("Bad event "+key1_H+":"+key1_L+":"+key2_H+":"+key2_L);
            }
        }
    }

   private static native long getKeystroke(Thread t);

}