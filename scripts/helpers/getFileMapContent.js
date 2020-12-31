var helpers = require('./');

module.exports = function (platform, fileName) {
    var path = require('path');
    var fs = require('fs');
    var cordovaUtil = this.requireCordovaModule('cordova-lib/src/cordova/util');
    var projectRoot = cordovaUtil.isCordova();
    var platformPath = path.join(projectRoot, 'platforms', platform);
    var sourceFile;
    var content;

    if (platform === 'android') {
        var fileBasename = fileName;
        var filePath = 'com/cescit/integrity/' + fileBasename + '.java';
        try {
            sourceFile = path.join(platformPath, 'app/src/main/java', filePath);
            content = fs.readFileSync(sourceFile, 'utf-8');
        } catch (_e) {
            try {
                sourceFile = path.join(platformPath, 'src', filePath);
                content = fs.readFileSync(sourceFile, 'utf-8');
            } catch (e) {
                helpers.exit('Unable to read java class source at path ' + sourceFile, e);
            }
        }
    }

    return {
        content: content,
        path: sourceFile
    };
};
