
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<%@ page session="true" %>


<div class = "chat-content ${param.room}" data-room="${param.room}">

    <div style="position:relative; height: 100%; width:100%">
        <div class="chat-sidebar">
            <div class = "sidebar-controls">
                <button class="recBtn imageBtn">Record</button>
                <button class="playBtn imageBtn">Record</button>
                <button class="leaveBtn imageBtn">Leave</button>
                <button class="destroyBtn imageBtn">Destroy</button>

            </div>
            <div><span class="userCount">0</span> users</div>
            <table class ="userTable"></table>
        </div>
        <div class="chat-messagepanel"></div>
        <div class="chat-controlpanel"></div>
        <div class="chat-textpanel">

            <textarea placeholder="Enter message"></textarea>
            <button class="chat-send-btn dropbtn">Send</button>
            <button class="chat-emo-btn dropbtn">:)</button>
        </div>
        <div class="formdiv kickdiv">
            <form>
                <label>Reason: </label>
                <input type="text" required="required" maxlength=300 class="textinput"/>
                <input type="submit" value="Kick" />
            </form>
        </div>
        <div class="formdiv whisperdiv">
            <form>
                <label>Message: </label>
                <input type="text" required="required" maxlength=300 class="textinput"/>
                <input type="submit" value="Send" />
            </form>
        </div>
        <div class="formdiv destroydiv">
            <form>
                <label>Reason: </label>
                <input type="text" required="required" maxlength=300 class="textinput"/>
                <input type="submit" value="Destroy" />
            </form>
        </div>

        <div class="formdiv messagehistorydiv"></div>
        <div class="menudiv usermenudiv custom-menu">
            <h3 class="title" style="text-align:center; border-bottom: 1px gray solid"></h3>
            <ul class="custom-menua">
                <li data-action="grant">Grant token</li>
                <li data-action="revoke">Revoke token</li>
                <li data-action="kick">Kick</li>
                <li data-action="whisper">Whisper</li>
            </ul>
        </div>
        <div class="menudiv emomenudiv">
            <c:forEach var="emoPath" items="${emoticonPaths}" >
                <c:set var="emoCode" value="${fn:substringBefore(emoPath, '.')}" />
                <c:set var="emoCode" value="${fn:substringAfter(emoCode, 'emoticons/')}" />
                <c:set var="emoPath" value="${fn:substringAfter(emoPath, '/')}" />
                <img src="${emoPath}" data-code="${emoCode}" alt="${emoCode}"/>
            </c:forEach>
        </div>
    </div>

</div>

<script>

    (function () {

        if (!window.emoticons) {
            window.emoticons = new Object();
            var $imgs = $('.emomenudiv img');
            $imgs.each(function () {
                var $this = $(this);
                var emoCode = '[' + $this.attr('data-code') + ']';
                var emoPath = $this.attr('src');
                window.emoticons[emoCode] = emoPath;
            });
        }

        var room = '${param.room}';
        var user = '${userRecord.username}';
        var $page = $('div.' + room);
        var chatSession = getChatSession(user);
        var client = chatSession.client;

        var view = new View(
                $page.find('.chat-messagepanel'),
                $page.find('.chat-sidebar table'),
                null,
                null,
                $page.find('.userCount'));
        view.roomName = room;
        view.userName = user;
        chatSession.addRoomView(room, view);

        var $whisperdiv = $page.find('.whisperdiv');
        var $kickdiv = $page.find('.kickdiv');
        var $destroydiv = $page.find('.destroydiv');
        var closeFunc = function () {
            chatSession.closeRoom(room);
        };
        var viewcontroller = new ViewController(client, view, closeFunc);
        viewcontroller.initDialogs($whisperdiv, $kickdiv, $destroydiv);
        viewcontroller.initEmoMenu($page.find('.emomenudiv'), $page.find('.chat-emo-btn'), $page.find('.chat-messagepanel'), $page.find('textarea'));
        viewcontroller.initSendControls($page.find('.chat-send-btn'), $page.find('textarea'));
        //viewcontroller.initStatusControls($page.find('select'));
        viewcontroller.initUserMenu($page.find('.usermenudiv'), $page.find('.chat-sidebar table'));
        viewcontroller.initRoomControls($page.find('button.leaveBtn'), $page.find('button.destroyBtn'));
        viewcontroller.initHistoryControls($page.find('button.recBtn'), $page.find('button.playBtn'), $page.find('div.messagehistorydiv'));
        /*client.send(new Command('join').in(room).withargs(pass)).
         done(function(body) {
         view.init(user, $(body));
         }).fail(function(err) {
         alert('Error joining room ' + room + ': ' + err);
         chatSession.closeRoom(room);
         });*/
        if (room.toLowerCase() === 'lobby') {
            $('.leaveBtn').hide();
            $('.destroyBtn').hide();
        }
    }());
</script>



