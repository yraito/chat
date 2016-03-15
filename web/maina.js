var createPopupMenu = function ($attachee, event, $menuElem, handler) {


    //SO
    //Show user menu on click table row, and set targetUsr 
    $attachee.bind(event, function (e) {
        //Only if clicked on table row
        if ($(e.target).parents('tr').length > 0) {
            e.preventDefault();
            vc.targetUser = $(e.target).parents('tr').find('td.name').text();
            //alert(this.targetUsr);
            //var x = e.pageX - $userTable.offset().left;
            //var y = e.pageY - $userTable.offset().top;
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
            vc.targetUser = null;
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
        alert('post');
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

var showDialog = function ($form, btnfunc) {
    var dialog, form;
    dialog = $form.dialog({
        autoOpen: false,
        modal: true,
        close: function () {
            form[ 0 ].reset();
        }
    });

    form = dialog.find("form").on("submit", function (event) {
        event.preventDefault();
        btnfunc();
        dialog.dialog("close");
    });

    dialog.dialog("open");
};