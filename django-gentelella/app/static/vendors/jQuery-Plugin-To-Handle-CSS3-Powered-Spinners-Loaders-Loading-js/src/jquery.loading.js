'use strict';
/*!
 * loading.css -https://github.com/Dn9x/loading.js
 * Version - 1.0.0
 * Licensed under the MIT license - http://opensource.org/licenses/MIT
 *
 * Copyright (c) 2016 Dn9x
 */
(function($) {

    var defaultWidth = 100;
    var _methods = {
        top: function(options) {
            var _loading = document.createElement("div");

            _loading.setAttribute('id', '_loading');
            _loading.style.position = "absolute";
            _loading.style.width = defaultWidth + "px";
            _loading.style.height = "0px";
            _loading.style.top = "50%";
            _loading.style.left = "50%";
            _loading.style['z-index'] = 99999;
            _loading.style['text-align'] = "center";

            for (var i in options) {
                if (i == 'width') {
                    _loading.style[i] = options[i] + 'px';
                    defaultWidth = options[i];
                }
                if (i == 'height') {
                    _loading.style[i] = options[i] + 'px';
                }
            }

            var clientWidth = document.body.clientWidth;
            var v = (clientWidth - defaultWidth) / 2;
            _loading.style.left = v + 'px';

            return _loading;
        }
    };

    _methods['line-pulse'] = function() {
        var _loading = _methods.top({
            "width": "100",
            "height": "20"
        });
        for (var i = 0; i < 4; i++) {
            var _cell = document.createElement("div");
            _loading.appendChild(_cell);
        }

        _loading.setAttribute('class', 'line-pulse');

        return _loading;
    };

    _methods['jump-pulse'] = function() {

        var _loading = _methods.top({
            "width": "100",
            "height": "20"
        });
        for (var i = 0; i < 4; i++) {
            var _cell = document.createElement("div");
            _loading.appendChild(_cell);
        }

        _loading.setAttribute('class', 'jump-pulse');

        return _loading;
    };

    _methods['circle-turn'] = function() {
        var _cell1 = document.createElement("div");

        var _loading = _methods.top({
            "width": "25",
            "height": "25"
        });
        _loading.appendChild(_cell1);
        _loading.setAttribute('class', 'circle-turn');

        return _loading;
    };

    _methods['circle-turn-scale'] = function() {
        var _cell1 = document.createElement("div");

        var _loading = _methods.top({
            "width": "25",
            "height": "25"
        });
        _loading.appendChild(_cell1);
        _loading.setAttribute('class', 'circle-turn-scale');

        return _loading;
    };

    _methods['circle-fade'] = function() {
        var _loading = _methods.top({
            "width": "50",
            "height": "72"
        });
        for (var i = 0; i < 8; i++) {
            var _cell = document.createElement("div");
            _loading.appendChild(_cell);
        }
        _loading.setAttribute('class', 'circle-fade');

        return _loading;
    };

    _methods['square-flip'] = function() {
        var _loading = _methods.top({
            "width": "50",
            "height": "0"
        });
        for (var i = 0; i < 1; i++) {
            var _cell = document.createElement("div");
            _loading.appendChild(_cell);
        }
        _loading.setAttribute('class', 'square-flip');

        return _loading;
    };

    _methods['line-scale'] = function() {
        var _loading = _methods.top({
            "width": "60",
            "height": "40"
        });
        for (var i = 0; i < 5; i++) {
            var _cell = document.createElement("div");
            _loading.appendChild(_cell);
        }
        _loading.setAttribute('class', 'line-scale');

        return _loading;
    };

    $.showLoading = function() {
        var defaultOptions = {
            name: 'line-pulse',
            maskClick: false,
            callback: function() {}
        };

        if (arguments) {
            if (typeof arguments[0] === 'string') {
                defaultOptions.name = arguments[0];
            } else if (typeof arguments[0] === 'object') {
                for (var i in arguments[0]) {
                    defaultOptions[i] = arguments[0][i];
                }
            }
        }

        $.hideLoading();

        var _mask = document.createElement("div");

        _mask.setAttribute('id', '_mask');
        _mask.style.position = "fixed";
        _mask.style.top = "0";
        _mask.style.left = "0";
        _mask.style.bottom = "0";
        _mask.style.right = "0";
        _mask.style.overflow = "hidden";
        _mask.style['z-index'] = 99998;
        _mask.style['background-color'] = '#000';
        _mask.style.opacity = 0.6;
        _mask.style.zoom = 1;

        if (defaultOptions.allowHide) {
            _mask.addEventListener('click', function() {
                $.hideLoading();
            }, false);
        }

        $('body').append(_mask);
        $('body').append(_methods[defaultOptions.name]());

        defaultOptions.callback();
    };
    $.hideLoading = function() {
        var _mask = $('#_mask');
        var _loading = $('#_loading');
        if (typeof _mask !== 'undefined' && _mask.length === 0 && typeof _loading !== 'undefined' && _loading.length === 0) {
            return;
        }
        _mask.remove();
        _loading.remove();
    };

    $(window).resize(function() {
        var _loading = $('#_loading');
        if (typeof _loading === 'object' && _loading.length != 0) {
            var clientWidth = document.body.clientWidth;
            var v = (clientWidth - defaultWidth) / 2;
            _loading.css("left", v + 'px');
        }
    });
}(jQuery || window.jQuery));