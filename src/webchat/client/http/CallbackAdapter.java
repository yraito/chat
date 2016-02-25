/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.http;

import com.google.common.util.concurrent.FutureCallback;
import webchat.core.*;
import webchat.client.*;

/**
 *
 * @author Nick
 */
public class CallbackAdapter<V> implements FutureCallback<V>{

    ChatCallback cmdCallback;

    public CallbackAdapter(ChatCallback cmdCallback) {
        this.cmdCallback = cmdCallback;
    }
    
    @Override
    public void onSuccess(V v) {
        cmdCallback.onCompleted(v);
    }

    @Override
    public void onFailure(Throwable thrwbl) {
       cmdCallback.onError(thrwbl);
    }

}
