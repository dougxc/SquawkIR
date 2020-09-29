package bench.delta;


import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


public class MidBench extends MIDlet {

    private Display display;    // The display for this MIDlet

    public MidBench() {
        System.out.println("MidBench");
        display = Display.getDisplay(this);
    }

    /**
     * Start up the Hello MIDlet by creating the TextBox and associating
     * the exit command and listener.
     */
    public void startApp() {
        System.out.println("MidBench::startApp");
        TextBox t = new TextBox("MidBench MIDlet", "Test string", 256, 0);

        display.setCurrent(t);

        runit();

        destroyApp(false);
        notifyDestroyed();
    }

    /**
     * Pause is a no-op since there are no background activities or
     * record stores that need to be closed.
     */
    public void pauseApp() {
        System.out.println("MidBench::pauseApp");
    }

    /**
     * Destroy must cleanup everything not handled by the garbage collector.
     * In this case there is nothing to cleanup.
     */
    public void destroyApp(boolean unconditional) {
        System.out.println("MidBench::destroyApp");
    }


    public void runit() {
        System.out.println("MidBench::runit++");
        new Main().main(null);
        System.out.println("MidBench::runit--");
    }

}





