<%-- 
    Document   : main
    Created on : Feb 11, 2016, 2:47:45 PM
    Author     : Edward
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <base href="../">
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


            #tabs-min {    
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
           
                border: none; 
            } 
            #tabs-min .ui-tabs-nav .ui-state-default a {   
                color: #c0c0c0; 
            } 
            #tabs-min .ui-tabs-nav .ui-state-active a {    
                color: #459e00; 
            }

            #tabs-min > div {
                overflow:hidden;
            }

        </style>
    </head>
    <body>

        <div id = "page-header">
            <h1>
                MyChat <small></small>
            </h1>


            <div class="dropdown" style="float:right; z-index: 2">
                <button class="dropbtn" type="button">
                    My Account
                </button>
                <div class="dropdown-content">
                    <a id ="accounta" href="account">Profile</a>
                    <a id ="logouta" href="processLogout">Sign out</a>
                </div>
            </div>
            <span style="float:right; margin-right: 50px; padding-right:10px; border-right-color: #c0c0c0; border-right-width: 1px; border-right-style: solid">
                <button class="dropbtn" onclick="window.open('api.html')">API</button>
                <button class="dropbtn" onclick="window.open('admin')">Admin</button>
            </span>
            <span style="float:right; margin-right: 50px; padding-right:10px; border-right-color: #c0c0c0; border-right-width: 1px; border-right-style: solid">
                <button class="dropbtn" id="ansBtn">Ans Machine</button>
                <button class="dropbtn" id="roomsBtn">Rooms</button>
                <select id="statusSelect" style="padding:12px">
                    <option>ONLINE</option>
                    <option>BUSY</option>
                    <option>AWAY</option>
                    <option>WAITING</option>
                    <option>OFFLINE</option>
                </select>
            </span>
        </div>

        <nav id = "tabs-min" >

            <ul class="nav navbar-nav">

            </ul>
        </nav>


        <div class="formdiv" id="accountdiv" title="My Account">

        </div>
        <div class="formdiv" id="roomsdiv" title="Rooms">

        </div>
        <div class="formdiv" id="replymessagediv" title="Setup answering message">
            <form>
                <label>Auto-reply message: </label>
                <textarea style="width:90%"></textarea>
                <input type="submit" value="Start" />
            </form>
        </div>
        <div class="formdiv" id="answeringmachinediv" title="Answering machine">
            <form>
                <label>Auto-reply message: </label>
                <br/>
                <textarea disabled="disabled" style="width:90%"></textarea>
                <br/>
                <label>Whispers received: </label>
                <br/>
                <div style="width:90%; overflow:auto"></div>

            </form>
        </div>
        <script type="text/javascript" src="jquery-1.12.0.min.js"></script>
        <script type="text/javascript" src="jquery-ui.min.js"></script>
        <script type="text/javascript" src="client.js"></script>
        <script type="text/javascript" src="view.js"></script>
        <script type="text/javascript" src="viewcontroller.js"></script>
        <script type="text/javascript" src="session.js"></script>
        <script type="text/javascript" src="main.js"></script>
        <script>
                    (function () {
                        var tabs = $('#tabs-min').tabs({
                            heightStyle: "fill"
                        });
     
                        var tabTemplate = "<li><a href='#[href]'>#[label]</a></li>";
                        var addTab = function (label, html) {
                            var id = "tabs-" + label;
                            var li = $(tabTemplate.replace(/#\[href\]/g, "web/main#" + id).replace(/#\[label\]/g, label));
                            li.attr('id', 'lis-' + label);
                            tabs.find(".ui-tabs-nav").append(li);
                            tabs.append("<div id=\"" + id + "\" class=\"tabdiv\">" + html + "</div>");
                            tabs.tabs("refresh");
                            $('#tabs-min > div').css({
                                overflow: 'hidden'
                                
                                
                            });
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


                        //var $acctDlg = createDialog($('#accountdiv'), function () {
                        //    return;
                        //});
                        var $acctDlg = $('#accountdiv').dialog({
                            height: window.innerHeight * 0.9,
                            width: window.innerWidth * 0.9,
                            autoOpen: false,
                            modal: true
                        });
                        var $roomsDlg = $('#roomsdiv').dialog({
                            height: window.innerHeight * 0.7,
                            width: window.innerWidth * 0.7,
                            autoOpen: false,
                            modal: true
                        });


                        var openAccountDialog = function () {
                            $.ajax({
                                url: 'web/account',
                                type: 'get',
                                cache: false,
                                t: new Date().getTime()
                            }).done(function (html) {
                                var $account = $('#accountdiv');
                                if (/*$account.length*/ false) {
                                    $('.account-content').find('.replace-div').html($(html).find('.replace-div').html());

                                } else {
                                    $account.html(html);
                                    //$acctDlg = createDialog($('.account-content'), function () {
                                    //    return;
                                    //s});
                                }
                                $acctDlg.dialog('open');

                            }).fail(function (req) {
                                alert(req.statusText);
                            });
                        }
                        $('#accounta').on('click', function (e) {
                            e.preventDefault();
                            openAccountDialog();
                        });
                        $('#logouta').on('click', function (e) {
                            alert("j");
                            
                            e.preventDefault();
                            $.ajax({
                                url: 'web/processLogout',
                                type: 'POST'
                            }).done(function () {
                                window.location.replace('login');
                            });
                        });
                        var thisUser = '${userRecord.username}';
                        var chatSession = getChatSession(thisUser);
                        //var $roomsDlg = createDialog($('#roomsdiv'), function() {});
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
                            /* var $rooms = $('#tabs-Rooms');
                             //alert(page);
                             if ($rooms.length) {
                             //alert('replace');
                             //$('div.roomsdiv').replaceWith(page);
                             $('.roomsdiv').find('.replace-div').html($(page).find('.replace-div').html());
                             tabs.tabs('refresh');
                             
                             } else {
                             //alert('add');
                             addTab('Rooms', page);
                             }*/
                            var $replacediv = $('.roomsdiv').find('.replace-div');
                            if ($replacediv.length) {
                                $replacediv.html($(page).find('.replace-div').html());
                            } else {
                                $('#roomsdiv').html(page);
                            }
                            //$('#roomsdiv').html(page);

                            $roomsDlg.dialog('open');
                        };
                        $('#roomsBtn').on('click', function () {
                            chatSession.listRooms();
                        });
                        chatSession.joinRoom('Lobby', null);
                        //chatSession.listRooms();
                        initStatusControls(chatSession, $('#statusSelect'));


                        var $ansDlg = $('#answeringmachinediv').dialog({
                            height: window.innerHeight * 0.7,
                            width: window.innerWidth * 0.7,
                            autoOpen: false,
                            modal: true,
                            close: function () {
                                delete chatSession.listeners['answeringmachine'];
                            }
                        });

                        var replyMsg = null;
                        var $replyMsgDlg = createDialog($('#replymessagediv'), function () {
                           
                            $ansDlg.dialog('open');
                            replyMsg = $('#replymessagediv').find('textarea').val();
                            $('#answeringmachinediv').find('textarea').first().text(replyMsg);
                            var $mailTextarea = $('#answeringmachinediv').find('div').last();
                            $mailTextarea.empty();
                            chatSession.listeners['answeringmachine'] = function (msg) {
                                if (msg.command.toLowerCase() === 'whisper') {
                                    if (msg.target && chatSession.username && msg.target.toLowerCase() === chatSession.username.toLowerCase()) {
                                        $('<div />').text(msg.source + ': ' + msg.message).appendTo($mailTextarea);
                                        chatSession.client.send(new Command('whisper').to(msg.source).in(msg.room).saying('(Auto-Reply)' + replyMsg));
                                    }
                                }
                            };
                        });

                        $('#ansBtn').on('click', function () {
                            $replyMsgDlg.dialog('open');
                        });
                        
                        chatSession.disconnectHandler = function() {
                            window.location.replace('web/login');
                          
                        }

                        chatSession.client.send(new Command('status').withargs('ONLINE'));
                    }());
        </script>
    </body>
</html>
