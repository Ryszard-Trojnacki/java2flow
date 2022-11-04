package pl.rtprog.java2flow.sample;

public class JavaBeanJavadoc {
    public String publicString;

    /**
     * Private field javadoc
     */
    private String privateString;

    /**
     * Getter for private field
     * @return
     */
    public String getPrivateString() {
        return privateString;
    }

    /**
     * Setter for private field
     * @param privateString
     */
    public void setPrivateString(String privateString) {
        this.privateString = privateString;
    }
}
