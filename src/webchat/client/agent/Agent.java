package webchat.client.agent;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.client.blocking.*;
import webchat.client.http.HttpChatSessionFactory;
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
    ScheduledExecutorService execService;
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

        logger.info("{}: Opening session to: {} ", config.userName, config.rootUrl());
        HttpChatSessionFactory csf = new HttpChatSessionFactory(config.commandUrl(), config.streamUrl(), config.formatter);
        this.chatSessionFactory = new BlockingConnector(csf);
        this.chatSession = chatSessionFactory.connect(config.userName, config.userPass);
        
        logger.info("ChatSession opened. Starting list room poller");
        this.execService = Executors.newSingleThreadScheduledExecutor();
        this.execService.scheduleWithFixedDelay(listRoomPoller, 0, 20, TimeUnit.SECONDS);
        this.chatSession.getEventManager().addListener(new FunctionRoomListener());
        
        logger.info("Room poller started. Joining Lobby");
        try {
            this.chatSession.joinRoom("lobby");
        } catch (ChatException ex) {
            throw ex;
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

       
        @Override
        public void onMessage(BlockingRoom br, String src, String msg) {
            logger.debug("Received message: {} {} {}", src, br.getName(), msg);
            funcProcessor.process(br, src, msg, false);
        }

        @Override
        public void onWhisper(BlockingRoom br, String src, String msg) {
            logger.debug("Received whisper: {} {} {}", src, br.getName(), msg);
            funcProcessor.process(br, src, msg, true);
        }
    };

    public static void main(String[] args) throws Exception {
        ClientConfig cc = new ClientConfig();
        cc.webAppPath = "/Chat2";
        cc.userName = "tombrady";
        cc.userPass = "TomBrady";
        Agent a = new Agent();
        a.start(cc);

    }
}
