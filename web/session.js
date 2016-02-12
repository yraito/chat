/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function Session(username) {

    this.username = username;
    this.roomViews = new Object();
    this.client = new Client('command', 'stream');
    this.roomHandler = function (name, page) {
        alert('default room open handler. ' + name + ': ' + page);
    };
    this.roomCloseHandler = function (name) {
        alert('default room close handler. ' + ': ' + name);
    };
    this.listHandler = function (page) {
        alert('default room list handler. ' + ': ' + page);
    };

    this.addRoomView = function (room, view) {
        this.roomViews[room]["innerView"] = view;
    };
    this.removeRoomView = function (room) {
        delete this.roomViews[room];
    };
    this.listRooms = function () {
        var handler = this.listHandler;
        $.ajax({
            url: 'rooms',
            type: 'GET'
        }).done(function (page) {
            handler(page);
        }).fail(function (req) {
            alert(req.responseText);
        });
    };
    this.createRoom = function (name, password) {
        this.openRoom(name, password, this.roomHandler);
    };
    this.joinRoom = function (name, password) {
        this.openRoom(name, password, this.roomHandler);
    };
    this.openRoom = function (name, password, handler) {
        var s = this;
        this.roomViews[name] = new BufferedView();
        var params = {
            room: name,
            password: password
        };
        $.ajax({
            url: 'chat',
            type: 'POST',
            data: params
        }).fail(function (req) {
            alert('Could not open room ' + name + ': ' + req.responseText);
            s.closeRoom(name);
        }).done(function (body) {
            handler(name, body);
        });
    };
    this.closeRoom = function (name) {
        this.removeRoomView(name);
        this.roomCloseHandler(name);
    };
    this.dispatch = function (msg) {
        for (var room in this.roomViews) {
            var view = this.roomViews[room];
            view.update(msg.source, msg);
        }
    };
    this.start = function (s) {
        this.client.startread(function (msg) {
            s.dispatch(msg);
        },
                null);
    };
}

function BufferedView() {

    this.innerView = null;
    this.buffer = [];
    this.update = function (cmd, src) {
        if (this.innerView === null) {
            this.buffer.push({
                command: cmd,
                source: src
            });
        } else if (this.buffer.length > 0) {
            this.buffer.reverse();
            while (this.buffer.length > 0) {
                var obj = this.buffer.pop();
                this.innerView.update(obj.command, obj.source);
            }
            if (cmd !== null) {
                this.innerView.update(cmd, src);
            }
        } else if (cmd !== null) {
            this.innerView.update(cmd, src);
        }
    };
}
;


function getChatSession(user) {
    if (!window.chatSession) {
        window.chatSession = new Session(user);
        window.chatSession.start();
    }
    return window.chatSession;
}