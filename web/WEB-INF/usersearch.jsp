<%-- 
    Document   : usersearch
    Created on : Feb 12, 2016, 5:13:23 PM
    Author     : Edward
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<%@ page session="true" %>

<t:adminsearch action="" numResults="${numResults}" rangeStart="${startIndex}" 
               rangeSize="${fn:length(records)}" recordType="users">
    <jsp:attribute name="filters">

        <h3>Username</h3>
        <div>
            <input type="text" name="usernames[]" maxlength=300 />
        </div>

        <h3>Owner username</h3>
        <div>
            <input type="text" name="ownernames[]" maxlength=300 />
        </div>

        <h3>Account Type</h3>
        <div>
            <input type="checkbox" name="types[]" value="user" >User
            <input type="checkbox" name="types[]" value="agent" >Agent
        </div>

        <h3>Account Status</h3>
        <div>
            <input type="checkbox" name="statuses[]" value="active" >Active
            <input type="checkbox" name="statuses[]" value="inactive" >Inactive
        </div>

        <h3>Create Date/Time</h3>
        <div>
            <input type="datetime-local" name="startdate" value="2016-01-31T20:55:55.123">Start
            <input type="datetime-local" name="enddate" value="2016-02-31T20:55:55.123"> End
        </div>

    </jsp:attribute>

    <jsp:attribute name="sortOptions">
        <option value="created">Created</option>
        <option value="username">Name</option>
    </jsp:attribute>
    <jsp:attribute name="table">
        <table class="CSSTableGenerator">
            <thead>
                <tr>
                    <th>Username</th>
                    <th>Owner</th>
                    <th>Email</th>
                    <th>Password</th>
                    <th>Type</th>
                    <th>Created</th>
                    <th>Status</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="userRecord" items="${records}">
                    <tr data-id="${userRecord.id}" data-owner="${userRecord.ownerId}">
                        <td class="td-editabletext" data-name="username" data-value="${userRecord.username}">${userRecord.username}</td>
                        <td>${userRecord.ownerName}</td>
                        <td class="td-editabletext" data-name="email" data-value="${userRecord.email}">${userRecord.email}</td>
                        <td class="td-editabletext" data-name="password"></td>
                        <td>${userRecord.ownerId == null? "user" : "agent"}</td>
                        <td><t:datetime timeMillis="${userRecord.created}" /></td>
                        <c:set var="acctDeleted" value='${userRecord.destroyed == null ? "false" : "true"}' />
                        <c:set var="acctStatus" value='${userRecord.destroyed == null ? "active" : "deleted"}' />
                        <td class="td-editableselect" data-name="delete" data-value="${acctDeleted}">${acctStatus}</td>
                        <td>
                            <form action="processModify" method="POST"  >
                                <input type="hidden" name="id" value="${userRecord.id}" />
                                <input type="submit" value="Modify" class="modify-btn"/>
                            </form>
                        </td>
                    </tr>

                </c:forEach>

            </tbody>
        </table>

        <div class="formdiv" style="display: none">

            <form method="POST" action="processModify" id="userform">
                <label for="username">New username: </label>
                <input type="text" maxlength="30" id="username" name="username" />
                <label for="password">New password: </label>
                <input type="text" maxlength="30" id="password" name="password" />
                <label for="email">New email: </label>
                <input type="text" maxlength="30" id="email" name="email" />
                <label for="status">New status: </label>
                <select id="status" name="status" >
                    <option value="active">Active</option>
                    <option value="inactive">Inactive</option>
                </select>
                <input type="hidden" name="id" value="${userRecord.id}" />
                <input type="submit" value="Submit" />
            </form>

            <form method="POST" action="processModify" id="agentform">
                <label for="username">New username: </label>
                <input type="text" maxlength="30" id="agent-username" name="username" />
                <label for="password">New password: </label>
                <input type="text" maxlength="30" id="agent-password" name="password" />
                <label for="status">New status: </label>
                <select id="agent-status" name="delete" >
                    <option value="false">Active</option>
                    <option value="true">Deleted</option>
                </select>
                <input type="hidden" name="id" value="${userRecord.id}" />
                <input type="submit" value="Submit" />
            </form>
        </div>



        <script>

            var toggleButton = function ($td) {
                var $inputs = $td.parents('tr').first().find('input');
                var $selects = $td.parents('tr').first().find('select');
                var $button = $td.parents('tr').first().find('input.modify-btn');
                if ($inputs.length + $selects.length <= 2) {
                    //alert('disabling');
                    $button.attr('disabled', 'disabled');
                } else {
                    //alert('enabling' + $inputs.length);


                    $button.removeAttr('disabled');
                }
            };

            $('input.modify-btn').each(function () {
                var $td = $(this).parents('td').first();
                toggleButton($td);
            });
            $('td.td-editabletext').on('focus', function () {
                var $td = $(this);
                var $input = $td.html('<input type="text" maxlength=30 />').find('input');
                var name = $td.attr('data-name');
                $input.attr('name', name);
                var value = $td.attr('data-value');
                if (value) {
                    $input.attr('value', value);
                }

                $input.focus();
                toggleButton($td);

                $input.on('blur', function () {
                    var text = $input.val();
                    //s  alert(text.length);
                    if (text.length == 0 || (value && text == value)) {
                        //  alert('asdf');
                        var txt = value ? value : '';
                        $td.text(txt);
                        toggleButton($td);
                    }
                });
            });

            $('td.td-editableselect').on('focus', function () {
                var $td = $(this);
                var value = $td.attr('data-value');
                var otherValue = value == 'true' ? 'false' : 'true';
                if (value == 'true') {
                    return;
                }
                var label = value == 'true' ? 'deleted' : 'active';
                var otherLabel = value == 'true' ? 'active' : 'deleted';
                var $input = $td.html('<select/>').find('select');
                var name = $td.attr('data-name');
                $input.attr('name', name);


                $input.append('<option value=' + value + '>' + label + '</option>');
                $input.append('<option value=' + otherValue + '>' + otherLabel + '</option>');
                $input.focus();
                toggleButton($td);
                $input.on('blur', function () {
                    var text = $input.val();
                    if (text.length === 0 || (value && text === value)) {
                        $td.text(label);
                        toggleButton($td);
                    }
                });
            });


            $('input.modify-btn').on('click', function (e) {
                e.preventDefault();

                var $tr = $(this).parents('tr').first();
                var $inputs = $tr.find('input');
                var $selects = $tr.find('select');
                //$inputs.attr('disabled', 'disabled');
                //$selects.attr('disabled', 'disabled');
                var $all = $inputs.add($selects);
                alert($all.serialize());
                //var label = $selects.val() == 'true' ? 'deleted' : 'active';
                $.ajax({
                    url: '../processModify',
                    type: 'POST',
                    data: $all.serialize()
                }).done(function (html) {
                    alert('success');
                    $inputs.each(function () {
                        if ($(this).attr('type') != 'submit' && $(this).attr('type') != 'hidden') {
                            var $td = $(this).parents('td').first();
                            $td.text($(this).val());
                            $td.attr('data-value', $td.text());
                        }

                    })
                    $selects.each(function () {
                        var $td = $(this).parents('td').first();
                        $td.text($(this).val() == "true" ? 'deleted' : 'active');
                        $td.attr('data-value', $(this).val());
                    })
                    toggleButton($tr.find('td').first());
                }).fail(function (req) {
                    alert('fail: ' + req.responseText);
                    $inputs.removeAttr('disabled');
                    $selects.removeAttr('disabled');
                });

            });
        </script>
    </jsp:attribute>

</t:adminsearch>