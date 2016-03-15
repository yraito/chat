<%-- 
    Document   : adminsearch
    Created on : Feb 12, 2016, 5:16:29 AM
    Author     : Edward
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@tag description="put the tag description here" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib  prefix="tt" uri="/WEB-INF/tlds/admintaglib" %>
<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="recordType" required="true"%>
<%@attribute name="numResults" required="true"%>
<%@attribute name="rangeStart" required="true"%>
<%@attribute name="rangeSize" required="true"%>
<%@attribute name="action" required="true"%>
<%@attribute name="filters" fragment="true"%>
<%@attribute name="sortOptions" fragment="true"%>
<%@attribute name="table" fragment="true"%>

<%-- any content can be specified here e.g.: --%>

<t:adminpage activeLink="${recordType}">

    <jsp:attribute name="pageHead">
        <title>Admin search</title>
        <link rel="stylesheet" href="../theme.css">
        <link rel="stylesheet" href="../table.css">
        <link rel="stylesheet" href="../jquery-ui.min.css">
        <link rel="stylesheet" href="../jquery-ui.structure.min.css">
        <link rel="stylesheet" href="../jquery-ui.theme.min.css">
        <script src="../jquery-1.12.0.min.js"></script>
        <script src="../jquery-ui.min.js"></script>
        <style>
            #filterssidebardiv {
                width:30%;
                float:left;
            }

            #tablediv {
                width:70%;
                float: left;
            }
            #controlsdiv span{
                float:right;
            }
            #controlsdiv p {
                display: inline;
            }
            #filtersdiv input[type=text],
            #filtersdiv input[type=datetime-local]{
                width: 100%;
            }
            #tablenavdiv li {
                display: inline;
                list-style: none;
                margin-left: 5px;
                margin-right: 5px; 
            }
            #tablenavdiv ul {
                float: right;
            }
            #tablediv td.wrappable {
                word-wrap: break-word;
                word-break: break-all;
            }
            #controlsdiv {
                margin-top: 5px;
                margin-bottom: 5px;
            }

        </style>
    </jsp:attribute>

    <jsp:attribute name="pageBody">
        <form method="GET" action="${recordType}">

            <div id="controlsdiv">
                <c:choose>
                    <c:when test="${numResults != null && numResults != 0}">
                        <c:set var="rangeEnd" value="${rangeStart + perPage > numResults ? numResults : rangeStart + perPage}" />
                        <p>Showing results ${rangeStart + 1} to ${rangeEnd} of ${numResults}</p>
                    </c:when>
                    <c:otherwise>
                        <p>No results found</p>
                    </c:otherwise>
                </c:choose>

                <span>
                    <label>Results per page: </label>
                    <select name="perpage">
                        <option value="25">25</option>
                        <option value="50">50</option>
                        <option value="100">100</option>
                    </select>
                    <label>Sort by: </label>
                    <select name="sortby">
                        <jsp:invoke fragment="sortOptions" />
                    </select>
                </span>


            </div>

            <div id="filterssidebardiv">
                <div id="filtersdiv">
                    <jsp:invoke fragment="filters" />

                </div>
                <span class="buttons">
                    <input type="submit" value="Search" />
                    <input type="reset" value="Clear" />
                </span>

            </div>

            <div id="tablediv">


                <jsp:invoke fragment="table" />
                <div id="tablenavdiv">
                    <tt:tablenav startIndex="${rangeStart}" resultsPerPage="${rangeSize}" numResults="${numResults}" baseUrl="${recordType}" /> 
                </div>
            </div>
        </form>
                <script src="moment.js"></script>
                <script src="combodate.js"></script>
        <script>
            $('#filtersdiv').accordion();


        </script>
    </jsp:attribute>

</t:adminpage>
