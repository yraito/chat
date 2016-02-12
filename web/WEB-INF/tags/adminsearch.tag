<%-- 
    Document   : adminsearch
    Created on : Feb 12, 2016, 5:16:29 AM
    Author     : Edward
--%>

<%@tag description="put the tag description here" pageEncoding="UTF-8"%>

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
<html>
    <head>
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

            #header h1 {
                display:inline;
            }
            #header a {
                float: right;
                -webkit-appearance: button;
                -moz-appearance: button;
                appearance: button;

                text-decoration: none;
                color: initial;
            }

            nav li{
                display:inline;
            }
        </style>
    </head>
    <body>
        <div id="header">
            <h1>MyChat <small>Admin</small></h1>
            <a href="processLogout" >Sign Out</a>
        </div> 

        <nav>
            <ul>
                <li><a href="messages">Messages</a></li>
                <li><a href="events">Events</a></li>
                <li><a href="users">Users</a></li>
            </ul>

        </nav>
        <form method="POST" action="${recordType}">
            <div id="filterssidebardiv">
                <div id="filtersdiv">
                    <jsp:invoke fragment="filters" />

                </div>
                <input type="submit" value="Search" />
                <input type="reset" value="Clear" />
            </div>

            <div id="tablediv">
                <h2>${recordType}</h2>

                <div id="controlsdiv">
                    <p>Showing results ${rangeStart} to ${rangeStart + rangeSize - 1} of ${numResults}</p>
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
                <jsp:invoke fragment="table" />
                <div id="tablenavdiv">

                </div>
            </div>
        </form>

        <script>
            $('#filtersdiv').accordion();
        </script>
    </body>

</html>