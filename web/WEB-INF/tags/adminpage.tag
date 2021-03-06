<%-- 
    Document   : modifyuserpage
    Created on : Feb 16, 2016, 12:40:56 PM
    Author     : Edward
--%>

<%@tag description="put the tag description here" pageEncoding="UTF-8"%>

<%@attribute name="pageHead" fragment="true" required="false"%>
<%@attribute name="pageBody" fragment="true" %>
<%@attribute name="activeLink" %>
<html>
    <head>
        <link rel="stylesheet" href="../theme.css">
 
        <jsp:invoke fragment="pageHead" />
    </head>
    <body>

    <c:if test="${param.pagefragment != 'true'}">
        <div class="admin-header">
            <h1>MyChat <small>Admin</small></h1>
            <span><a class="logoutBtn" href="processLogout" >Sign Out</a></span>
        </div> 

        <nav class="admin-nav">
            <ul>
                <li><a class="${activeLink eq 'messages' ? 'active' : 'not-active'}" href="messages">Messages</a></li>
                <li><a class="${activeLink eq 'events' ? 'active' : 'not-active'}" href="events">Events</a></li>
                <li><a class="${activeLink eq 'users' ? 'active' : 'not-active'}" href="users">Users</a></li>
            </ul>
        </nav>
    </c:if>

    <jsp:invoke fragment="pageBody" />
</body>
<script>
    $('.admin-header a').on('click', function (e) {
        e.preventDefault();
        $.ajax({
            url: 'processLogout',
            type: 'POST'
        }).done(function () {
            window.location.replace('login');
        })
    });
</script>
</html>
