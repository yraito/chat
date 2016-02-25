/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
if (!window.console) {
    window.console = {
        log: function () {
            return;
        }
    };
}

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
        this.roomViews[room].inner(view);
    };
    this.removeRoomView = function (room) {
        delete this.roomViews[room];
    };
    this.getRoomView = function(room) {
        return this.roomViews[room];
    };
    this.listRooms = function (f) {
        var handler = f ? f : this.listHandler;
        var t = new Date().getTime();
        var jqxhr = $.ajax({
            url: 'rooms',
            type: 'GET',
            data: {
                t : t
            }
        }).done(function (page) {
            handler(page);
        }).fail(function (req) {
            alert(req.responseText);
        });
        return jqxhr;
    };
    this.createRoom = function (name, password) {
        var cmd = new Command('create').in(name).withargs(password);
        this.openRoom(name, password, cmd, this.roomHandler);
    };
    this.joinRoom = function (name, password) {
        var cmd = new Command('join').in(name).withargs(password);
        this.openRoom(name, password, cmd, this.roomHandler);
    };
    this.openRoom = function (name, password, command, handler) {
        var s = this;
        this.roomViews[name] = new BufferedView(this, name);
        var params = {
            room: name,
            password: password,
            t: new Date().getTime()
        };
        var getpage = function () {
            $.ajax({
                url: 'chat',
                type: 'POST',
                data: params
            }).fail(function (req) {
                alert('Could not open page for room ' + name + ': ' + req.responseText);
                s.closeRoom(name);
            }).done(function (body) {
                handler(name, body);
            });
        };
        this.client.send(command).
                done(function (body) {
                    //view.init(user, $(body));
                    getpage();
                }).
                fail(function (err) {
                    alert('Error joining/creating room ' + name + ': ' + err);
                    s.closeRoom(name);
                });


    };
    this.closeRoom = function (name) {
        this.removeRoomView(name);
        this.roomCloseHandler(name);
    };
    this.dispatch = function (msg) {
        
        for (var room in this.roomViews) {
            //alert(room);
            var view = this.roomViews[room];
            view.update(msg, msg.source);
        }
    };
    this.start = function () {
        var s = this;
        this.client.startread(function (msg) {
            s.dispatch(msg);
        },
                null);
    };
}

function BufferedView(sess, roomName) {
    this.sess = sess;
    this.roomName = roomName;
    this.innerView = null;
    this.buffer = [];
    this.update = function (cmd, src) {
        //alert('update');
        this.buffer.push({
            command: cmd,
            source: src
        });
        if (this.innerView !== null) {
            this.flush(this.innerView);
        } 
        this.updateSession(cmd, src);
    };

    this.updateSession = function (cmd, src) {
        if (cmd.room && cmd.room.toLowerCase() !== this.roomName.toLowerCase()) {
            if (cmd.command.toLowerCase() !== 'status') {
                return;
            }
        }
        if (typeof src === 'undefined' || src === null) {
            src = cmd.source;
        }
        var outgoing = (!src || src.toLowerCase() === sess.username.toLowerCase());
        switch (cmd.command) {

            case 'leave':
                if (outgoing) {
                    sess.closeRoom(roomName);
                }
                break;
            case 'kick':
                alert (this.sess.username.toLowerCase() + ': ' +  cmd.target.toLowerCase());
                if (this.sess.username.toLowerCase() == cmd.target.toLowerCase()) {
                    alert('You have been kicked from ' + cmd.room + ' because ' + cmd.message);
                    sess.closeRoom(roomName);
                }
                break;

            case 'destroy':
                alert(cmd.source + ' has closed the room because: ' + cmd.message);
                sess.closeRoom(roomName);
                break;
        }
    };
    this.updateError = function (req) {
        console.log(req);
    };

    this.inner = function (innerView) {
        this.innerView = innerView;
        this.flush(this.innerView);
    };

    this.flush = function (v) {
        if (this.buffer.length > 0) {
            //alert('flush');
            this.buffer.reverse();
            while (this.buffer.length > 0) {
                //alert('flush pop');
                var obj = this.buffer.pop();
                v.update(obj.command, obj.source);
            }
        }
    };
}

function getChatSession(user) {
    if (!window.chatSession) {
        window.chatSession = new Session(user);
        window.chatSession.start();
    }
    return window.chatSession;
}