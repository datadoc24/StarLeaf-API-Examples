'use strict';

var crypto = require('crypto');
var request = require('request');
var binascii = require('binascii');



var username = 'email';
var password = 'pwd';
var api = 'https://api.starleaf.com/v1/';

request.get({
    url: api + 'challenge?username=' + encodeURIComponent(username),
    json: true }, (err, res, data) => {
    if (err) {
      console.log('Error:', err);
    } else if (res.statusCode !== 200) {
      console.log('Status:', res.statusCode);
    } else {
      // data is already parsed as JSON:
      console.log('Challenge (Hex) :' + data.challenge);
      console.log('Salt (Hex) :' + data.salt);
      console.log('Iterations :' + data.iterations);
      var challenge_bin = binascii.unhexlify(data.challenge);
      var salt_bin = binascii.unhexlify(data.salt);

      console.log('Challenge (Bin) :' + challenge_bin);
      console.log('Salt (Bin) :' + salt_bin);

      crypto.pbkdf2(password, salt_bin, data.iterations, 32, 'sha256',
        function(err, key) {
            if (err) throw err;

            console.log("Got a key of " + key);

            const hmac = crypto.createHmac('sha256', key );
            hmac.update(challenge_bin);
            var response = hmac.digest('hex');

            console.log("Got response of " + response);

        });

    }
});
