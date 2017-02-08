package com.cardshifter.gdx.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Base64Coder;
import com.cardshifter.api.CardshifterSerializationException;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.serial.ByteTransformer;
import com.cardshifter.gdx.CardshifterClient;
import com.cardshifter.gdx.CardshifterMessageHandler;
import com.cardshifter.gdx.GdxLogger;
import com.cardshifter.gdx.GdxReflection;
import com.sksamuel.gwt.websockets.BinaryWebsocketListener;
import com.sksamuel.gwt.websockets.Websocket;

import java.io.ByteArrayInputStream;

/**
 * Created by Simon on 4/25/2015.
 */
public class GWTClient implements CardshifterClient, BinaryWebsocketListener {

    private static final String TAG = "Websocket";
    private final Websocket websocket;
    private final CardshifterMessageHandler handler;
    private final ByteTransformer transformer;
    private final LoginMessage loginMessage;

    public GWTClient(String host, int port, CardshifterMessageHandler handler, LoginMessage loginMessage) {
        websocket = new Websocket(host + ":" + port);
        websocket.addListener(this);
        this.handler = handler;
        transformer = new ByteTransformer(new GdxLogger(), new GdxReflection());
        this.loginMessage = loginMessage;
        websocket.open();
    }

    @Override
    public void send(Message message) {
        try {
            byte[] data = transformer.transform(message);
            websocket.send(data);
        } catch (CardshifterSerializationException e) {
            Gdx.app.log(TAG, "Error sending " + message, e);
        }
    }

    @Override
    public void onClose() {
        Gdx.app.log(TAG, "Websocket closed");
    }

    @Override
    public void onMessage(String msg) {
        Gdx.app.log(TAG, "Websocket String: " + msg);
    }

    @Override
    public void onOpen() {
        Gdx.app.log(TAG, "Websocket opened");
        send(loginMessage);
  /*      websocket.send("{ \"command\": \"serial\", \"type\": \"1\" }");
        Gdx.app.log("Client", "Sent serial type");*/
        //platform.setupLogging();
    }

    @Override
    public void onMessage(byte[] bytes) {
//        Gdx.app.log(TAG, "Message: " + msg);
        try {
//            byte[] bytes = Base64Coder.decode(msg);
            Gdx.app.log(TAG, "Received " + bytes.length + " bytes");
            Message message = transformer.readOnce(new ByteArrayInputStream(bytes));
            Gdx.app.log(TAG, "Received " + message);
            handler.handle(message);
        } catch (CardshifterSerializationException e) {
            Gdx.app.log(TAG, "Read error", e);
        }
    }
}
