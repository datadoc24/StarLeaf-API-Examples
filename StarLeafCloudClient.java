/**
 * Java example
 * download json-20160212.jar to your java_home libs/ext folder
 **/
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class StarLeafCloudClient {
    private String login = "<your StarLeaf account email address>";
    private String password = "<your StarLeaf portal password>";
    private String api = "https://api.starleaf.com/v1";
    private boolean loggedin = false;
    private SecretKey key;

    public StarLeafCloudClient() {
        String urlencLogin = new String();

        try {urlencLogin = URLEncoder.encode(login, "UTF-8");}
        catch(UnsupportedEncodingException uee){ uee.printStackTrace(); }

        String authurl = new String( api + "/challenge?username=" + urlencLogin );
        JSONObject obj = new JSONObject(getUrlContents(authurl));


        String iterations = String.valueOf(obj.get("iterations"));
        String challenge = (String) obj.get("challenge");
        String salt = (String) obj.get("salt");
        Mac mac;
        byte[] rawresponse = new byte[32];
        System.out.println("Got a challenge of " + challenge + " and a salt of " + salt);
        //generate the key and challenge hash
        try{
            key = getEncryptedPassword(password, hexStringToByteArray(salt), Integer.valueOf(iterations), 32)  ;
            mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            rawresponse = mac.doFinal(hexStringToByteArray(challenge));
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e){ e.printStackTrace(); }

        //put together the response JSON
        String response = bytesToHex(rawresponse);
        JSONObject responseObj = new JSONObject();
        responseObj.put("username", login);
        responseObj.put("response", response);
        System.out.println("Going to send a response of " + responseObj.toString());

        int code = postJsonResponse( new String(api + "/authenticate") , responseObj.toString() );

        if (code == 204){
            System.out.println( "Successfully authenticated!");
            loggedin = true;
        }
        else{
            System.out.println( "Authentication failed - sorry.");
        }
    }

    public void ListFeatures(){
        if (loggedin){
            System.out.println( "Your account has these features: " + getUrlContents( api + "/features"));
        }
        else{
            System.out.println( "You must be logged in to see your features.");
        }
    }

    public static void main(String[] args) {
        System.out.println("Logging into StarLeaf Cloud"); // Display the string.
        CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
        StarLeafCloudClient myClient = new StarLeafCloudClient();
        myClient.ListFeatures();
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

    private static int postJsonResponse(String theUrl, String theJson){
        try{
            URL responseUrl = new URL( theUrl );
            byte[] postDataBytes = theJson.getBytes("UTF-8");
            HttpURLConnection conn = (HttpURLConnection)responseUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            return conn.getResponseCode();
        }
        catch(Exception e){ e.printStackTrace();  }
        return 0;
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

    private static String bytesToHex(byte[] in) {
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
