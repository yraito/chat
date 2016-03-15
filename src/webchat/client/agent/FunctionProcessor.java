package webchat.client.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import webchat.client.blocking.BlockingRoom;
import webchat.client.function.CreateFunctionHandler;
import webchat.client.function.HelpFunctionHandler;
import webchat.client.function.JoinFunctionHandler;
import webchat.util.StringUtils;

public class FunctionProcessor {

    private Map<String, FunctionHandler> handlers = new ConcurrentHashMap<>();

    public FunctionProcessor(Map<String, FunctionHandler> handlers) {
        this.handlers.putAll(handlers);
    }

    public FunctionProcessor() {

    }

    public void addHandler(FunctionHandler fh) {
        handlers.put(fh.getName().toLowerCase(), fh);
    }

    public void addHandlers(Collection<FunctionHandler> fhs) {
        for (FunctionHandler fh : fhs) {
            handlers.put(fh.getName().toLowerCase(), fh);
        }
    }

    public Collection<FunctionHandler> getHandlers() {
        return Collections.unmodifiableCollection(handlers.values());
    }

    public void process(BlockingRoom chan, String src, String msg, boolean whisper)  {

        for (FunctionHandler fh : handlers.values()) {
            List<String> args = fh.getMatcher().match(msg);
            if (args != null) {
                String[] ss = args.toArray(new String[args.size()]);
                try {
                    Object o = fh.invoke(chan, ss);
                    respond(chan, src, o, whisper);
                } catch (Exception e) {
                   
                    respond(chan, src, "Oops, an error. " + e.getMessage(), whisper);
                    e.printStackTrace();
                }
                return;
            }
        }
        respond(chan, src, "Sorry, I don't understand your request", whisper);
    }

    private void respond(BlockingRoom chan, String src, Object response, boolean whisper) {
        if (response == null) {
            return;
        }
        try {
            if (whisper) {
                chan.sendWhisper(src, response.toString());
            } else {
                chan.sendMessage(response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FuncInvoc parse(String msg) {
        String msg1 = msg.trim().toLowerCase();
        String[] tkns = StringUtils.splitQuoted(msg1);
        if (tkns.length == 0 || msg1.startsWith("!")) {
            return null;
        } else if (tkns.length == 1) {
            return new FuncInvoc(tkns[0].substring(1), new String[0]);
        } else {
            String[] argtkns = Arrays.copyOfRange(tkns, 1, tkns.length);
            return new FuncInvoc(tkns[0].substring(1), argtkns);
        }
    }

    private static class FuncInvoc {

        FuncInvoc(String funcName, String[] args) {
            this.funcName = funcName;
            this.args = args;
        }

        String funcName;
        String[] args;
    }

}
