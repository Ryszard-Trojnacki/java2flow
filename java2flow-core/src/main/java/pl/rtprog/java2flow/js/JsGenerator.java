package pl.rtprog.java2flow.js;

/**
 * Helper class for generating JavaScript code.
 */
public class JsGenerator {
    private final StringBuilder sb;
    /**
     * Current ident for code/comments
     */
    private int ident=0;

    public JsGenerator(StringBuilder sb, int ident) {
        this.sb = sb;
        this.ident = ident;
    }

    public JsGenerator() {
        this.sb=new StringBuilder(1024);
    }

    public JsGenerator append(char[] str) {
        sb.append(str);
        return this;
    }

    public JsGenerator append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        return this;
    }

    public JsGenerator append(boolean b) {
        sb.append(b);
        return this;
    }

    public JsGenerator append(char c) {
        sb.append(c);
        return this;
    }

    public JsGenerator append(int i) {
        sb.append(i);
        return this;
    }

    public JsGenerator append(long lng) {
        sb.append(lng);
        return this;
    }

    public JsGenerator append(float f) {
        sb.append(f);
        return this;
    }

    public JsGenerator append(double d) {
        sb.append(d);
        return this;
    }

    public JsGenerator appendCodePoint(int codePoint) {
        sb.appendCodePoint(codePoint);
        return this;
    }

    public int length() {
        return sb.length();
    }

    public char charAt(int index) {
        return sb.charAt(index);
    }

    public int codePointAt(int index) {
        return sb.codePointAt(index);
    }

    public JsGenerator append(String str) {
        sb.append(str);
        return this;
    }

    public JsGenerator append(StringBuffer sb) {
        sb.append(sb);
        return this;
    }
}
