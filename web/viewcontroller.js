function ViewController(client, view) {
	this.targetUser = null;
	this.client = client;
	this.view = view;
	this.$whisperdiv = null;
	this.$kickdiv = null;
	this.$destroydiv = null;
        this.initX = null;
        this.initY = null;
        
	this.showWhisperDialog = function() {
		var $formdiv = this.$kickdiv;
		var target = this.targetUser;
		var room = this.view.roomName;
		
		$formdiv.attr('title', 'Whisper @' + target);
		var whisper = function() {
			var message = $formdiv.find('.textinput').val();
			var whisperCmd = new Command('whisper').in(room).to(target).saying(message);
			this.client.send(whisperCmd)
				.done(function() {
					this.view.displayMessage(this.view.userName, message, true, true);
				})
				.fail(function(errMsg) {
					this.view.displayError(errMsg);
				});
		};
		showDialog($formdiv, whisper);
	};

	this.showKickDialog = function() {
		var $formdiv = this.$kickdiv;
		var target = this.targetUser;
		var room = this.view.roomName;
		//alert('kicking');
		$formdiv.attr('title', 'Kick ' + target);
		var kick = function() {
			var message = $formdiv.find('.textinput').val();
			var kickCmd = new Command('kick').in(room).to(target).saying(message);
			this.client.send(kickCmd)
				.done(function() {
					alert(target + ' kicked');
				})
				.fail(function(errMsg) {
					this.view.displayError(errMsg);
				});
		};
		showDialog($formdiv, kick);

	};

	this.showDestroyDialog = function($formdiv) {
		var room = this.view.roomName;
		
		$formdiv.attr('title', 'Destroy ' + room);
		var destroy = function() {
			var message = $formdiv.find('.textinput').val();
			var destroyCmd = new Command('destroy').in(room).saying(message);
			this.client.send(destroyCmd)
				.done(function() {
					this.view.close(this.view.userName, message);
				})
				.fail(function(errMsg) {
					this.view.displayError(errMsg);
				});
		};
		showDialog($formdiv, destroy);
	};

	this.initUserMenu = function($userMenu, $userTable) {
                
		var vc = this;

		//SO
		//Show user menu on click table row, and set targetUsr 
		$userTable.bind('click', function(e) {
			//Only if clicked on table row
			if ($(e.target).parents('tr').length > 0) {
				e.preventDefault();
				vc.targetUser = $(e.target).parents('tr').find('td.name').text();
				//alert(this.targetUsr);
				//var x = e.pageX - $userTable.offset().left;
				//var y = e.pageY - $userTable.offset().top;
				$userMenu.finish().toggle(100)
				   .css({
	       				 top: e.pageY + "px",
	        		     left: e.pageX + "px"
	    		});
			}
		});

		//Hide user menu on click elsewhere
		$(document).bind("mousedown", function (e) {
		    
		    if (!$(e.target).parents('.usermenudiv').length > 0) {
		    	vc.targetUser = null;
		        $userMenu.hide(100);
		    }
		});


		//Execute command on click menu item
		$userMenu.find('li').click(function(e){
			//alert('click');
		    switch($(this).attr("data-action")) {
		        case "whisper": 
		        	vc.showWhisperDialog(vc.$whisperdiv);
		        	break;
		        case "kick": 
		        	vc.showKickDialog(vc.$kickdiv);
		        	break;
		        case "grant":
		        	var grantCmd = new Command('grant').to(vc.targetUser).in(vc.view.roomName);
		        	vc.client.send(grantCmd).fail(function(msg) {
	        			vc.view.displayError(msg);
	        		});
		        	break;
	        	case "revoke":
	        		var revokeCmd = new Command('revoke').to(vc.targetUser).in(vc.view.roomName);
	        		vc.client.send(revokeCmd).fail(function(msg) {
	        			vc.view.displayError(msg);
	        		});
	        		break;
		    }
		    $userMenu.hide(100);
		    alert('post');
		  });
	};

	this.initEmoMenu = function($emoSpan, $emoButton, $msgPanel, $textArea) {
            $emoButton.on('click', function(e) {
                e.preventDefault();
                layoutEmoticons($emoSpan, $msgPanel);
            });
            
            $(document).bind("mousedown", function (e) {	    
		    if (! ($(e.target).parents('.emomenudiv').length > 0) ){
		        $emoSpan.hide(100);
		    }
            });

            $emoSpan.find('img').on('click', function(e) {
                $textArea.val($textArea.val() + ' [' + $(this).attr('data-code') + ']');
                $emoSpan.hide(100);
            });
	};

	this.initSendControls = function($sendButton, $sendTextarea) {
                alert($sendButton);
		$sendButton.on('click', function() {
			alert('hgh');
			var msgToSend = $sendTextarea.val();
			var msgCommand = new Command('message').in(view.roomName).saying(msgToSend);
			client.send(msgCommand)
				.done(function() {
					view.displayMessage(view.userName, msgToSend, false, true);
				})
				.fail(function(errMsg) {
					view.displayError(errMsg);
				});
			$sendTextarea.val('');
		});
	};

	this.initStatusControls = function($statusSelect) {
		$statusSelect.on('change', function() {
			var newStatus = $statusSelect.val();
			alert($statusSelect.val());
			var statusCmd = new Command('status').withargs(newStatus);
			client.send(statusCmd).fail(function(errMsg) {
				view.displayError('STATUS' + errMsg);
			});
		});
	};

	this.initRoomControls = function($leaveBtn, $destroyBtn, closehandler) {
               $leaveBtn.on('click', function() {
                   var leaveCmd = new Command('leave').in(view.roomName);
                   client.send(leaveCmd).fail(function(err) {
                       alert(err);
                   }).done(function() {
                       closehandler();
                   });
               });
                $destroyBtn.on('click', function() {
                   var destroyCmd = new Command('destroy').in(view.roomName);
                   client.send(destroyCmd).fail(function(err) {
                       alert(err);
                   }).done(function() {
                       closehandler();
                   });
               });
	};



}

	var showDialog = function($form, btnfunc) {
		  var dialog, form;
		  dialog = $form.dialog({
		      autoOpen: false,
		      height: 150,
		      width: 350,
		      modal: true,
		      close: function() {
		        form[ 0 ].reset();
		        //allFields.removeClass( "ui-state-error" );
		      }
	    });
	 
	    form = dialog.find( "form" ).on( "submit", function( event ) {
	      event.preventDefault();
	      btnfunc();
	      dialog.dialog("close");
	    });

	     dialog.dialog( "open" );
	};
        
        var layoutEmoticons = function($emoSpan, $msgPanel) {
            
            var initX = $msgPanel.width() + $msgPanel.position().left;
            var initY = $msgPanel.height() + $msgPanel.position().top;
            //initY = Math.floor(window.innerHeight*0.92);
            var imgSize = 40;
            var numImgs = $emoSpan.find('img').length;
            var viewRatio = window.innerWidth / window.innerHeight;
            
           // alert(viewRatio);
            var width = Math.sqrt(numImgs*viewRatio);
            width = Math.floor(width)*imgSize;
            width = Math.min(width, window.innerWidth);
            var height = Math.ceil(width / viewRatio);
            var left = Math.max(0, initX - width);
            var top = Math.max(0, initY - height);
            $emoSpan.finish().toggle(100).css( {
                position: "absolute",
                left: left,
                top: top,
                width: width + "px",
                "max-width": width + "px",
                height : height + "pxs"
            });
           // alert(width + " " + height + " " + left + " " + top);
	};
        