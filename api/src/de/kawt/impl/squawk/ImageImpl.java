
package de.kawt.impl.squawk;

import java.awt.*;
import java.awt.image.*;

public class ImageImpl extends Image {

    int imageIndex;

    ImageImpl(MemoryImageSource memoryImage) {
        imageIndex = createMemoryImage(
                                        memoryImage.hs,
                                        memoryImage.vs,
                                        memoryImage.rgb,
                                        memoryImage.stride
                                      );
    }

    ImageImpl(byte[] data, int offset, int length) {
        imageIndex = createImage(data, offset, length);
    }

    ImageImpl(String ressourceName) {
        imageIndex = getImage(ressourceName);
    }

    public int getWidth (ImageObserver o) {
        return imageWidth(imageIndex);
    }

    public int getHeight (ImageObserver o) {
        return imageHeight(imageIndex);
    }

    public void flush () {
        flush0(imageIndex);
    }

    private native int getImage(String ressourceName);
    private native int createImage(byte[] data, int offset, int length);
    private native int createMemoryImage(int hs, int vs, int[]rgb, int stride);

    private native int imageWidth(int number);
    private native int imageHeight(int number);
    private native void flush0(int number);

}

