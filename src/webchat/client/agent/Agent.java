package webchat.client.agent;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import webchat.client.blocking.*;
import webchat.client.http.HttpChannelFactory;
import webchat.core.*;
import webchat.client.blocking.RoomListener;
import webchat.client.function.CreateFunctionHandler;
import webchat.client.function.HelpFunctionHandler;
import webchat.client.function.JoinFunctionHandler;
import webchat.client.function.WeatherFunctionHandler;

public class Agent {

    private final static Logger logger = LoggerFactory.getLogger(Agent.class);

    BlockingConnector chatSessionFactory;
    BlockingSession chatSession;
    FunctionProcessor funcProcessor = new FunctionProcessor();
    ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor();
    AtomicReference<List<RoomInfo>> roomInfo = new AtomicReference<>(new LinkedList<>());
    AtomicReference<Throwable> exception = new AtomicReference<>();

    public Agent() {

        funcProcessor.addHandler(new JoinFunctionHandler());
        funcProcessor.addHandler(new CreateFunctionHandler());
        funcProcessor.addHandler(new WeatherFunctionHandler());
        funcProcessor.addHandler(new HelpFunctionHandler(funcProcessor)); //Escaped this

    }

    public synchronized void start(ClientConfig config) throws IOException, ChatException {

        logger.info("Starting agent: {}", config.userName);
        this.exception.set(null);
        Runnable listRoomPoller = () -> {
            try {
                roomInfo.set(chatSession.getRoomList());
                logger.debug("{}: Received room list, {} entries", config.userName, roomInfo.get().size());
            } catch (IOException | ChatException e) {
                logger.error("{}: Minor error polling for room list, continuing: {}", config.userName, e);
            } catch (Throwable t) {
                logger.error("{}: Major error polling for room list, stopping: {}", config.userName, t);
                exception.set(t);
                stop();
            }
        };

        Runnable loginJoinPoller = () -> {
            try {
                logger.info("Logging in with credentials {}; {}; {}", config.userName, config.userPass, config.userUuid);
                this.chatSession.login(config.userName, config.userPass, config.userUuid);
                
                logger.info("Joining Lobby");
                this.chatSession.joinRoom("Lobby");
            } catch (IOException | ChatException e) {
                logger.error(e.getMessage());
            }
        };
        
        
        RetryStrategy basicRetry = (cmdMsg, tries) -> {
            boolean login = cmdMsg.getCommand().equalsIgnoreCase("login");
            boolean join = cmdMsg.getCommand().equalsIgnoreCase("join");
            if (login || join && tries <= 5) {
                return 1000;
            } else {
                return -1;
            }
        };

        try {
            logger.info("{}: Opening session to: {} ", config.userName, config.rootUrl());
            HttpChannelFactory csf = new HttpChannelFactory(config.commandUrl(), config.streamUrl(), config.formatter);
            this.chatSessionFactory = new BlockingConnector(csf);
            this.chatSession = chatSessionFactory.open();
            this.chatSession.setRetryStrategy(basicRetry);

            loginJoinPoller.run();
            this.execService.scheduleWithFixedDelay(loginJoinPoller, 2, 2, TimeUnit.MINUTES);
            
            logger.info("ChatSession opened and logged in. Starting list room poller");
            this.execService = Executors.newSingleThreadScheduledExecutor();
            this.execService.scheduleWithFixedDelay(listRoomPoller, 0, 30, TimeUnit.SECONDS);
            this.chatSession.getEventManager().addListener(new FunctionRoomListener(chatSession.getUsername()));

        } catch (IOException e) {
            throw e;
        }

    }

    public synchronized void stop() {

        if (chatSession != null) {
            logger.info("Stopping agent {}", chatSession.getUsername());
            chatSession.close();
        }

        try {
            execService.shutdownNow();
            if (!execService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                //error
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public Throwable getException() {
        return exception.get();
    }

    public boolean isRunning() {
        return execService.isShutdown();
    }

    public void addFunction(FunctionHandler af) {
        funcProcessor.addHandler(af);
    }

    public void addFunctions(Collection<FunctionHandler> afs) {
        funcProcessor.addHandlers(afs);
    }
    //auto get new session
    //add trigger
    // 

    private class FunctionRoomListener extends RoomListener {

        String username;
        String prefix;

        FunctionRoomListener(String username) {
            this.username = username.toLowerCase();
            prefix = "\\W*?" + username + "\\W*+";
        }


        @Override
        public void onMessage(BlockingRoom br, String src, String msg) {
            logger.debug("Received message: {} {} {}", src, br.getName(), msg);
            msg = msg.toLowerCase();
            if (msg.matches(prefix + ".*")) {
                logger.debug("Message addressed to self username, processing");
                msg = msg.replaceFirst(prefix, "");
                funcProcessor.process(br, src, msg, false);

            } else {
                logger.debug("Message not addressed, ignoring");
            }

        }

        @Override
        public void onWhisper(BlockingRoom br, String src, String msg) {
            logger.debug("Received whisper: {} {} {}", src, br.getName(), msg);
            funcProcessor.process(br, src, msg, true);
        }
    };

    public static void main(String[] args) throws Exception {
        ClientConfig cc = new ClientConfig();
        cc.webAppPath = "/MyChat";
        cc.userName = "bradybot0";
        cc.userPass = "bradybot0";
        Agent a = new Agent();
        a.start(cc);
        
        /*String pre = "\\W*?bradybot0\\W*+";
        BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = rdr.readLine();
            System.out.println(line.matches(pre+".*"));
            System.out.println(line.replaceFirst(pre, ""));
        }*/
        

    }
}
