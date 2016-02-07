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
import webchat.client.blocking.ChannelListener;

public class Agent {

    private final static Logger logger = LoggerFactory.getLogger(Agent.class);

    BlockingConnector chatSessionFactory;
    BlockingSession chatSession;
    FunctionProcessor funcProcessor = new FunctionProcessor();
    ScheduledExecutorService execService;
    AtomicReference<List<RoomInfo>> roomInfo = new AtomicReference<>(new LinkedList<>());
    AtomicReference<Throwable> exception = new AtomicReference<>();

    public Agent() {
        super();
    }

    public synchronized void start(ClientConfig config) throws IOException {

        logger.info("Starting agent: {}", config.userName);
        this.exception.set(null);
        Runnable listRoomPoller = () -> {
            try {
                roomInfo.set(chatSession.getAvailableChannels());
                logger.debug("{}: Received room list, {} entries", config.userName, roomInfo.get().size());
            } catch (IOException e) {
                logger.error("{}: Minor error polling for room list, continuing: {}", config.userName, e);
            } catch (Throwable t) {
                logger.error("{}: Major error polling for room list, stopping: {}", config.userName, t);
                exception.set(t);
                stop();
            }
        };

        logger.info("{}: Opening session to: {} ", config.userName, config.rootUrl());
        HttpChatSessionFactory csf = new HttpChatSessionFactory(config.commandUrl(), config.streamUrl());
        this.chatSessionFactory = new BlockingConnector(csf);
        this.chatSession = chatSessionFactory.connect(config.userName, config.userPass, config.connectTimeoutMs);
        this.chatSession.setTimeout(config.respTimeoutMs);
        
        logger.info("ChatSession opened. Starting list room poller");
        this.execService = Executors.newSingleThreadScheduledExecutor();
        this.execService.scheduleWithFixedDelay(listRoomPoller, 0, 10, TimeUnit.SECONDS);
        this.chatSession.addListener(new SessionListener() {
            public void onJoinChannel(BlockingChannel chan) {
                chan.addListener(new FunctionChannelListener(chan));
            }
        });
        
        
        logger.info("Room poller started. Joining Lobby");
        try {
            this.chatSession.openChannel("lobby");
        } catch (ChatException ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized void stop() {

        if (chatSession != null) {
            logger.info("Stopping agent {}", chatSession.getUsername());
            chatSession.disconnect();
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

    private class FunctionChannelListener implements ChannelListener {

        BlockingChannel chan;

        FunctionChannelListener(BlockingChannel chan) {
            this.chan = chan;
        }
        
        @Override
        public void onMessage(String src, String msg) {
            logger.debug("Received message: {} {} {}", src, chan.getName(), msg);
            funcProcessor.process(chan, src, msg, false);
        }

        @Override
        public void onWhisper(String src, String msg) {
            logger.debug("Received whisper: {} {} {}", src, chan.getName(), msg);
            funcProcessor.process(chan, src, msg, true);
        }
    };

    public static void main(String[] args) throws Exception {
        ClientConfig cc = new ClientConfig();
        cc.webAppPath = "/myapp";
        cc.userName = "YoYoMa";
        cc.userPass = "YoYoMa";
        Agent a = new Agent();
        a.start(cc);

    }

}
