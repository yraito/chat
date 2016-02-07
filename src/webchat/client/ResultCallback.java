package webchat.client;

public interface ResultCallback {

    void onSuccess(Object rslt);
    
    void onError(String errMsg);
}
