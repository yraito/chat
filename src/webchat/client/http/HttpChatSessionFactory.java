package webchat.client.http;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;

import webchat.servlet.api.Formatter;
import webchat.core.*;
import webchat.core.command.*;
import webchat.client.*;

import static webchat.util.Util.*;

public class HttpChatSessionFactory implements ChatSessionFactory {

    String streamUrl;
    String commandUrl;
    Formatter formatter;
    ListeningExecutorService execService;

    public HttpChatSessionFactory(String commandUrl, String streamUrl, Formatter formatter) {
        this.streamUrl = streamUrl;
        this.commandUrl = commandUrl;
        this.formatter = formatter;
    }

    @Override
    public ChatFuture<ChatSession> open(String userName, String password, ChatHandler ioh) throws IOException {
        HttpClientContext httpContext = HttpClientContext.create();
        httpContext.setCookieStore(new BasicCookieStore());
        HttpMessageSender sender = new HttpMessageSender(httpContext, commandUrl, formatter);
        ChatFuture<ResultMessage> futureCmd = sender.writeFuture(new LoginCommand(userName, password));
        SettableFuture<ChatSession> futureSession = SettableFuture.create();
        futureCmd.addCallback(new ChatCallback<ResultMessage>() {

            @Override
            public void onCompleted(ResultMessage rm) {
                HttpChatSession chatSess = null;
                HttpMessageReceiver receiver = null;
                try {
                    if (rm.isError()) {
                        futureSession.setException(new ChatException(rm.getError()));
                    } else {
                        receiver = new HttpMessageReceiver(httpContext, streamUrl, formatter);
                        chatSess = new HttpChatSession(sender, receiver, ioh);
                        chatSess.start();
                        futureSession.set(chatSess);
                        ioh.onSessionOpened(chatSess);
                    }
                } catch (Throwable t) {
                    futureSession.setException(t);
                    closeQuietly(chatSess);
                    closeQuietly(receiver);
                    t.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {
                futureSession.setException(t);
                closeQuietly(sender);
                t.printStackTrace();
            }
        });

        return new FutureAdapter(futureSession);
    }

}
