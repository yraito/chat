<%-- 
    Document   : roomsstub
    Created on : Feb 10, 2016, 1:44:17 AM
    Author     : Edward
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<div class="roomsdiv CSSTableGenerator">
    <button class="create-btn">Create</button>
    <button class="refresh-btn">Refresh</button>
    <table>
        <thead>
            <tr>
                <th>Name</th>
                <th>Owner</th>
                <th>Users</th>
                <th>Private</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach varStatus="loop" var="roomInfo" items="${requestScope.roomInfos}">
                <c:set var="rowClass" value="${loop.index % 2 == 0 ? 'even' : 'odd'}" /> 
                <c:set var = "buttonClass" value="${roomInfo.isPrivateRoom ? 'join-private-btn' : 'join-public-btn'}" />
                <tr class="${rowClass}">
                    <td>${roomInfo.name}</td>
                    <td>${roomInfo.owner}</td>
                    <td>${roomInfo.numberOfUsers}</td>
                    <td>${roomInfo.isPrivateRoom}</td>
                    <td><button class="${buttonClass}" data-room="${roomInfo.name}">Join</button></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>



    <div class="formdiv">
        <div class="creatediv" title="Create a new room">
            <form>
                <label for="createName">Room Name: </label>
                <input type="text" name="createName" id="createName" required="required" maxlength=30 />
                <br />
                <label for="createPass">Room Password: </label>
                <input type="text" name="createPass" id="createPass" maxlength = 30 disabled="disabled" />
                <br />
                <label for="createPrivate">Private</label>
                <input type="checkbox" name="createPrivate" id="createPrivate" value="true" />
                <br />
                <input type="submit" value="Create" /> 
            </form>
        </div>

        <div class="joindiv" title="Join a private room">
            <form>
                <label for="joinPass">Room Password: </label>
                <input type="text" name="joinPass" id="joinPass" required="required" maxlength = 30 />
                <br />
                <input type="submit" value="Create" /> 
            </form>
        </div>

    </div>
</div>
<script>
    var chatSession = getChatSession('${userRecord.username}');
    var $page = $('.roomsdiv');

    $('.create-btn').on('click', function (e) {
        showDialog($page.find('.creatediv'), function () {
            var roomName = $('#createName').val();
            var roomPass = $('#createPrivate').val() ? $('#createPass').val() : null;
            chatSession.createRoom(roomName, roomPass);
        });
    });

    $('.join-public-btn').on('click', function (e) {
        var roomName = $(this).attr('data-room');
        chatSession.joinRoom(roomName, null);
    });

    $('.join-private-btn').on('click', function (e) {
        var roomName = $(this).attr('data-room');
        showDialog($page.find('.joindiv'), function () {
            var roomPass = $('#joinPass').val();
            chatSession.joinRoom(roomName, roomPass);
        });
    });
    
    $('#createPrivate').on('click', function(e) {
       if (this.checked) {
           $('#createPass').removeAttr('disabled');
           $('#createPass').attr('required', 'required');
       } else {
           $('#createPass').removeAttr('required');
           $('#createPass').attr('disabled', 'disabled');
       }
    });
</script>