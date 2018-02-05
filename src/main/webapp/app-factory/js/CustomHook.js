/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Ioannis Moutsatsos, Bruno P. Kinoshita
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
'use strict';
//jQuery.noConflict();

var CustomView = CustomView || (function($) {
    // The final public object

    var instance = {};

    // Hacks in Jenkins core
    /**
     * <p>This function is the same as makeStaplerProxy available in Jenkins core, but executes calls
     * <strong>synchronously</strong>. Since many parameters must be filled only after other parameters have been
     * updated, calling Jenkins methods assynchronously causes several unpredictable errors.</p>
     */
    /* public */ function makeStaplerProxy2(url, crumb, methods) {
        if (url.substring(url.length - 1) !== '/') url+='/';
        var proxy = {};
        var stringify;
        if (Object.toJSON) // needs to use Prototype.js if it's present. See commit comment for discussion
            stringify = Object.toJSON;  // from prototype
        else if (typeof(JSON)=="object" && JSON.stringify)
            stringify = JSON.stringify; // standard
        var genMethod = function(methodName) {
            proxy[methodName] = function() {
                var args = arguments;
                // the final argument can be a callback that receives the return value
                var callback = (function(){
                    if (args.length==0) return null;
                    var tail = args[args.length-1];
                    return (typeof(tail)=='function') ? tail : null;
                })();
                // 'arguments' is not an array so we convert it into an array
                var a = [];
                for (var i=0; i<args.length-(callback!=null?1:0); i++)
                    a.push(args[i]);
                 //Assume prototype should work
                    new Ajax.Request(url+methodName, {
                        method: 'post',
                        requestHeaders: {'Content-type':'application/x-stapler-method-invocation;charset=UTF-8','Crumb':crumb},
                        postBody: stringify(a),
                        onSuccess: function(t) {
                            if (callback!=null) {
                                t.responseObject = function() {
                                    return eval('('+this.responseText+')');
                                };
                                callback(t);
                            }
                        }
                    });

            }
        };
        for(var mi = 0; mi < methods.length; mi++) {
            genMethod(methods[mi]);
        }
        return proxy;
    }

    instance.makeStaplerProxy2 = makeStaplerProxy2;
    return instance;
})(jQuery);
