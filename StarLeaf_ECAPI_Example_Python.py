import requests
import hashlib
import hmac
import binascii

headers = {'Content-type': 'application/json'}

'''
Tested in Python 2.7 and Python 3.4
You might need to pip install requests
'''
# local IP address of your StarLeaf room system
endpoint = "192.168.1.2"
# ECAPI password or key of your StarLeaf room system, as configured in the portal (for Cloud endpoints)
# or Web UI / Maestro (for GTm)
password = "mypassword"
testcall = "support@starleaf.com"

'''
Use this API for StarLeaf Cloud endpoints i.e GT Mini 3330 / GT 3351
This connects to port 23456 of http://<local IP address of endpoint>
'''
api = ":23456"

'''
Use this API for StarLeaf GTm (Skype for Business) endpoints 5250 and 5140
GTm uses port 80 for ECAPI requests
'''
# api = "/ecapi"



class StarLeafECAPIClient(object):
    
    def __init__(self, endpoint, password, api):
        self.endpoint = endpoint
        self.password = password
        self.api = api
        self.key = None
        self.authenticated = False
        self.session = requests.Session()

    def _get(self, path, params=None):
        r = self.session.get('http://' + self.endpoint + api + path, params=params)
        return r

    def _apiauthentication(self, salt_hex, iterations, challenge):
        if self.key is None:
            salt = binascii.unhexlify(salt_hex)
            self.key = hashlib.pbkdf2_hmac('sha256', str(self.password).encode(), salt, iterations)

        _hash = hmac.new(self.key, str(challenge).encode(), hashlib.sha256)
        response = _hash.hexdigest()
        return response

    def authenticate(self):
        response = self._get('/auth')
        body = response.json()
        status = response.status_code
            
        if status == 200:
            authresponse = self._apiauthentication(body['salt'],
                                                   body['iterations'],
                                                   body['challenge'])

            params = {'challenge': body['challenge'], 'response': authresponse}
            authresult = self._get('/auth', params=params)
            resultbody = authresult.json()

            if resultbody['authenticated'] is True:
                self.authenticated = True
                return True

        return False

    def call(self, address):
        params = {'action': 'dial', 'number': address}
        result = self._get('/action', params=params)
        return result.status_code

    
myClient = StarLeafECAPIClient(endpoint, password, api)
authStatus = myClient.authenticate()

if authStatus is True:
    # place a test call to StarLeaf Demo Line
    print("Authenticated successfully. Placing a test call to " + testcall)
    myClient.call(testcall)

else:
    print ("Sorry - authentication failed. Check the endpoint's ECAPI settings and password or key")
