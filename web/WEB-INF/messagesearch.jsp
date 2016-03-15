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
               rangeSize="${perPage}" recordType="messages">
    <jsp:attribute name="filters">

        <h3>Keywords</h3>
        <div>
            <input type="text" name="keywords[]" maxlength=300 />
            <br />
            <label>All</label>
            <input type="radio" name="keywordsall" value="true">
            <label>Any</label>
            <input type="radio" name="keywordsall" value="false" checked="checked">
        </div>

        <h3>Emoticons</h3>
        <div>
            <div id='emocheckboxes'>
                <c:forEach var="emoticon" items="${emoticonPaths}" varStatus="loop">
                    <label for="emoticon${loop.index}"><img src="../${emoticon.path}"></label>
                    <input type="checkbox" name="emoticons[]" id="emoticon${loop.index}" value="${emoticon.code}">
                </c:forEach>
            </div>
            <input type="radio" name="emoticonsall" value="true">All
            <input type="radio" name="emoticonsall" value="false" checked="checked">Any
        </div>

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
            <input type="checkbox" name="types[]" value="message" >Message
            <input type="checkbox" name="types[]" value="whisper" >Whisper
        </div>
        
        <h3>Timestamp</h3>
        <div>
            <input type="datetime-local" name="startdate" value="2016-01-31T20:55:55">Start
            <input type="datetime-local" name="enddate" value="2016-02-31T20:55:55"> End
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
                    <th>Source</th>
                    <th>Target</th>
                    <th>Message</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="eventRecord" items="${records}">
                    <tr>
                        <td><t:datetime timeMillis="${eventRecord.timestamp}" /></td>
                        <td>${eventRecord.roomName}</td>
                        <td>${eventRecord.sourceName}</td>
                        <td>${eventRecord.targetName}</td>
                        <td class="wrappable">${eventRecord.message}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </jsp:attribute>

</t:adminsearch>