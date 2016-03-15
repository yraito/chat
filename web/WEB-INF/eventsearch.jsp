<%-- 
    Document   : eventsearch
    Created on : Feb 12, 2016, 4:47:36 PM
    Author     : Edward
--%>

<%-- 
    Document   : messagesearch
    Created on : Feb 12, 2016, 5:59:54 AM
    Author     : Edward
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<%@ page session="true" %>

<t:adminsearch action="" numResults="${numResults}" rangeStart="${startIndex}" 
               rangeSize="${perPage}" recordType="events">
    <jsp:attribute name="filters">


        <h3>Rooms</h3>
        <div>
            <input type="text" name="roomnames[]" maxlength=300 />
        </div>

        <h3>Sources</h3>
        <div>
            <input type="text" name="sourcenames[]" maxlength=300 />
        </div>

        <h3>Targets</h3>
        <div>
            <input type="text" name="targetnames[]" maxlength=300 />
        </div>

        <h3>Types</h3>
        <div>
            <input type="checkbox" name="types[]" value="join" >Join
            <input type="checkbox" name="types[]" value="leave" >Leave
            <input type="checkbox" name="types[]" value="kick" >Kick
            <input type="checkbox" name="types[]" value="grant" >Grant
            <input type="checkbox" name="types[]" value="revoke" >Revoke
            <input type="checkbox" name="types[]" value="create" >Create
            <input type="checkbox" name="types[]" value="destroy" >Destroy
            <input type="checkbox" name="types[]" value="login" >Login
            <input type="checkbox" name="types[]" value="logout" >Logout
            <input type="checkbox" name="types[]" value="status" >Status
        </div>

        <h3>Timestamp</h3>
        <div>
            <input type="datetime-local" name="startdate" value="2016-01-31T20:55:55.123" data-template="DD / MM / YYYY     hh : mm a" data-format="YYYY-MM-DDTHH:MM">Start
            <input type="datetime-local" name="enddate" value="2016-02-31T20:55:55.123" data-template="DD / MM / YYYY     hh : mm a" data-format="YYYY-MM-DDTHH:MM"> End
        </div>
    </jsp:attribute>

    <jsp:attribute name="sortOptions">
        <option value="timestamp">Time</option>
        <option value="sourceName">Source</option>
        <option value="targetName">Target</option>
        <option value="roomName">Room</option>
    </jsp:attribute>
    <jsp:attribute name="table">
        <table class="CSSTableGenerator">
            <thead>
                <tr>
                    <th>Time</th>
                    <th>Room</th>
                    <th>Type</th>
                    <th>Source</th>
                    <th>Target</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="eventRecord" items="${records}">
                    <tr>
                        <td><t:datetime timeMillis="${eventRecord.timestamp}" /></td>
                        <td>${eventRecord.roomName}</td>
                        <td>${eventRecord.type}</td>
                        <td>${eventRecord.sourceName}</td>
                        <td>${eventRecord.targetName}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </jsp:attribute>

</t:adminsearch>