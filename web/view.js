
var emoticons = {
    '[smile]' : 'png/smile.png',
    '[laugh]'  : 'png/laugh.png',
    '[cry]'  : 'png/cry.png'
 };

function subEmoticons(text) {

  return text.replace(/\[.{1,10}\]/g, function (match) {
    return typeof emoticons[match] != 'undefined' ?
           '<img src="'+ emoticons[match]+'" />' :
           match;
  });
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

	this.init = function(userName, $roomXml) {
                alert('init' + $roomXml);
		this.userName = userName;
		this.roomName = $roomXml.find('name').first().text();
                alert('init' + this.roomName);
		$roomXml.find('user').each(function(index) {
			var $this = $(this);
			var name = $this.find('name').text();
                        alert(name);
			var privs = $this.find('privs').text();
			if (privs.toLowerCase() == 'none') {
				privs = null;
			}
			var state = $this.find('state').text();
			if (name.toLowerCase() == userName) {
				userStatus = state;
				userPrivs = privs;
			}
			displayAddUser(name, privs, state);
		});

	}

	this.displayMessage = function(src, msg, isWhisper, isOutgoing) {
		var spanClass = isOutgoing ? 'self' : 'other';
		var divClass = isWhisper ? 'whisper' : 'message';
		var emoMsg = subEmoticons(msg);
		var $span = $('<span />').addClass(spanClass).text(src + ': ');
		var $div = $('<div />').addClass(divClass).text(emoMsg).prepend($span);
		this.$msgPanel.append($div);
	}

	this.displayEvent = function(msg) {
		$('<div />').addClass('event').text(msg).appendTo(this.$msgPanel);
	}

	this.displayError = function(msg) {
		var $span = $('<span />').text('Error: ');
		var $div = $('<div />').addClass('error').text(msg).prepend($span);
		this.$msgPanel.append($div);
	}

	this.displayInitUsers = function(userList) {
		var $prevtbl = this.$userTbl;
		this.$userTbl = $('<table />');
		for (var user in userList) {
			this.displayAddUser(user.name, user.privs, user.state);
		}
		$prevtbl.replaceWith(this.$userTbl);
	}

	this.displayAddUser = function(usr, privs, state) {
		var icon;
		if (privs === 'owner') {
			icon = '@';
		} else if (privs === 'token') {
			icon = '#';
		} else {
			icon = ' ';
		}
		var $td0 = $('<td />').text(icon).addClass('privs');
		var $td1 = $('<td />').text(usr).addClass('name');
		var $td2 = $('<td />').text(state).addClass('state');
		var $tr = $('<tr />').addClass(privs).addClass(state);
		$tr.append($td0).append($td1).append($td2).appendTo(this.$userTbl);
		var count = this.$userTbl.find('tr').length;
		this.$userCount.text(count);
	};

	this.displayModifyUser = function(usr, privs, state) {
		var $tds = this.$userTbl.find('td');
		$tds.each( function(index) {
			var $thistd = $(this);
			if ($thistd.text() == usr) {
				var $tr = $thistd.parent();
				var icon;
				if (privs === 'owner') {
					icon = '@';
				} else if (privs === 'token') {
					icon = '#';
				} else {
					icon = ' ';
				}
				$tr.find('td.privs').text(icon);
				if (state != null) {
					$tr.find('td.state').text(state);
				}
			}
		});
	};

	this.displayRemoveUser = function(usr) {
		var $tds = this.$userTbl.find('td');
		$tds.each( function(index) {
			var $thistd = $(this);
			if ($thistd.text() == usr) {
				$thistd.parent().remove();
			}
		});
		var count = this.$userTbl.find('tr').length;
		this.$userCount.text(count);
	}

	this.disableInput = function() {
		this.$msgInput.attr('disabled', 'disabled');
		this.$emoSpan.attr('disabled', 'disabled');
	}

	this.enableInput = function() {
		this.$msgInput.removeAttr('disabled');
		this.$emoSpan.removeAttr('disabled');
	}

	this.update = function(cmd, src) {
		
		if (cmd.room.toLowerCase() !== this.roomName.toLowerCase()) {
			if (cmd.command.toLowerCase() !== 'status') {
				return;
			}
		}
		if (typeof src === 'undefined' || src == null) {
			src = cmd.source;
		}
		var outgoing = (src.toLowerCase() === this.userName.toLowerCase());
		switch(cmd.command) {
			case 'message':
				this.displayMessage(src, cmd.message, false, outgoing);
				break;
			case 'whisper':
				this.displayMessage(src, cmd.message, true, outgoing);
				break;
			case 'join':
				this.displayEvent(cmd.source + ' has joined');
				this.displayAddUser(src, null, cmd.args[0]);
				break;
			case 'leave':
				this.displayEvent(cmd.source + ' has left');
				this.displayRemoveUser(src);
				break;
			case 'kick':
				this.displayEvent(cmd.target + ' has been kicked by ' + cmd.source + ' because: ' + cmd.message);
				this.displayRemoveUser(cmd.target);
				break;
			case 'grant':
				this.displayEvent(cmd.target + ' has been given an ownership token');
				this.displayModifyUser(cmd.target, 'token', null);
				break;
			case 'revoke':
				this.displayEvent(cmd.target + ' has lost his ownership token');
				this.displayModifyUser(cmd.target, null. null);
				break;
			case 'destroy':
				alert(cmd.source + ' has closed the room because: ' + cmd.message);
				this.close();
				break;
			case 'status':
				this.displayModifyUser(cmd.source, null, cmd.args[0]);
				break;
		}
	};

	this.close = function(user, msg, callback) {
		alert('todo:close()');
	}
} 