'use strict';

var request = require('request');
var crypto = require('crypto');

var username = '<your portal account email address>';
var password = '<your portal password>';
var api = 'https://api.starleaf.com/v1/';

request.get({
    url: api + 'challenge?username=' + username,
    json: true }, (err, res, data) => {
    if (err) {
      console.log('Error:', err);
    } else if (res.statusCode !== 200) {
      console.log('Status:', res.statusCode);
    } else {
      
      var salt_binary = new Buffer(data.salt, 'hex');
      var challenge_binary = new Buffer(data.challenge, 'hex');
      
      crypto.pbkdf2(password, salt_binary, data.iterations, 32, 'sha256',
        function(err, key) {
            
            if (err) throw err;
            
            const hmac = crypto.createHmac('sha256', key );
            
            hmac.update(challenge_binary);
            
            var response = hmac.digest('hex');
            
            var authdata = {
                    "username" : username,
                    "response" : response
                    };
            
            request({url: api + 'authenticate',
                     method: "POST",
                     json: authdata}, function ( error , response , body ){
                         if( response.statusCode == 204){
                             console.log("Successfully authenticated! Status code " + response.statusCode);
                         } else{
                            console.log("Authentication failed, got a code of " + response.statusCode );
                         }
                     });
        });
    }
});