if (!window.console) {
    window.console = {
        log: function () {
            return;
        }
    };
}

function ViewController(client, view, closeRoomFunc) {
    this.targetUser = null;
    this.client = client;
    this.view = view;
    this.closeRoomFunc = closeRoomFunc;
    this.$whisperdiv = null;
    this.$kickdiv = null;
    this.$destroydiv = null;
    this.$whisperdialog = null;
    this.$kickdialog = null;
    this.$destroydialog = null;
    this.initX = null;
    this.initY = null;
    this.recording = false;

    this.showWhisperDialog = function () {
        this.$whisperdiv.attr('title', 'Whisper @' + this.targetUser);
        this.$whisperdialog.dialog('open');
    };

    this.showKickDialog = function () {
        this.$kickdiv.attr('title', 'Kick ' + this.targetUser);
        this.$kickdialog.dialog('open');
    };

    this.showDestroyDialog = function () {
        this.$destroydiv.attr('title', 'Destroy ' + this.view.roomName);
        this.$destroydialog.dialog('open');
    };

    this.initUserMenu = function ($userMenu, $userTable) {

        var vc = this;

        //SO
        //Show user menu on click table row, and set targetUsr 
        $userTable.bind('click', function (e) {
            //Only if clicked on table row
            if ($(e.target).parents('tr').length > 0) {

                e.preventDefault();
                vc.targetUser = $(e.target).parents('tr').find('td.name').text();
                $userMenu.find('.title').text(vc.targetUser);
                //alert(vc.targetUser + e.pageY + e.pageX);
                var x = e.pageX - $userTable.offset().left;
                var y = e.pageY - $userTable.offset().top;
                $userMenu.finish().toggle(100)
                        .css({
                            top: e.pageY + "px",
                            left: e.pageX + "px"
                        });
            }
        });

        //Hide user menu on click elsewhere
        $(document).bind("mousedown", function (e) {

            if (!$(e.target).parents('.usermenudiv').length > 0) {
                //vc.targetUser = null;
                $userMenu.hide(100);
            }
        });


        //Execute command on click menu item
        $userMenu.find('li').click(function (e) {
            //alert('click');
            switch ($(this).attr("data-action")) {
                case "whisper":
                    vc.showWhisperDialog(vc.$whisperdiv);
                    break;
                case "kick":
                    vc.showKickDialog(vc.$kickdiv);
                    break;
                case "grant":
                    var grantCmd = new Command('grant').to(vc.targetUser).in(vc.view.roomName);
                    vc.client.send(grantCmd).fail(function (msg) {
                        vc.view.displayError(msg);
                    });
                    break;
                case "revoke":
                    var revokeCmd = new Command('revoke').to(vc.targetUser).in(vc.view.roomName);
                    vc.client.send(revokeCmd).fail(function (msg) {
                        vc.view.displayError(msg);
                    });
                    break;
            }
            $userMenu.hide(100);
            //alert('post');
        });
    };

    this.initEmoMenu = function ($emoSpan, $emoButton, $msgPanel, $textArea) {
        $emoButton.on('click', function (e) {
            e.preventDefault();
            layoutEmoticons($emoSpan, $msgPanel);
        });

        $(document).bind("mousedown", function (e) {
            if (!($(e.target).parents('.emomenudiv').length > 0)) {
                $emoSpan.hide(100);
            }
        });

        $emoSpan.find('img').on('click', function (e) {
            $textArea.val($textArea.val() + ' [' + $(this).attr('data-code') + ']');
            $emoSpan.hide(100);
        });
    };

    this.initSendControls = function ($sendButton, $sendTextarea) {
        //alert($sendButton);
        $sendButton.on('click', function () {
            var msgToSend = $sendTextarea.val();
            var msgCommand = new Command('message').in(view.roomName).saying(msgToSend);
            client.send(msgCommand)
                    .done(function () {
                        view.displayMessage(view.userName, msgToSend, false, true);
                    })
                    .fail(function (errMsg) {
                        view.displayError(errMsg);
                    });
            $sendTextarea.val('');
        });
    };

    this.initStatusControls = function ($statusSelect) {
        var initStatus = this.view.userStatus;
        if (!initStatus) {
            initStatus = 'ONLINE';
        }
        $statusSelect.val(initStatus);
        $statusSelect.on('change', function () {
            var newStatus = $statusSelect.val();
            console.log('changing status to ' + newStatus);
            var statusCmd = new Command('status').withargs(newStatus);
            client.send(statusCmd).fail(function (errMsg) {
                view.displayError('STATUS ' + errMsg);
                $statusSelect.val(view.userStatus);
            }).done(function (body) {
                view.userStatus = $statusSelect.val();
            });
        });
    };

    this.initRoomControls = function ($leaveBtn, $destroyBtn) {
        var vc = this;
        $leaveBtn.on('click', function () {
            var leaveCmd = new Command('leave').in(view.roomName);
            client.send(leaveCmd).fail(function (err) {
                alert(err);
                vc.closeRoomFunc();
            }).done(function () {
                vc.closeRoomFunc();

            });
        });
        $destroyBtn.on('click', function () {
            var destroyCmd = new Command('destroy').in(view.roomName);
            client.send(destroyCmd).fail(function (err) {
                alert(err);
            }).done(function () {
                vc.closeRoomFunc();
            });
        });
    };

    this.initDialogs = function ($whisperdiv, $kickdiv, $destroydiv) {
        this.$whisperdiv = $whisperdiv;
        this.$kickdiv = $kickdiv;
        this.$destroydiv = $destroydiv;

        var room = this.view.roomName;
        var vc = this;

        var kickfunc = function () {
            var message = $kickdiv.find('.textinput').val();
            var target = vc.targetUser;
            var kickCmd = new Command('kick').in(room).to(target).saying(message);
            vc.client.send(kickCmd)
                    .fail(function (errMsg) {
                        vc.view.displayError(errMsg);
                    });
        };
        var whisperfunc = function () {
            var message = $whisperdiv.find('.textinput').val();
            var target = vc.targetUser;
            //alert('whisper' + target);
            var whisperCmd = new Command('whisper').in(room).to(target).saying(message);
            vc.client.send(whisperCmd)
                    .done(function () {
                        vc.view.displayMessage(vc.view.userName, message, true, true);
                    })
                    .fail(function (errMsg) {
                        vc.view.displayError(errMsg);
                    });
        };
        var destroyfunc = function () {
            var message = $destroydiv.find('.textinput').val();
            var destroyCmd = new Command('destroy').in(room).saying(message);
            vc.client.send(destroyCmd)
                    .done(function () {
                        vc.closeRoomFunc();
                    })
                    .fail(function (errMsg) {
                        vc.view.displayError(errMsg);
                    });
        };
        this.$whisperdialog = createDialog($whisperdiv, whisperfunc);
        this.$kickdialog = createDialog($kickdiv, kickfunc);
        this.$destroydialog = createDialog($destroydiv, destroyfunc);
    };

    this.initHistoryControls = function ($recBtn, $playBtn, $histDiv) {

        var vc = this;
        $recBtn.on('click', function () {
            if (!vc.recording) {
                $histDiv.empty();
                vc.view.$msgHistory = $histDiv;
                $recBtn.css({
                    'background-image': 'url("images/stop.png")'
                });
            } else {
                vc.view.$msgHistory = null;
                $recBtn.css({
                    'background-image': 'url("images/rec.png")'
                });
            }
            vc.recording = !vc.recording;
        });

        $playBtn.on('click', function () {
            var newWindow = window.open('', '', '');
            var doc = newWindow.document;
            doc.open();
            doc.write('<html><head><base href="../"><link rel="stylesheet" type="text/css" href="chatstyle.css" /></head><body><div class="chat-messagepanel">' + $histDiv.html() + '</div></body></html>');
            doc.close();
        });
    }
}

var createDialog = function ($formdiv, submitfunc) {
    var dialog, form;
    dialog = $formdiv.dialog({
        autoOpen: false,
        //height: 150,
        //width: 350,
        modal: true,
        close: function () {
            if (form.length) {
                form[ 0 ].reset();
                //allFields.removeClass( "ui-state-error" );
            }
        }
    });

    form = dialog.find("form").on("submit", function (event) {
        event.preventDefault();
        submitfunc();
        dialog.dialog("close");
    });
    return dialog;
};


var layoutEmoticons = function ($emoSpan, $msgPanel) {

    var initX = $msgPanel.width() + $msgPanel.position().left;
    var initY = $msgPanel.height() + $msgPanel.position().top;
    //initY = Math.floor(window.innerHeight*0.92);
    var imgSize = 38;
    var numImgs = $emoSpan.find('img').length;
    var viewRatio = window.innerWidth / window.innerHeight;

    // alert(viewRatio);
    var width = Math.sqrt(numImgs * viewRatio);
    width = Math.floor(width) * imgSize;
    width = Math.min(width, window.innerWidth);
    var nrows = Math.ceil(numImgs / Math.floor(width / imgSize));
    height = nrows * imgSize;
    //var height = Math.ceil(width / viewRatio);
    var left = Math.max(0, initX - width);
    var top = Math.max(0, initY - height);
    $emoSpan.finish().toggle(100).css({
        position: "absolute",
        left: left,
        top: top,
        width: width + "px",
        "max-width": width + "px",
        height: height + "pxs"
    });
    // alert(width + " " + height + " " + left + " " + top);
};
        