'use strict';
app.factory('WsService', ['$rootScope', '$log', '$http',
    function($rootScope, $log, $http) {

        var _url = 'ws://localhost:9000/weatherws';

        var _ws;
        var _connected = false;

        return {

            isConnected: function() {
              return _ws !== null;
            },

            connect: function() {

                if (_ws === undefined) {
                    $log.info('Connecting to websocket at %s', _url);

                    _ws = new WebSocket(_url);

                    _ws.onopen = function() {
                        $log.info('Connected to websocket at %s', _url);
                    };

                    _ws.onmessage = function(message) {
                        $log.info('Receiving: %s', message.data);
                        var json = JSON.parse(message.data);
                        if (json === null) {
                            return;
                        }
                        if(json.status) {
                            $rootScope.$broadcast('endOfPositions', json);
                        } else {
                            $rootScope.$broadcast('newPosition', json);
                        }
                    };

                    _ws.onclose = function() {
                        $log.info('Websocket connection closed.');
                        _ws = null;
                    };
                }
            },

            send: function(msg) {
                _ws.send(angular.toJson(msg));
            }
        };
    }
]);