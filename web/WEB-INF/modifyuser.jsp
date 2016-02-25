<%-- 
    Document   : modifyuser
    Created on : Feb 16, 2016, 12:41:26 PM
    Author     : Edward
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<t:adminpage>
    <jsp:attribute name="pageHead">
        <title>Modify User Account</title>
    </jsp:attribute>
    <jsp:attribute name="pageBody">
        <h2>User Account ${userRecord.id}</h2>
        <p>Username: ${userRecord.username}</p>
        <p>Email: ${userRecord.email}</p>
        <p>Status: ${userRecord.destroyed == null ? "active" : "inactive"}</p>
        <form method="POST" action="processModify">
            <label for="username">New username: </label>
            <input type="text" maxlength="30" id="username" name="username" />
            <label for="password">New password: </label>
            <input type="text" maxlength="30" id="password" name="password" />
            <label for="email">New email: </label>
            <input type="text" maxlength="30" id="email" name="email" />
                        <label for="status">New email: </label>
                        <select id="status" name="status" >
                            <option value="active">Active</option>
                            <option value="inactive">Inactive</option>
                        </select>
            <input type="hidden" name="id" value="${userRecord.id}" />
            <input type="submit" value="Submit" />
        </form>
    </jsp:attribute>
</t:adminpage>
