
	
	var postMimeType = 'application/x-www-form-urlencoded';

	function Command(command) {

		this.command = command;
		this.target = null;
		this.room = null;
		this.message = null;
		this.args = null;
		this.source = null;
		
		this.to = function(target) {
			this.target = target;
			return this;
		};

                this.from = function(source) {
                    this.source = source;
                    return this;
                };
                
		this.in = function(room) {
			this.room = room;
			return this;
		};

		this.saying = function(message) {
			this.message = message;
			return this;
		};

		this.withargs = function() {
			if (arguments.length !== 0) {
				this.args = [];
				for (var j = 0; j < arguments.length; j++) {
                                        if (arguments[j] !== null) {
                                            this.args.push(arguments[j]);
                                        }
					
				}
			}
			return this;
		};
	} 

	function parseCommands(xml) {
		var cmdArray = [];
                $(xml).find('command').each(function(index, elem) {
                    var $cmd = $(this);
                    var command = $cmd.attr('command');
                    var room = $cmd.find('room').text();
                    var target = $cmd.find('target').text();
                    var source = $cmd.find('source').text();
                    var message = $cmd.find('message').text();
                    var $args = $cmd.find('arg');
                    var args = [];
                    $args.each(function(index, elem) {
                        args.push($(this).text());
                    });
                    var parsedCmd = new Command(command).
                            in(room).
                            to(target).
                            from(source).
                            saying(message).
                            withargs(args);
                    cmdArray.push(parsedCmd);
                });
                return cmdArray;
	}
        


	function Client(commandUrl, streamUrl) {

		this.commandUrl = commandUrl;
		this.streamUrl = streamUrl;
		this.streamhandler = null;
		this.streamerrhandler = null;
		this.defaultdone = null;
		this.defaultfail = null;
		this.started = false;
		this.mininterval = 2000;
		this.lastpoll = 0;

		this.send = function(command, time) {
			alert('send0');
			var jqxhr = $.ajax( {
				url: commandUrl,
				type: 'GET',
				data: command,
				dataType: 'xml',
				timeout: time ? time: 10000,
				beforeSend: function(xhr) {
					xhr.overrideMimeType(postMimeType);
				}
 			});

			var cmdprom =new CommandPromise(jqxhr);
			if (this.defaultdone) {
				cmdprom.done(this.defaultdone);
			}
			if (this.defaultfail) {
				cmdprom.fail(this.defaultfail);
			}
			alert('send');
			return cmdprom;

		};
                

		this.startread = function(streamhandler, streamerrhandler) {
			this.streamhandler = streamhandler;
			this.streamerrhandler = streamerrhandler;
			this.started = true;
			this.poll(this);
		};

		this.stopread = function() {
			this.started = false;
			this.streamhandler = null;
			this.streamerrhandler = null;
		};


		this.nextpoll = function(c) {
				if (!c.started) {
					return;
				}
			 	var t = (new Date()).getTime();
 				var deltat = t - c.lastpoll;
 				if (deltat < c.mininterval) {
 					setTimeout( function() {
 						t = (new Date()).getTime();
 						c.lastpoll = t;
 						c.poll(c);
 					}, c.mininterval - deltat);
 				} else {
 					 	c.lastpoll = t;
 						c.poll(c);
 				}
		};

		this.poll = function(c) {
			alert('poll');
			var jqxhr = $.ajax( {
				url: this.streamUrl,
				type: 'GET',
				dataType: 'xml',
				timeout: 60000
 			});
 			alert('poll2');
 			
 			var sf = function(xml) {
 				alert('sf' + this.streamUrl);
 				var cmds = parseCommands(xml);
 				if (c.streamhandler) {
 					for (var j = 0; cmds !== null && j < cmds.length; j++) {
 						c.streamhandler(cmds[j]);
 					}
 				}
 				c.nextpoll(c);
 			};

 			var ff = function(req, stat, errmsg) {
 				if (c.streamerrhandler) {
 					alert('this: ' );
 					c.streamerrhandler(req, stat, errmsg);
 				}
 				alert('huh');
 				c.nextpoll(c);
 			};

 			//var cf = function() {c.poll(c);}
 			//setTimeout(cf, 5000);
 			jqxhr.done(sf);
 			jqxhr.fail(ff);
		}; 

	} 


	function Result(xml) {


		this.xml = xml;

		this.succeeded = function() {
			var status = xml.getElementsByTagName('result')[0].getAttribute('status');
			if (status.toUpperCase() === 'SUCCESS') {
				return true;
			} else {
				return false;
			}
		};

		this.getBody = function() {
			if (this.succeeded()) {
				var content = xml.getElementsByTagName('content')[0];
				return content;
			} else {
				var errmsg = xml.getElementsByTagName('error')[0];
				return errmsg.textContent;
			}
		};
 	}

 	 function CommandPromise(jqxhr) {

 		this.jqxhr = jqxhr;

 		this.done = function(callback) {
 			alert('done');
 			var f = function(xml) {
 				var rslt = new Result(xml);
 				if (rslt.succeeded()) {
 					callback(rslt.getBody());
 				}
 			};
 			this.jqxhr.done(f);
 			return this;
 		};

 		this.fail = function(callback) {
 			alert('fail');
 			var f0 = function(xml) {
 				var rslt = new Result(xml);
 				if (!rslt.succeeded()) {
 					callback(rslt.getBody());
 				}
 			};
 			var f1 = function(req) {
 				callback(req.status + ': ' + req.statusText);
 			};
 			this.jqxhr.done(f0).fail(f1);
 			return this;
 		};
 	} 




