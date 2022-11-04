package pl.rtprog.java2flow.samples;

import org.jetbrains.annotations.NotNull;

public class JavaBeanClass {
    @NotNull
    public String publicString;

    private String privateString;

    private String other;

    @NotNull
    public String getPrivateString() {
        return privateString;
    }

    public void setPrivateString(String privateString) {
        this.privateString = privateString;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
