package webchat.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;

import webchat.client.ChatHandler;
import webchat.client.ChatSession;
import webchat.client.ChatSessionFactory;

public class HttpChatSessionFactory implements ChatSessionFactory {

    String streamUrl;
    String commandUrl;


    public HttpChatSessionFactory(String commandUrl, String streamUrl) {
        this.streamUrl = streamUrl;
        this.commandUrl = commandUrl;
    }

    @Override
    public Future<ChatSession> open(String userName, String password, ChatHandler ioh) throws IOException{

        try (CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault()) {
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setCookieStore(new BasicCookieStore());
            httpClient.start();
            HttpPost loginReq = new HttpPost(commandUrl);
            LinkedList<NameValuePair> paramLst = new LinkedList<>();
            paramLst.add(new BasicNameValuePair("args", userName));
            paramLst.add(new BasicNameValuePair("args", password));
            loginReq.setEntity(new UrlEncodedFormEntity(paramLst));
            FutureCallback<HttpResponse> callback = null; //TODO
            final Future<HttpResponse> futureResp = httpClient.execute(loginReq, localContext, null);
            return new ChatSessionFuture(futureResp, ioh, localContext);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private class ChatSessionFuture extends HttpWrappingFuture<ChatSession> {

        ChatHandler chatHandler;
        HttpClientContext httpContext;

        ChatSessionFuture(Future<HttpResponse> inner, ChatHandler chatHandler, HttpClientContext httpContext) {
            super(inner, ChatSession.class);
            this.chatHandler = chatHandler;
            this.httpContext = httpContext;
        }

        @Override
        protected ChatSession convert(InputStream is, HttpResponse hr) throws IOException {

            HttpOutputChatChannel httpOut = new HttpOutputChatChannel(httpContext, commandUrl);
            HttpInputStreamReceiver httpIn = new HttpInputStreamReceiver(httpContext, streamUrl, httpOut, chatHandler);
            httpIn.start();
            return httpOut;
        }
    }
    
}
