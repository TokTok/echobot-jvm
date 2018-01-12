package im.tox.echobot;

import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.callbacks.ToxCoreEventAdapter;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.exceptions.ToxFriendAddException;
import im.tox.tox4j.core.exceptions.ToxFriendSendMessageException;
import im.tox.tox4j.impl.jni.ToxCoreImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

final class EchoBotTest {
    @Test
    void start() throws InterruptedException, ToxFriendAddException {
        try (ToxCore tox = new ToxCoreImpl(EchoBot.DEFAULT_OPTIONS);
             EchoBot bot = new EchoBot()) {
            // Start the bot in a separate thread.
            Thread botThread = new Thread(() -> {
                try {
                    bot.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            botThread.start();

            // Add the bot to the test tox instance.
            tox.addFriend(bot.getAddress(), "Hey, add me please!".getBytes());

            boolean running = true;
            while (running) {
                Thread.sleep(tox.iterationInterval());
                running = tox.iterate(new ToxCoreEventAdapter<Boolean>() {
                    @Override
                    public Boolean friendConnectionStatus(
                            int friendNumber,
                            ToxConnection connectionStatus,
                            Boolean running
                    ) {
                        assertEquals(0, friendNumber);
                        if (connectionStatus != ToxConnection.NONE) {
                            try {
                                tox.friendSendMessage(
                                        friendNumber,
                                        ToxMessageType.NORMAL,
                                        0,
                                        "Hello there!".getBytes()
                                );
                            } catch (ToxFriendSendMessageException e) {
                                fail(e);
                            }
                        }
                        return running;
                    }

                    @Override
                    public Boolean friendMessage(
                            int friendNumber,
                            ToxMessageType messageType,
                            int timeDelta,
                            byte[] message,
                            Boolean running
                    ) {
                        assertEquals(0, friendNumber);
                        assertEquals(ToxMessageType.NORMAL, messageType);
                        if (new String(message).equals("Hello there!")) {
                            try {
                                tox.friendSendMessage(
                                        friendNumber,
                                        ToxMessageType.NORMAL,
                                        0,
                                        "die".getBytes()
                                );
                            } catch (ToxFriendSendMessageException e) {
                                fail(e);
                            }
                            return false;
                        }
                        return running;
                    }
                }, true);
            }

            botThread.join();
        }
    }
}
