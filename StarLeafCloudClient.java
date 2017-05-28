/**
 * Java version
 * Still a work in progress, calculates the response correctly but posting it to /authenticate is still TBD
 * download json-simple.jar to your java_home libs folder
 */
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StarLeafCloudClient {
    private String login;
    private String password;
    private String api = "https://api.starleaf.com/v1";

    public StarLeafCloudClient(String userlogin, String userpassword) {
        password = userpassword;
        try{
            login = URLEncoder.encode(userlogin, "UTF-8");
        }
        catch(Exception e){ e.printStackTrace(); }
        
        System.out.println("Logging in as " + login);
        String authurl = new String( api + "/challenge?username=" + login );
        JSONParser parser = new JSONParser();
        
        try {
            JSONObject obj = (JSONObject) parser.parse(getUrlContents(authurl)); 
            String iterations = String.valueOf(obj.get("iterations"));
            String challenge = (String) obj.get("challenge");
            String salt = (String) obj.get("salt");
            
            System.out.println( "Got salt of " + salt + " and challenge of " + challenge + " and " + iterations + " iterations." );
            
            SecretKey key = getEncryptedPassword(password, hexStringToByteArray(salt), Integer.valueOf(iterations), 32)  ;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] rawresponse = mac.doFinal(hexStringToByteArray(challenge));
            String response = bytesToHex(rawresponse);
            System.out.println("Created a response of " + response);
            
            // Post username and response back to server
            JSONObject responseObj = new JSONObject();
            responseObj.put("username", (String) userlogin);
            responseObj.put("response", (String) response);
            
            System.out.println(responseObj);
            
        }
        catch(Exception e){ e.printStackTrace(); }
        
    }
    
     public static void main(String[] args) {
        System.out.println("Creating a demo StarLeaf Cloud Client"); // Display the string.
        StarLeafCloudClient myClient = new StarLeafCloudClient("as+mr@hjhkjh.com","ghuigjkgjhgjh");
    }
    
    private static String getUrlContents(String theUrl)
    {
        StringBuilder content = new StringBuilder();
        try
        {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e){ e.printStackTrace(); }
        return content.toString();
    }
    
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
    
    private static SecretKey getEncryptedPassword(String password, byte[] salt,  int iterations,  int derivedKeyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength * 8);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return f.generateSecret(spec);
    }
}