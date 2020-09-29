
package de.kawt.impl.squawk;

import java.awt.*;

public class FontMetricsImpl extends FontMetrics {

    static FontMetricsImpl defaultFontMetrics = FontMetricsImpl.create(new Font("plain", Font.PLAIN, 8));

    int fontIndex;

    static FontMetricsImpl create(Font font) {
        return new FontMetricsImpl(font);
    }

    private FontMetricsImpl(Font font) {
        super(font);
        fontIndex = createFontMetrics(font.getSize(), font.isBold() ? 1 : 0);
//if (font.getSize() == 0) throw new RuntimeException();
    }

    public int stringWidth(String s) {
        return fontStringWidth(fontIndex, s);
    }

    public int getHeight() {
        return fontGetHeight(fontIndex);
    }

    public int getAscent() {
        return fontGetAscent(fontIndex);
    }

    public int getDescent() {
        return fontGetDescent(fontIndex);
    }

    private native int createFontMetrics(int size, int isBold);
    private native int fontStringWidth(int font, String string);
    private native int fontGetHeight(int font);
    private native int fontGetAscent(int font);
    private native int fontGetDescent(int font);

}
