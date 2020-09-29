package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;
import  java.awt.*;
import  java.awt.event.*;
import  java.awt.image.*;
import  java.util.*;
import  java.io.*;

public class Native extends BaseFunctions implements KeyListener, FocusListener, MouseListener, MouseMotionListener {

    Interpreter interp;
    VirtualMachine vm;

    Vector buffer = new Vector();
    BasicObject      keyThread;
    ActivationObject keyAr;
    int              keyResultOffset;

    IntHashtable fonts  = new IntHashtable();
    IntHashtable images = new IntHashtable();
    int          nextImageNumber = 0;
    Frame f;
    Panel p;
    Graphics gg;
    Image imgBuf;
    Graphics imgBuf_g;
    boolean offScreen = false;
    private static MediaTracker tracker;

    int screenWidth  = 300;
    int screenHeight = 300;

    private void setupGraphics() {
        trace(vm.tracegraphics(), "setupGraphics "+screenWidth+":"+screenHeight);

        f = new Frame();

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                trace(vm.tracegraphics(), "bye...");
                interp.exit(1);
            }
        });

        p = new Panel() {
            public void paint(Graphics g) {
                addEvent(0, 0);
            }
        };
        p.addKeyListener(this);
        p.addMouseListener(this);
        p.addMouseMotionListener(this);
        f.addFocusListener(this);

        f.setSize(screenWidth+8, screenHeight+27);

        f.add(p);

        f.setVisible(true);
        gg = p.getGraphics();

        tracker = new MediaTracker(p);
    }




    Graphics getGraphics() {
        if (gg == null) {
            setupGraphics();
        }
        // MIDP apps are double buffered, regular kawt apps are not
        if (offScreen && imgBuf == null) {
//System.out.println("get imgBuf");
            p.setBackground(Color.black);
            imgBuf = p.createImage(f.getWidth(), f.getHeight());
            imgBuf_g = imgBuf.getGraphics();
            imgBuf_g.setColor(Color.blue);
            imgBuf_g.fillRect(0, 0, f.getWidth(), f.getHeight());

        }

        if (offScreen) {
            return imgBuf_g;
        } else {
            return gg;
        }
    }

    void flushScreen() {
        if (offScreen && gg != null) {
            gg.drawImage(imgBuf, 0, 0, p);
        }
    }



   /**
    * Constructor
    */
    public Native(Interpreter interp, VirtualMachine vm) {
        this.interp = interp;
        this.vm     = vm;
    }


   /* ------------------------------------------------------------------------ *\
    *                                doNative                                  *
   \* ------------------------------------------------------------------------ */

    public void doNative(IntermediateMethod callee, Instruction[] parms, Invoke inst) {
        int  result = inst.getResultOffset();
        Type parent = callee.parent();

        if (callee.name().equals("prt") || callee.name().equals("prtn")) { // in java.lang.Object
            InstanceObject string = (InstanceObject)interp.oopAt(parms[1]);
            ArrayObject array = (ArrayObject)string.oopAt("value");
            int         count = string.intAt("count");
            int        offset = string.intAt("offset");
            for (int i = offset ; i < (offset+count) ; i++) {
                System.out.print((char)array.intAt(i));
            }
            if (callee.name().equals("prtn")) {
                System.out.println();
            }
            return;
        }

        if (callee.name().equals("putchar")) {              // in com.sun.cldc.io.j2me.debug
            int ch = interp.intAt(parms[1]);
            System.out.print((char)ch);
            return;
        }

        if (callee.name().equals("screenWidth")) {          // in de.kawt.impl.squawk.ToolkitImpl
            interp.intAtPut(result, screenWidth);
            return;
        }

        if (callee.name().equals("screenHeight")) {         // in de.kawt.impl.squawk.ToolkitImpl
            interp.intAtPut(result, screenHeight);
            return;
        }

        if (callee.name().equals("beep")) {                 // in de.kawt.impl.squawk.ToolkitImpl
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        if (callee.name().equals("setOffScreenMode")) {     // in de.kawt.impl.squawk.ToolkitImpl
            offScreen = true;
            return;
        }

        if (callee.name().equals("flushScreen")) {          // in de.kawt.impl.squawk.ToolkitImpl
            trace(vm.tracegraphics(), "setOnScreen");
            flushScreen();
            return;
        }




        if (callee.name().equals("createImage")) {          // in de.kawt.impl.squawk.ImageImpl
            trace(vm.tracegraphics(), "createImage");

            ArrayObject data = (ArrayObject)interp.oopAt(parms[1]);
            int       offset = interp.intAt(parms[2]);
            int       length = interp.intAt(parms[3]);

            byte[] buf = new byte[length];
            int[] in = (int[])data.data();

            for (int i = 0 ; i < length ; i++) {
                buf[i] = (byte)in[offset++];
            }

            getGraphics();
            Image img = Toolkit.getDefaultToolkit().createImage(buf);
            tracker.addImage(img, 0);
            try {tracker.waitForID(0);}
            catch (InterruptedException e) {e.printStackTrace();}


//getGraphics();
//Image img = p.createImage(10, 10);
            int res = nextImageNumber++;
            images.put(res, img);
            interp.intAtPut(result, res);
            return;
        }


        if (callee.name().equals("createMemoryImage")) {    // in de.kawt.impl.squawk.ImageImpl
            trace(vm.tracegraphics(), "createMemoryImage");


            int       hs     = interp.intAt(parms[1]);
            int       vs     = interp.intAt(parms[2]);
            ArrayObject data = (ArrayObject)interp.oopAt(parms[3]);
            int       stride = interp.intAt(parms[4]);
            int[] rgb = (int[])data.data();

            getGraphics();

            DirectColorModel colormodel = new DirectColorModel(24, 0x0000ff, 0x00ff00, 0xff0000);
            MemoryImageSource imageSource = new MemoryImageSource(hs, vs, colormodel, rgb, 0, stride );
            Image img = Toolkit.getDefaultToolkit().createImage(imageSource);

            int res = nextImageNumber++;
            images.put(res, img);
            interp.intAtPut(result, res);
            return;
        }



        if (callee.name().equals("getImage")) {          // in de.kawt.impl.squawk.ImageImpl
            InstanceObject string = (InstanceObject)interp.oopAt(parms[1]);
            String s = interp.getString(string);
            trace(vm.tracegraphics(), "getImage "+s);

            getGraphics();

            Image img = Toolkit.getDefaultToolkit().getImage(s);

            tracker.addImage(img, 0);
            try {tracker.waitForID(0);}
            catch (InterruptedException e) {e.printStackTrace();}

//System.out.println("res="+tracker.isErrorAny());
//Image img = p.createImage(10, 10);
            int res = nextImageNumber++;
            images.put(res, img);
            interp.intAtPut(result, res);
            return;
        }









        if (callee.name().equals("imageWidth")) {           // in de.kawt.impl.squawk.ImageImpl
            trace(vm.tracegraphics(), "imageWidth");
            int index = interp.intAt(parms[1]);
            Image img = (Image)images.get(index);
            interp.intAtPut(result, img.getWidth(null));
            return;
        }


        if (callee.name().equals("imageHeight")) {          // in de.kawt.impl.squawk.ImageImpl
            trace(vm.tracegraphics(), "imageHeight");
            int index = interp.intAt(parms[1]);
            Image img = (Image)images.get(index);
            interp.intAtPut(result, img.getHeight(null));
            return;
        }

        if (callee.name().equals("drawImage0")) {           // in de.kawt.impl.squawk.ImageImpl
            int index = interp.intAt(parms[1]);
            int     x = interp.intAt(parms[2]);
            int     y = interp.intAt(parms[3]);
            trace(vm.tracegraphics(), "drawImage0 "+index+" at "+x+":"+y );
            Image img = (Image)images.get(index);
            getGraphics().drawImage(img, x, y, null);
            return;
        }

        if (callee.name().equals("flush0")) {               // in de.kawt.impl.squawk.ImageImpl
            int index = interp.intAt(parms[1]);
            trace(vm.tracegraphics(), "flush0 "+index);
            Image img = (Image)images.get(index);
            img.flush();
            return;
        }

        if (callee.name().equals("createFontMetrics")) {    // in de.kawt.impl.squawk.FontMetricsImpl
            int size   = interp.intAt(parms[1]);
            int isBold = interp.intAt(parms[2]);
            int sizeBold = size << 16 + isBold;
            FontMetrics metrics = (FontMetrics)fonts.get(sizeBold);
            if (metrics == null) {
                metrics = Toolkit.getDefaultToolkit().getFontMetrics(new Font("TimesRoman", isBold==1 ? Font.BOLD : Font.PLAIN, size));
                fonts.put(sizeBold, metrics);
            }
            trace(vm.tracegraphics(), "createFontMetrics "+sizeBold+" = "+metrics.getFont());
            interp.intAtPut(result, sizeBold);
            return;
        }

        if (callee.name().equals("fontStringWidth")) {      // in de.kawt.impl.squawk.FontMetricsImpl
            int sizeBold          =                 interp.intAt(parms[1]);
            InstanceObject string = (InstanceObject)interp.oopAt(parms[2]);
            FontMetrics metrics = (FontMetrics)fonts.get(sizeBold);
            String s = interp.getString(string);
            trace(vm.tracegraphics(), "fontStringWidth "+sizeBold+ ":"+s+" = "+metrics.stringWidth(s));
            interp.intAtPut(result, metrics.stringWidth(s));
            return;
        }

        if (callee.name().equals("fontGetHeight")) {        // in de.kawt.impl.squawk.FontMetricsImpl
            int sizeBold = interp.intAt(parms[1]);
            FontMetrics metrics = (FontMetrics)fonts.get(sizeBold);
            trace(vm.tracegraphics(), "fontGetHeight "+sizeBold+" = "+metrics.getHeight());
            interp.intAtPut(result, metrics.getHeight());
            return;
        }

        if (callee.name().equals("fontGetAscent")) {        // in de.kawt.impl.squawk.FontMetricsImpl
            int sizeBold = interp.intAt(parms[1]);
            FontMetrics metrics = (FontMetrics)fonts.get(sizeBold);
            trace(vm.tracegraphics(), "fontGetHeight "+sizeBold+" = "+metrics.getAscent());
            interp.intAtPut(result, metrics.getAscent());
            return;
        }

        if (callee.name().equals("fontGetDescent")) {       // in de.kawt.impl.squawk.FontMetricsImpl
            int sizeBold = interp.intAt(parms[1]);
            FontMetrics metrics = (FontMetrics)fonts.get(sizeBold);
            trace(vm.tracegraphics(), "fontGetHeight "+sizeBold+" = "+metrics.getDescent());
            interp.intAtPut(result, metrics.getDescent());
            return;
        }



        if (callee.name().equals("setFont0")) {             // de.kawt.impl.squawk.GraphicsImpl
            int sizeBold = interp.intAt(parms[1]);
            FontMetrics metrics = (FontMetrics)fonts.get(sizeBold);
            trace(vm.tracegraphics(), "setFont0 "+metrics.getFont());
            getGraphics().setFont(metrics.getFont());
            return;
        }

        if (callee.name().equals("setColor0")) {            // de.kawt.impl.squawk.GraphicsImpl
            int c  = interp.intAt(parms[1]);
            trace(vm.tracegraphics(), "setColor0 "+c);
            getGraphics().setColor(new Color(c));
            return;
        }

        if (callee.name().equals("setClip0")) {             // de.kawt.impl.squawk.GraphicsImpl
            int x  = interp.intAt(parms[1]);
            int y  = interp.intAt(parms[2]);
            int w  = interp.intAt(parms[3]);
            int h  = interp.intAt(parms[4]);
            trace(vm.tracegraphics(), "setClip0 "+x+":"+y+":"+w+":"+h);
            getGraphics().setClip(x, y, w, h);
            return;
        }

        if (callee.name().equals("drawString0")) {          // de.kawt.impl.squawk.GraphicsImpl
            InstanceObject string = (InstanceObject)interp.oopAt(parms[1]);
            int x = interp.intAt(parms[2]);
            int y = interp.intAt(parms[3]);
            if (string != null) {
                String s = interp.getString(string);
                trace(vm.tracegraphics(), "drawString0 \""+s+"\" "+x+":"+y);
                getGraphics().drawString(s, x, y);
            }
            return;
        }

        if (callee.name().equals("drawLine0")) {            // de.kawt.impl.squawk.GraphicsImpl
            int x  = interp.intAt(parms[1]);
            int y  = interp.intAt(parms[2]);
            int w  = interp.intAt(parms[3]);
            int h  = interp.intAt(parms[4]);
            trace(vm.tracegraphics(), "drawLine0 "+x+":"+y+":"+w+":"+h);
            getGraphics().drawLine(x, y, w, h);
            return;
        }

        if (callee.name().equals("drawOval0")) {            // de.kawt.impl.squawk.GraphicsImpl
            int x  = interp.intAt(parms[1]);
            int y  = interp.intAt(parms[2]);
            int w  = interp.intAt(parms[3]);
            int h  = interp.intAt(parms[4]);
            trace(vm.tracegraphics(), "drawOval0 "+x+":"+y+":"+w+":"+h);
            getGraphics().drawOval(x, y, w, h);
            return;
        }

        if (callee.name().equals("drawRect0")) {            // de.kawt.impl.squawk.GraphicsImpl
            int x  = interp.intAt(parms[1]);
            int y  = interp.intAt(parms[2]);
            int w  = interp.intAt(parms[3]);
            int h  = interp.intAt(parms[4]);
            trace(vm.tracegraphics(), "drawRect0 "+x+":"+y+":"+w+":"+h);
            getGraphics().drawRect(x, y, w, h);
            return;
        }

        if (callee.name().equals("fillRect0")) {            // de.kawt.impl.squawk.GraphicsImpl
            int x  = interp.intAt(parms[1]);
            int y  = interp.intAt(parms[2]);
            int w  = interp.intAt(parms[3]);
            int h  = interp.intAt(parms[4]);
            trace(vm.tracegraphics(), "fillRect0 "+x+":"+y+":"+w+":"+h);
            getGraphics().fillRect(x, y, w, h);
            return;
        }

        if (callee.name().equals("drawRoundRect0")) {       // de.kawt.impl.squawk.GraphicsImpl
            int x  = interp.intAt(parms[1]);
            int y  = interp.intAt(parms[2]);
            int w  = interp.intAt(parms[3]);
            int h  = interp.intAt(parms[4]);
            int aw = interp.intAt(parms[5]);
            int ah = interp.intAt(parms[6]);
            trace(vm.tracegraphics(), "drawRoundRect0 "+x+":"+y+":"+w+":"+h+":"+aw+":"+ah);
            getGraphics().drawRoundRect(x, y, w, h, aw, ah);
            return;
        }

        if (callee.name().equals("fillRoundRect0")) {       // de.kawt.impl.squawk.GraphicsImpl
            int x  = interp.intAt(parms[1]);
            int y  = interp.intAt(parms[2]);
            int w  = interp.intAt(parms[3]);
            int h  = interp.intAt(parms[4]);
            int aw = interp.intAt(parms[5]);
            int ah = interp.intAt(parms[6]);
            trace(vm.tracegraphics(), "fillRoundRect0 "+x+":"+y+":"+w+":"+h+":"+aw+":"+ah);
            getGraphics().fillRoundRect(x, y, w, h, aw, ah);
            return;
        }

        if (callee.name().equals("fillArc0")) {             // de.kawt.impl.squawk.GraphicsImpl
            int x  = interp.intAt(parms[1]);
            int y  = interp.intAt(parms[2]);
            int w  = interp.intAt(parms[3]);
            int h  = interp.intAt(parms[4]);
            int ba = interp.intAt(parms[5]);
            int ea = interp.intAt(parms[6]);
            trace(vm.tracegraphics(), "fillArc0 "+x+":"+y+":"+w+":"+h+":"+ba+":"+ea);
            getGraphics().fillArc(x, y, w, h, ba, ea);
            return;
        }

        if (callee.name().equals("fillPolygon0")) {             // de.kawt.impl.squawk.GraphicsImpl

            ArrayObject x = (ArrayObject)interp.oopAt(parms[1]);
            ArrayObject y = (ArrayObject)interp.oopAt(parms[2]);
            int    count  = interp.intAt(parms[3]);

            int[] xx = (int[])x.data();
            int[] yy = (int[])y.data();

            trace(vm.tracegraphics(), "fillPolygon0 "+count);
            getGraphics().fillPolygon(xx, yy, count);
            return;
        }







        if (callee.name().equals("repaint0")) {             // de.kawt.impl.squawk.GraphicsImpl
            trace(vm.tracegraphics(), "repaint0");
            p.repaint();
            return;
        }






        if (callee.name().equals("getKeystroke")) {         // de.kawt.impl.squawk.EventDespatcher
            keyThread = interp.oopAt(parms[1]);
            keyResultOffset = result;
            keyAr           = interp.ar;
            interp.blockThread(inst);
            trace(vm.traceevents(), "getKeystroke waiting... ");
            return;
        }


        if (callee.name().equals("FileInputStream_open")) {
            InstanceObject string = (InstanceObject)interp.oopAt(parms[1]);
            try {
//prtn("---"+interp.getString(string));
                theInputStream = new FileInputStream(interp.getString(string));
                interp.intAtPut(result, 1);
            } catch(FileNotFoundException ex) {
//prtn("fnf");
                interp.intAtPut(result, 0);
            }
            return;
        }

        if (callee.name().equals("FileInputStream_read")) {
            try {
                int ch = theInputStream.read();
//prtn("---"+ch);
                interp.intAtPut(result, ch);
            } catch(IOException ex) {
                interp.intAtPut(result, -1);
            }
            return;
        }

        if (callee.name().equals("FileInputStream_close")) {
            try {
                theInputStream.close();
            } catch(IOException ex) {
            }
            return;
        }

        throw interp.fatal("Unlmplemented native method "+callee.name());
    }



    FileInputStream theInputStream;



    public void focusGained(FocusEvent e) {
        p.requestFocus();
        flushScreen();
    }

    public void focusLost(FocusEvent e) {
    }




    protected void event() {
        synchronized(this) {
            if (buffer.size() > 0 && keyThread != null) {
                Long firstKey = (Long)buffer.firstElement();
                buffer.removeElementAt(0);
                keyAr.longAtPut(keyResultOffset, firstKey.longValue());
                interp.restartThread(keyThread);
                keyAr = null;
                keyThread = null;
            }
        }
    }


    public void addEvent(long a, long b) {
        synchronized(this) {
            buffer.addElement(new Long(a<<32 | b));
            notify();
        }
    }

    public void keyPressed(KeyEvent e) {
        trace(vm.traceevents(), "keyPressed "+e.getKeyCode()+":"+e.getKeyChar());

        if (e.getKeyCode() >= 32 /*|| e.getKeyCode() == 0xA*/) {
            addEvent(1<<16 | e.getID(), e.getKeyCode() << 16 | e.getKeyChar());
        }
    }

    public void keyTyped(KeyEvent e) {
        trace(vm.traceevents(), "keyTyped "+e);
        if (e.getKeyChar() >= 32) {
            addEvent(1<<16 | e.getID(), e.getKeyCode() << 16 | e.getKeyChar());
        } else {
            addEvent(1<<16 | 401, e.getKeyChar() << 16 | e.getKeyChar());
        }
    }

    public void keyReleased(KeyEvent e) {
        trace(vm.traceevents(), "keyReleased "+e);
        addEvent(1<<16 | e.getID(), e.getKeyCode() << 16 | e.getKeyChar());
    }

    public void mousePressed (MouseEvent e) {
        trace(vm.traceevents(), "mousePressed "+e);
        addEvent(2<<16 | e.getID(), e.getX() << 16 | e.getY());
    }

    public void mouseReleased (MouseEvent e) {
        trace(vm.traceevents(), "mouseReleased "+e);
        addEvent(2<<16 | e.getID(), e.getX() << 16 | e.getY());
    }

    public void mouseClicked (MouseEvent e) {
        trace(vm.traceevents(), "mouseClicked "+e);
        addEvent(2<<16 | e.getID(), e.getX() << 16 | e.getY());
    }

    public void mouseEntered (MouseEvent e) {
    }

    public void mouseExited (MouseEvent e) {
    }

    public void mouseMoved (MouseEvent e) {
 //       trace(vm.traceevents(), "mouseMoved "+e);
 //       addEvent(2<<16 | e.getID(), e.getX() << 16 | e.getY());
    }

    public void mouseDragged (MouseEvent e) {
        trace(vm.traceevents(), "mouseDragged "+e);
        addEvent(2<<16 | e.getID(), e.getX() << 16 | e.getY());
    }

}
