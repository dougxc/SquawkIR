

package de.kawt.impl.squawk;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.image.*;


public class ToolkitImpl extends Toolkit {

    static {
        new EventDispatcher().start();
    }

    public ToolkitImpl() {
    }

    public FontMetrics getFontMetrics(Font font) {
        return FontMetricsImpl.create(font);
    }

    public Dimension getScreenSize() {
        return new Dimension(screenWidth(), screenHeight());
    }

    public Image createImage(byte [] data, int offset, int length) {
        return new de.kawt.impl.squawk.ImageImpl(data, offset, length);
    }

    //public void sync() {
    //    if (GraphicsImpl.dirty) {
    //        repaint();
    //    }
    //}

    native static int screenWidth();
    native static int screenHeight();
    public native void beep();

    public native void flushScreen();

    public Image createImage (ImageProducer producer) {
        return new ImageImpl((MemoryImageSource )producer);
    }


    public Image getImage (String ressourceName) {
        return new ImageImpl(ressourceName);
    }
}



