package im.tox.echobot;

import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxCoreConstants;
import im.tox.tox4j.core.callbacks.ToxCoreEventAdapter;
import im.tox.tox4j.core.callbacks.ToxCoreEventListener;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.exceptions.ToxFriendAddException;
import im.tox.tox4j.core.exceptions.ToxFriendSendMessageException;
import im.tox.tox4j.core.options.ProxyOptions;
import im.tox.tox4j.core.options.SaveDataOptions;
import im.tox.tox4j.core.options.ToxOptions;
import im.tox.tox4j.impl.jni.ToxCoreImpl;

public final class EchoBot implements AutoCloseable {
    private static final boolean ipv6Enabled = true;
    private static final boolean udpEnabled = true;
    private static final boolean localDiscoveryEnabled = true;
    private static final ProxyOptions proxy = ProxyOptions.None$.MODULE$;
    private static final int startPort = ToxCoreConstants.DefaultStartPort();
    private static final int endPort = ToxCoreConstants.DefaultEndPort();
    private static final int tcpPort = ToxCoreConstants.DefaultTcpPort();
    private static final SaveDataOptions saveData = SaveDataOptions.None$.MODULE$;
    private static final boolean fatalErrors = true;

    public static final ToxOptions DEFAULT_OPTIONS = new ToxOptions(
            ipv6Enabled, udpEnabled, localDiscoveryEnabled, proxy, startPort, endPort, tcpPort, saveData, fatalErrors);

    private boolean running = true;
    private final ToxCore tox = new ToxCoreImpl(DEFAULT_OPTIONS);

    private ToxCoreEventListener<Void> eventListener = new ToxCoreEventAdapter<Void>() {
        @Override
        public Void friendRequest(byte[] publicKey, int timeDelta, byte[] message, Void state) {
            try {
                tox.addFriendNorequest(publicKey);
            } catch (ToxFriendAddException e) {
                e.printStackTrace();
            }
            return state;
        }

        @Override
        public Void friendMessage(int friendNumber, ToxMessageType messageType, int timeDelta, byte[] message, Void state) {
            try {
                if (new String(message).equals("die")) {
                    tox.friendSendMessage(friendNumber, ToxMessageType.NORMAL, 0, "Goodbye".getBytes());
                    running = false;
                } else {
                    tox.friendSendMessage(friendNumber, messageType, timeDelta, message);
                }
            } catch (ToxFriendSendMessageException e) {
                e.printStackTrace();
            }
            return state;
        }
    };

    public byte[] getAddress() {
        return tox.getAddress();
    }

    public void start() throws InterruptedException {
        while (running) {
            Thread.sleep(tox.iterationInterval());
            tox.iterate(eventListener, null);
        }
    }

    public void close() {
        tox.close();
    }

    public static void main(String[] args) throws InterruptedException {
        try (EchoBot bot = new EchoBot()) {
            bot.start();
        }
    }
}
