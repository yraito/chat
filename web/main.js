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


var initStatusControls = function (chatSession, $statusSelect) {
        var initStatus = chatSession.userStatus;
        if (!initStatus) {
            initStatus = 'ONLINE';
        }
        $statusSelect.val(initStatus);
        $statusSelect.on('change', function () {
            var newStatus = $statusSelect.val();
            console.log('changing status to ' + newStatus);
            var statusCmd = new Command('status').withargs(newStatus);
            chatSession.client.send(statusCmd).fail(function (errMsg) {
                alert('Can\'t update status: ' + errMsg);
                $statusSelect.val(chatSession.userStatus);
            }).done(function (body) {
                chatSession.userStatus = $statusSelect.val();
            });
        });
    };