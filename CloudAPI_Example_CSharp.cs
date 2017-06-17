// StarLeaf Cloud - API Challenge/Response Authentication example
using System;
using System.Net;
using System.Security.Cryptography;
using System.Web;
using Newtonsoft.Json;
using Microsoft.AspNetCore.Cryptography.KeyDerivation;

namespace StarLeafCloud
{
    class Authenticate
    {
        public class CookieWebClient : System.Net.WebClient
        {
            public CookieContainer CookieContainer { get; private set; }

            public CookieWebClient()
            {
                this.CookieContainer = new CookieContainer();
            }

            public CookieWebClient(CookieContainer cookieContainer)
            {
                this.CookieContainer = cookieContainer;
            }

            protected override WebRequest GetWebRequest(Uri address)
            {
                var request = base.GetWebRequest(address) as HttpWebRequest;
                if (request == null) return base.GetWebRequest(address);
                request.CookieContainer = CookieContainer;
                return request;
            }
        }

        static byte[] Hex2Binary(string hexvalue)
        {
            int arrayLength = hexvalue.Length / 2;
            
            byte[] bytevals = new byte[arrayLength];
            for (int i = 0; i < arrayLength; i++)
            {
                string byteString = hexvalue.Substring(i*2, 2);
                byte b = Convert.ToByte(byteString, 16);
                bytevals[i] = b;
            }
            return bytevals;
        }

        static void Main()
        {
            string api = "https://api.starleaf.com/v1";
            string username = "<your StarLeaf Portal login email address>";
            string password = "<your StarLeaf Portal password>";

            var webClient = new CookieWebClient();

            Console.WriteLine("Getting challenge URL");

            var json = webClient.DownloadString(api + "/challenge?username=" + HttpUtility.UrlEncode(username));

            // Now parse with JSON.Net
            dynamic challengevars = JsonConvert.DeserializeObject(json);

            Console.WriteLine("Challenge: " + challengevars.challenge);
            Console.WriteLine("Salt: " + challengevars.salt);
            Console.WriteLine("Iterations: " + challengevars.iterations);

            //convert challenge and salt back into binary
            byte[] bin_salt = Hex2Binary(challengevars.salt.ToString());
            byte[] bin_challenge = Hex2Binary(challengevars.challenge.ToString());
            
            //derive key
            byte[] bin_key = KeyDerivation.Pbkdf2(
                password: password,
                salt: bin_salt,
                prf: KeyDerivationPrf.HMACSHA256,
                iterationCount: 10000,
                numBytesRequested: 32
            );
            
            //generate hash of challenge using key
            var hmac = new HMACSHA256(bin_key);
            byte[] bin_response = hmac.ComputeHash(bin_challenge);

            string hex_response = BitConverter.ToString(bin_response).Replace("-", string.Empty).ToLower();
            string json_response = "{\"username\" : \"" + username + "\",\"response\" : \"" + hex_response + "\"}";
            Console.WriteLine("Json response: \r\n" + json_response);
            
            webClient.Headers.Add(HttpRequestHeader.ContentType, "application/json");
            //will raise an exception if response status is not 2XX
            webClient.UploadString(new Uri(api + "/authenticate"), "POST", json_response);
            
            string features_list = webClient.DownloadString(new Uri(api + "/features"));
            Console.WriteLine("Got a features list of " + features_list);
            
            // Keep the console window open in debug mode.
            Console.WriteLine("Press any key to exit.");
            Console.ReadKey();
        }
    }
}
