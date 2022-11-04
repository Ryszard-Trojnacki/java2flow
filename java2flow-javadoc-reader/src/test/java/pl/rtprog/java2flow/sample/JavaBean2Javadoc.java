package pl.rtprog.java2flow.sample;

public class JavaBean2Javadoc {
    public String publicString;

    private String privateString;

    /**
     * Getter for private field
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
