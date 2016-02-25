
if (!window.console) {
    window.console = {
        log: function () {
            return;
        }
    };
}

function subEmoticons(text) {
    if (!window.emoticons) {
        console.log("missing emoticon object");
        return text;
    }
    return text.replace(/\[.{1,10}\]/g, function (match) {
        return typeof emoticons[match] != 'undefined' ?
                '<img src="' + emoticons[match] + '" />' :
                match;
    });
}

function buildEmoticonMessage(text) {
    if (!window.emoticons) {
        console.log("missing emoticon object");
        return text;
    }
    var ss = text.split(/\[[^\[\]]{1,30}\]/g);
    var $span = $('<span />');
    var ssIndex = 0;
    $('<span />').text(ss[ssIndex++]).appendTo($span);
    text.replace(/\[[^\[\]]{1,30}\]/g, function (match) {
        if (typeof emoticons[match] === 'undefined') {
            var $txtspan = $('<span />').text(match);
            $span.append($txtspan);
        } else {
            var html = '<img src="' + emoticons[match] + '" />';
            var $imgspan = $('<span />').html(html);
            $span.append($imgspan);
        }
        $('<span />').text(ss[ssIndex++]).appendTo($span);
        return match;
    });
    return $span;
}

function View($msgPanel, $userTbl, $msgInput, $emoSpan, $userCount) {

    this.$msgPanel = $msgPanel;
    this.$userTbl = $userTbl;
    this.$msgInput = $msgInput;
    this.$emoSpan = $emoSpan;
    this.$userCount = $userCount;
    this.roomName = null;
    this.userName = null;
    this.userStatus = null;
    this.userPrivs = null;


    this.init = function (userName, $roomXml) {
        console.log('init room', $roomXml);
        var v = this;
        this.userName = userName;
        this.roomName = $roomXml.find('name').first().text();
        console.log('room name', this.roomName);
        $roomXml.find('user').each(function (index) {
            var $this = $(this);
            var name = $this.find('name').text();
            var privs = $this.find('privs').text();
            if (privs.toLowerCase() == 'none') {
                privs = null;
            }
            var state = $this.find('state').text();
            if (name.toLowerCase() === userName.toLowerCase()) {
                v.userStatus = state;
                v.userPrivs = privs;
            }
            console.log(name, privs, state);
            v.displayAddUser(name, privs, state);
        });
    }

    this.displayMessage = function (src, msg, isWhisper, isOutgoing) {
        var spanClass = isOutgoing ? 'self' : 'other';
        var divClass = isWhisper ? 'whisper' : 'message';
        //var emoMsg = subEmoticons(msg);
        var $span = $('<span />').addClass(spanClass).text(src + ': ');
        //var $div = $('<div />').addClass(divClass).text(emoMsg).prepend($span);
        var $emospan = buildEmoticonMessage(msg);
        var $div = $('<div />').addClass(divClass).append($span).append($emospan);
        this.$msgPanel.append($div);
    }

    this.displayEvent = function (msg) {
        //alert(msg);
        $('<div />').addClass('event').text(msg).appendTo(this.$msgPanel);
    }

    this.displayError = function (msg) {
        var $span = $('<span />').text('Error: ');
        var $div = $('<div />').addClass('error').text(msg).prepend($span);
        this.$msgPanel.append($div);
    }

    this.displayInitUsers = function (userList) {
        var $prevtbl = this.$userTbl;
        this.$userTbl = $('<table />');
        for (var user in userList) {
            this.displayAddUser(user.name, user.privs, user.state);
        }
        $prevtbl.replaceWith(this.$userTbl);
    }

    this.displayAddUser = function (usr, privs, state) {
        var $tds = this.$userTbl.find('td.name');
        var $tr = null;
        $tds.each(function (index) {
            if ($(this).text().toLowerCase() === usr.toLowerCase()) {
                $tr = $(this).parent();
                return false;
            }
        });
        if ($tr !== null) {
            return;
        }
        $tr = $('<tr />').appendTo(this.$userTbl);
        this.displayUser($tr, usr, privs, state);
        var count = this.$userTbl.find('tr').length;
        this.$userCount.text(count);
    };

    this.displayModifyUser = function (userInfo) {
        var usr = userInfo['name'];
        var privs = userInfo['privs'];
        var state = userInfo['state'];
        var $tds = this.$userTbl.find('td.name');
        var v = this;
        $tds.each(function (index) {
            if ($(this).text().toLowerCase() === usr.toLowerCase()) {
                var $tr = $(this).parent();
                if (typeof state == 'undefined') {
                    state = $tr.attr('data-state');
                }
                if (typeof privs == 'undefined') {
                    privs = $tr.attr('data-privs');
                }
                v.displayUser($tr, usr, privs, state);
                return false;
            }
        });
    };

    this.displayUser = function ($trPrev, usr, privs, state) {
        var icon;
        if (privs && privs.toLowerCase() === 'owner') {
            icon = '(Owner)';
        } else if (privs && privs.toLowerCase() === 'token') {
            icon = '(Token)';
        } else {
            icon = '(User)';
        }
        /* $tr.find('td.name').text(usr);
         $tr.find('td.privs').text(icon);
         if (state) {
         $tr.find('td.state').text(state);
         } */
        var $td0 = $('<td />').text(icon).addClass('privs');
        var $td1 = $('<td />').text(usr).addClass('name');
        var $td2 = $('<td />').text(state).addClass('state');
        var $tr = $('<tr />');
        $tr.attr('data-name', usr).attr('data-privs', privs).attr('data-state', state);
        $tr.append($td0).append($td1).append($td2);
        $trPrev.replaceWith($tr);

    };

    this.displayRemoveUser = function (usr) {
        var $tds = this.$userTbl.find('td.name');
        $tds.each(function (index) {
            if ($(this).text().toLowerCase() === usr.toLowerCase()) {
                $(this).parents('tr').first().remove();
            }
        });
        var count = this.$userTbl.find('tr').length;
        this.$userCount.text(count);
    };

    this.disableInput = function () {
        this.$msgInput.attr('disabled', 'disabled');
        this.$emoSpan.attr('disabled', 'disabled');
    };

    this.enableInput = function () {
        this.$msgInput.removeAttr('disabled');
        this.$emoSpan.removeAttr('disabled');
    };

    this.update = function (cmd, src) {

        console.log('update view', cmd, src);
        if (cmd.room && cmd.room.toLowerCase() !== this.roomName.toLowerCase()) {
            if (cmd.command.toLowerCase() !== 'status') {
                return;
            }
        }
        if (typeof src === 'undefined' || src === null) {
            src = cmd.source;
        }
        var outgoing = (!src || src.toLowerCase() === this.userName.toLowerCase());
        switch (cmd.command) {
            case 'message':
                if (!outgoing) {
                    this.displayMessage(src, cmd.message, false, outgoing);
                }

                break;
            case 'whisper':
                if (!outgoing) {
                    this.displayMessage(src, cmd.message, true, outgoing);
                }

                break;
            case 'join':
                if (outgoing && cmd.$attachment) {
                    console.log('self join ' + cmd.room + ' event');
                    this.init(src, cmd.$attachment);
                    this.displayEvent('You have joined ' + cmd.room /*+ cmd.timestamp*/);
                    this.displayAddUser(src, null, cmd.args[0]);
                } else if (!outgoing) {
                    console.log(src + ' join ' + cmd.room + ' event');
                    this.displayEvent(src + ' has joined');
                    this.displayAddUser(src, null, cmd.args[0]);
                }
                break;
            case 'create':
                console.log('self create ' + cmd.room + ' event');
                this.init(src, cmd.$attachment);
                this.displayEvent('You have created ' + cmd.room);
                //this.displayAddUser(src, 'owner', cmd.args[0]);
                this.init(src, cmd.$attachment);
                // this.displayModifyUser(src, 'owner', this.userState);
                break;
            case 'leave':
                if (!outgoing) {
                    this.displayEvent(cmd.source + ' has left');
                    this.displayRemoveUser(src);
                }
                break;
            case 'kick':
                this.displayEvent(cmd.target + ' has been kicked by ' + cmd.source + ' because: ' + cmd.message);
                this.displayRemoveUser(cmd.target);
                break;
            case 'grant':
                this.displayEvent(cmd.target + ' has been given an ownership token');
                this.displayModifyUser({
                    name: cmd.target,
                    privs: 'token'});
                if (cmd.target.toLowerCase() === this.userName.toLowerCase()) {
                    this.userPrivs = 'token';
                }
                break;
            case 'revoke':
                this.displayEvent(cmd.target + ' has lost his ownership token');
                this.displayModifyUser({
                    name: cmd.target,
                    privs: null});
                if (cmd.target.toLowerCase() === this.userName.toLowerCase()) {
                    this.userPrivs = null;
                }
                break;
            case 'destroy':
                //alert(cmd.source + ' has closed the room because: ' + cmd.message);
                //this.close();
                break;
            case 'status':
                this.displayModifyUser({
                    name: cmd.source,
                    state: cmd.args[0]});
                break;
        }
    };

    this.updateError = function (req) {
        this.displayError(req.responseText);
    };

    this.close = function (user, msg, callback) {
        alert('todo:close()');
    }
} 