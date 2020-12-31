module.exports = function (msg, exception) {
    process.stdout.write('\n[完整性检验] ERROR! ' + msg + '\n');
    throw new Error(exception);
};
