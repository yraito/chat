<%-- 
    Document   : accoutstub
    Created on : Feb 11, 2016, 8:39:36 PM
    Author     : Edward
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ page isELIgnored="false" %>
<div class = "account-content">
    <div class="replace-div">
        <div class="profilediv splitdiv" style="width:40%">
            <h2>Profile</h2>
            <p class="status"></p>
            <p>Username: ${userRecord.username}</p>
            <p>Email: ${userRecord.email}</p>
            <p>Created:<t:datetime timeMillis="${userRecord.created}" /></p>
            <p>Password: <button class="user-pass-btn">Change</button></p>
        </div>
        <div class="agentsdiv splitdiv" style="width:60%">
            <h2>Agents</h2>

            <p class="status"></p>
            <table class ="CSSTableGenerator">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Created</th>
                        <th>UUID</th>
                        <th>Password</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="agent" items="${agents}">
                        <tr data-name="${agent.username}" data-id="${agent.id}">
                            <td>${agent.username}</td>
                            <td><t:datetime timeMillis="${agent.created}" /></td>
                            <td>${agent.uuid}</td>
                            <td><button class="agent-pass-btn">Change</button></td>
                            <td><button class="agent-delete-btn">Delete</button></td>
                        </tr>
                    </c:forEach>
                    <tr>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td><button class="agent-create-btn">Create</button></td>
                    </tr>
                </tbody>
            </table>

        </div>
        <script>
            $('.user-pass-btn').on('click', function (e) {
                window.targetUserId = null;
                $passDlg.dialog('open');
            });

            $('.agent-pass-btn').on('click', function (e) {
                window.targetUserId = $(this).parents('tr').first().attr('data-id');
                $passDlg.dialog('open');
            });

            $('.agent-create-btn').on('click', function (e) {
                $createDlg.dialog('open');
            });

            $('.agent-delete-btn').on('click', function (e) {
                window.targetUserId = $(this).parents('tr').first().attr('data-id');
                $deleteDlg.dialog('open');
            });
        </script>
    </div>
    <div class="formdiv" id="createagentdiv" title="Create a new agent">
        <form>
            <label for="createAgentName">Name: </label>
            <input type="text" required="required" name="username" id="createAgentName" maxlength=35 />
            <label for="createAgentPass">Password: </label>
            <input type="password" required="required" name="password" id="createAgentPass" maxlength=35 />
            <input type="hidden" name="agent" value="true" />
            <input type="submit" value="Submit" />

        </form>
    </div>
    <div class="formdiv" id="changepassdiv" title="Change password">
        <form>
            <label for="changePass">New Password: </label>
            <input type="password" name="password" required="required" id="changePass" maxlength=35 />
            <input type="submit" value="Submit" />
        </form>
    </div>

    <div class="formdiv" id="deleteagentdiv" title="Delete agent">
        <form>
            <input type="hidden" name="delete" value="true" />
            <input type="submit" value="Delete" />
        </form>
    </div>

</div>
<script>

    window.targetUserId = null;

    var showStatus = function ($p, success, text) {
        if (success) {
            $p.removeClass('error').addClass('success');
        } else {
            $p.removeClass('success').addClass('error');
        }
        $p.html(text);
    };

    var refreshPage = function (handler) {
        var jqxhr = $.ajax({
            url: 'account',
            cache: false,
            t: new Date().getTime()
        }).done(function (html) {
            handler(html);
        }).fail(function (req) {
            alert(req.status + ': ' + req.statusText + '<br />' + req.responseText);
        });
        return jqxhr;
    };

    var $createDlg = createDialog($('#createagentdiv'), function () {
        var data = $('#createagentdiv').find('form').serialize();
        var $p = $('div.agentsdiv p.status');
        $.ajax({
            url: 'processRegister',
            type: 'POST',
            data: data
        }).done(function (text) {
            alert(text);
            refreshPage(function (html) {
                $('.replace-div').html($(html).find('.replace-div').html());
                alert('refreshed');
                var $p2 = $('div.agentsdiv p.status');
                alert($p2.length);
                showStatus($p2, true, 'New agent created');

            });
        }).fail(function (req) {
            alert('fail');
            showStatus($p, false, req.status + ': ' + req.statusText + '<br />' + req.responseText);
        });
    });

    var $passDlg = createDialog($('#changepassdiv'), function () {
        var data = $('#changepassdiv').find('form').serialize();
        var $p;
        if (window.targetUserId) {
            $p = $('div.agentsdiv p.status');
            data['id'] = window.targetUserId;
        } else {
            $p = $('div.profilediv p.status');
        }

        $.ajax({
            url: 'processModify',
            type: 'POST',
            data: data
        }).done(function () {
            showStatus($p, true, "Password changed");
        }).fail(function (req) {
            showStatus($p, false, req.status + ': ' + req.statusText + '<br />' + req.responseText);
        });
    });

    var $deleteDlg = createDialog($('#deleteagentdiv'), function () {
        var $p = $('div.agentsdiv p.status');
        var id = window.targetUserId;
        $.ajax({
            url: 'processModify',
            type: 'POST',
            data: {
                id: id
            }
        }).done(function () {
            refreshPage(function (html) {
                $('.replace-div').html($(html).find('.replace-div').html());
                var $p2 = $('div.agentsdiv p.status');
                showStatus($p2, true, 'Agent ' + id + ' deleted');

            });
        }).fail(function (req) {
            showStatus($p, false, req.status + ': ' + req.statusText + '<br />' + req.responseText);
        });
    });
</script>