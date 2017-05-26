/**
 * Placeholder for Java version of StarLeaf Cloud API Authentication example
 */
public class StarLeafCloudClient {
    private String login;
    private String password;
    private String api = 'https://api.starleaf.com/v1';

    public StarLeafCloudClient(String login, String password) {
        System.out.println("Logging in as " + login);
    }
}

public class DemoProgram{
    public static void main(String[] args) {
        System.out.println("Creating a demo StarLeaf Cloud Client"); // Display the string.
        StarLeafCloudClient = new StarLeafCloudClient('as','aspwd');
    }
}
