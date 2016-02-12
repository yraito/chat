<%-- 
    Document   : main
    Created on : Feb 11, 2016, 2:47:45 PM
    Author     : Edward
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link rel="stylesheet" href="theme.css">
        <link rel="stylesheet" href="chatstyle.css">
        <link rel="stylesheet" href="main.css">
        <link rel="stylesheet" href="jquery-ui.structure.min.css">
        <link rel="stylesheet" href="jquery-ui.theme.min.css">
        <link rel="stylesheet" href="table.css">
        <style>
            #content {
                display: none;
            }
            .header-button {
                float: right;
            }
            #page-header h1 {
                display: inline;
            }
            #page-header {
                
            }
         
            .tabdivl, .tabdivl > div {
                overflow: hidden;
            }
               nav {
               
            }
            .dropdown {
                position: relative;
                display: inline-block;
                float: right;
            }

            .dropdown-content {
                display: none;
                position: absolute;
                background-color: #f9f9f9;
                min-width: 160px;
                box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
                padding: 12px 16px;
                z-index: 1;
            }

            .dropdown:hover .dropdown-content {
                display: block;
            }
        </style>
    </head>
    <body>

        <div id = "page-header">
            <h1>
                MyChat <small>Userspace</small>
            </h1>
            <div class="dropdown">
                <button class=".header-btn" type="button">
                    My Account
                </button>
                <ul class="dropdown-content">
                    <li data-action="account">Profile</li>
                    <li data-action="logout">Sign out</li>
                </ul>
            </div>
        </div>

        <nav id = "nav">
            <ul class="nav navbar-nav">

            </ul>
        </nav>

        <div id = "content">


        </div>

        <div class="formdiv" id="accountdiv">

        </div>
        <script type="text/javascript" src="jquery-1.12.0.min.js"></script>
        <script type="text/javascript" src="jquery-ui.min.js"></script>
        <script type="text/javascript" src="client.js"></script>
        <script type="text/javascript" src="view.js"></script>
        <script type="text/javascript" src="viewcontroller.js"></script>
        <script type="text/javascript" src="session.js"></script>
        <script>
            (function () {
                var tabs = $('#nav').tabs({
                    heightStyle: "fill"
                });
                var tabTemplate = "<li><a href='#[href]'>#[label]</a> <span class='ui-icon ui-icon-close' role='presentation'>Remove Tab</span></li>";
                var addTab = function (label, html) {
                    var id = "tabs-" + label,
                            li = $(tabTemplate.replace(/#\[href\]/g, "#" + id).replace(/#\[label\]/g, label));

                    tabs.find(".ui-tabs-nav").append(li);
                    tabs.append("<div id='" + id + "' class='tabdiv'><p>" + html + "</p></div>");
                    tabs.tabs("refresh");
                };
                tabs.delegate("span.ui-icon-close", "click", function () {
                    var panelId = $(this).closest("li").remove().attr("aria-controls");
                    $("#" + panelId).remove();
                    tabs.tabs("refresh");
                });

                var resize = function () {
                    var h = window.innerHeight - tabs.offset().top;
                    var w = window.innerWidth - tabs.offset().left;
                    tabs.css({
                        'box-sizing' : 'border-box',
                        'min-height': '400px',
                        'overflow': 'hidden',
                        'margin-top': '0px',
                        'margin-bottom': '0px',
                        'height': h,
                        'width': w-30
                    });
                };
                resize();
                $('body').on('resize', function() {
                    setTimeout(function() {
                        resize();
                    }, 100);
                });
                var thisUser = '${userRecord.username}';
                var chatSession = getChatSession(thisUser);
                chatSession.roomHandler = function (name, page) {
                    addTab(name, page);
                };
                chatSession.listHandler = function (page) {
                    addTab('Rooms', page);
                };
                chatSession.listRooms();
                chatSession.joinRoom('lobby', null);
            }());
        </script>
    </body>
</html>
