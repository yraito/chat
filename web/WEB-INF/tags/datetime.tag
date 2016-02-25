<%-- 
    Document   : datetime
    Created on : Feb 16, 2016, 2:54:48 AM
    Author     : Edward
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@tag description="put the tag description here" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="timeMillis" required="true"%>

<%-- any content can be specified here e.g.: --%>
<jsp:useBean id="date" class="java.util.Date" />
<jsp:setProperty name="date" property="time" value="${timeMillis}" />
<fmt:formatDate type="both" dateStyle="medium" timeStyle="medium" value="${date}" />