var exec = require('cordova/exec');

function AntiTampering () {}

AntiTampering.prototype.verify = function (successCallback, errorCallback) {
    exec(successCallback,
        errorCallback,
        'CescitIntegrity',
        'verify',
        []);
};

AntiTampering.prototype.getList = function (successCallback, errorCallback) {
    exec(successCallback,
        errorCallback,
        'CescitIntegrity',
        'getList',
        []);
};


module.exports = new AntiTampering();
