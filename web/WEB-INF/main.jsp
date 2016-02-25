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

            a.tabdivl, a.tabdivl > div {
                overflow: hidden;
            }


            tabs-min {    
                background: transparent;    
                border: none; 
            } 
            #tabs-min .ui-widget-header {    
                background: transparent;    
                border: none; 
                border-bottom: 1px solid #c0c0c0;  
                -moz-border-radius: 0px;   
                -webkit-border-radius: 0px;  
                border-radius: 0px; 
            } 
            #tabs-min .ui-tabs-nav .ui-state-default {  
                background: transparent;  
                border: none; 
            } 
            #tabs-min .ui-tabs-nav .ui-state-active { 
                background: transparent url(img/uiTabsArrow.png) no-repeat bottom center; 
                border: none; 
            } 
            #tabs-min .ui-tabs-nav .ui-state-default a {   
                color: #c0c0c0; 
            } 
            #tabs-min .ui-tabs-nav .ui-state-active a {    
                color: #459e00; 
            }
        </style>
    </head>
    <body>

        <div id = "page-header">
            <h1>
                MyChat <small>Userspace</small>
            </h1>
            <div class="dropdown" style="float:right; z-index: 1000">
                <button class="dropbtn" type="button">
                    My Account
                </button>
                <div class="dropdown-content">
                    <a id ="accounta" href="account">Profile</a>
                    <a id ="logouta" href="processLogout">Sign out</a>
                </div>
            </div>
        </div>

        <nav id = "tabs-min">
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
                var tabs = $('#tabs-min').tabs({
                    heightStyle: "fill"
                });
                var tabTemplate2 = "<li><a href='#[href]'>#[label]</a> <span class='ui-icon ui-icon-close' role='presentation'>Remove Tab</span></li>";
                var tabTemplate = "<li><a href='#[href]'>#[label]</a></li>";
                var addTab = function (label, html) {
                    var id = "tabs-" + label;
                    var li = $(tabTemplate.replace(/#\[href\]/g, "#" + id).replace(/#\[label\]/g, label));
                    li.attr('id', 'lis-' + label);
                    tabs.find(".ui-tabs-nav").append(li);
                    tabs.append("<div id='" + id + "' class='tabdiv'><p>" + html + "</p></div>");
                    tabs.tabs("refresh");
                };
                /* tabs.delegate("span.ui-icon-close", "click", function () {
                 var panelId = $(this).closest("li").remove().attr("aria-controls");
                 $("#" + panelId).remove();
                 tabs.tabs("refresh");
                 });*/

                var resize = function () {
                    var h = window.innerHeight - tabs.offset().top;
                    var w = window.innerWidth - tabs.offset().left;
                    tabs.css({
                        'box-sizing': 'border-box',
                        'min-height': '400px',
                        'overflow': 'hidden',
                        'margin-top': '0px',
                        'margin-bottom': '0px',
                        'height': h,
                        'width': w - 30
                    });
                };
                //resize();
                $('body').on('resize', function () {
                    setTimeout(function () {
                        //resize();
                    }, 100);
                });

                var openAccountTab = function () {
                    $.ajax({
                        url: 'account',
                        type: 'get'
                    }).done(function (html) {
                        var $account = $('#tabs-Account');
                        if ($account.length) {
                            $('.account-content').find('.replace-div').html($(html).find('.replace-div').html());
                        } else {
                            addTab('Account', html);
                        }

                    }).fail(function (req) {
                        alert(req.statusText);
                    });
                }
                $('#accounta').on('click', function (e) {
                    e.preventDefault();
                    openAccountTab();
                });
                $('#logouta').on('click', function (e) {
                    alert("j");s
                    e.preventDefault();
                    $.ajax({
                        url: 'processLogout',
                        type: 'POST'
                    }).done(function() {
                       window.location.replace('login'); 
                    });
                });
                var thisUser = '${userRecord.username}';
                var chatSession = getChatSession(thisUser);
                chatSession.roomHandler = function (name, page) {
                    var $room = $('#tabs-' + name);
                    if (!$room.length) {
                        addTab(name, page);
                    }

                    //chatSession.listRooms();
                };
                chatSession.roomCloseHandler = function (name) {
                    $('#lis-' + name).remove();
                    $('#tabs-' + name).remove();
                    tabs.tabs('refresh');
                    //chatSession.listRooms();
                };
                chatSession.listHandler = function (page) {
                    var $rooms = $('#tabs-Rooms');
                    //alert(page);
                    if ($rooms.length) {
                        //alert('replace');
                        //$('div.roomsdiv').replaceWith(page);
                        $('.roomsdiv').find('.replace-div').html($(page).find('.replace-div').html());
                        tabs.tabs('refresh');

                    } else {
                        //alert('add');
                        addTab('Rooms', page);
                    }

                };

                chatSession.joinRoom('Lobby', null);
                chatSession.listRooms();
            }());
        </script>
    </body>
</html>
