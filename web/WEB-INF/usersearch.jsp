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
            <input type="text" name="keywords[]" maxlength=300 />
            <br />
            <label>All</label>
            <input type="radio" name="keywordqualifier" value="all">
            <label>Any</label>
            <input type="radio" name="keywordqualifier" value="any">
        </div>

        <h3>Owner username</h3>
        <div>
            <input type="text" name="roomnames[]" maxlength=300 />
        </div>

        <h3>Account Type</h3>
        <div>
            <input type="checkbox" name="types[]" value="user" >User
            <input type="checkbox" name="types[]" value="agent" >Agent
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
                    <th>Passhash</th>
                    <th>Type</th>
                    <th>Created</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="userRecord" items="${records}">
                    <tr>
                        <td>${userRecord.userName}</td>
                        <td>${userRecord.ownerName}</td>
                        <td>${userRecord.email}</td>
                        <td>${userRecord.passhash}</td>
                        <td>${userRecord.ownerId == null? "user" : "agent"}</td>
                        <td>${userRecord.created}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </jsp:attribute>

</t:adminsearch>