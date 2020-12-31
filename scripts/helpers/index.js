/**
 * Exit script with custom error log.
 * @param {string} msg - Error message.
 * @param {Error} exception
 */
exports.exit = require('./error_exit');

/**
 * Get the real list of platforms affected by a running plugin hook.
 * @param {Object} context - Cordova context.
 */
exports.getPlatformsList = invokeHelper.bind(null, './get_platforms_list');

/**
 * Detect if the context process is running with verbose option.
 * @param {Object} context - Cordova context.
 */
exports.isVerbose = invokeHelper.bind(null, './is_verbose');

exports.getFileMapContent = invokeHelper.bind(null, './getFileMapContent');

function invokeHelper (path) {
    var helper = require(path);
    var context = arguments[1];
    return helper.apply(context, Array.prototype.splice.call(arguments, 2));
}
