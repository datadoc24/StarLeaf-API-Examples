import requests
import json
import hashlib
import hmac
import pbkdf2
import binascii
headers = {'Content-type': 'application/json'}


class StarLeafClient(object):
    
    def __init__(self, username, password,  apiserver, sslverify=True):
        self.apiServer = apiserver
        self.username = username
        self.password = password
        self.sslVerify = sslverify
        self.key = None
        self.session = requests.Session()

    def _get(self, path, params=None):
        r = self.session.get(self.apiServer + path, params=params, verify=self.sslVerify)
        return r

    def _post(self, path, body):
        r = self.session.post(self.apiServer + path, data=json.dumps(body), headers=headers, verify=self.sslVerify)
        return r

    def _apiauthentication(self, salt_hex, iterations, challenge_hex):
        if self.key is None:
            salt = binascii.unhexlify(salt_hex)
            print ("Got a salt of: ")
            print ' '.join([str(ord(a)) for a in salt])
            self.key = pbkdf2.PBKDF2(passphrase=self.password, salt=salt, iterations=iterations,
                                     digestmodule=hashlib.sha256, macmodule=hmac).read(32)
                
        challenge = binascii.unhexlify(challenge_hex)
		
        print ("Got a key of: " + self.key)
        _hash = hmac.new(self.key, challenge, hashlib.sha256)
        response = _hash.hexdigest()
        return response

    def authenticate(self):
        params = {'username': self.username}
        response = self._get('/challenge', params=params)
        body = response.json()
        status = response.status_code
            
        if status == 200:
            authresponse = self._apiauthentication(body['salt'],
                                                   body['iterations'],
                                                   body['challenge'])
            postbody = {'username': self.username,
                        'response': authresponse}
            
            authresult = self._post('/authenticate', postbody)
            return authresult.status_code

        return status
        
    def listfeatures(self):
        print json.dumps(self._get('/features').json(), indent=4, sort_keys=True)
        return
    
username = "<your StarLeaf Portal login email address>"
password = "<your StarLeaf Portal password>"
api = "https://api.starleaf.com/v1"

    
myClient = StarLeafClient(username, password, api)
authStatus = myClient.authenticate()
print "Authentication result: " + str(authStatus)

if authStatus == 204:
    print "Logged in! Here are the features on your StarLeaf account."
    myClient.listfeatures()
else:
    print "Sorry. Check your username and password!"
