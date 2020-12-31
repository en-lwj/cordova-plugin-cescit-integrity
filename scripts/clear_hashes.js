#!/usr/bin/env node

var helpers = require('./helpers');

module.exports = function (context) {
    var fs = require('fs');
    var fileList = ['AssetsIntegrity', 'ResIntegrity', 'ApkIntegrity'];
    process.stdout.write('[完整性检验] Clearing assets hash from previous build\n');

    helpers.getPlatformsList(context).forEach(function (platform) {
        fileList.forEach(fileName => {
            var source = helpers.getFileMapContent(context, platform, fileName);
            var content = source.content;

            let regexp = ''
            if(fileName == 'AssetsIntegrity') regexp = /hashList\s*=.+\s*new.*(\(\d+\)[^\w]*)\);/
            else if(fileName == 'ResIntegrity') regexp = /hashList\s*=.+\s*new.*(\(\d+\)[^\w]*)\);/
            else if(fileName == 'ApkIntegrity') regexp = /hashList\s*=.+\s*new.*(\(\d+\)[^\w]*)\);/
            content = source.content.replace(/\s*put\("[^"]+",\s"[^"]{64}"\);/g, '')
            .replace(regexp, function (match, group) {
                return match.replace(group, '()\n    ');
            });
            try {
                fs.writeFileSync(source.path, content, 'utf-8');
            } catch (e) {
                helpers.exit('Unable to write java class source at path ' + source.path, e);
            }
        })
    });
};
