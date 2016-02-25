<%-- 
    Document   : adminheader
    Created on : Feb 16, 2016, 4:23:35 PM
    Author     : Edward
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@tag description="put the tag description here" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>

<%-- any content can be specified here e.g.: --%>
<c:if test="${param.pagefragment != 'true'}">
<div class="admin-header">
    <h1>MyChat <small>Admin</small></h1>
    <a href="processLogout" >Sign Out</a>
</div> 

<nav class="admin-nav">
    <ul>
        <li><a href="messages">Messages</a></li>
        <li><a href="events">Events</a></li>
        <li><a href="users">Users</a></li>
    </ul>
</nav>
</c:if>