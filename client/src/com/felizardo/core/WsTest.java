package com.felizardo.core;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;

import net.zschech.gwt.websockets.client.CloseHandler;
import net.zschech.gwt.websockets.client.ErrorHandler;
import net.zschech.gwt.websockets.client.MessageEvent;
import net.zschech.gwt.websockets.client.MessageHandler;
import net.zschech.gwt.websockets.client.OpenHandler;
import net.zschech.gwt.websockets.client.WebSocket;

public class WsTest extends Composite implements HasText {

	private WebSocket webSocket;
    private boolean isConnected=false;
	private static WsTestUiBinder uiBinder = GWT.create(WsTestUiBinder.class);
	@UiField TextArea status;
	@UiField TextBox cmd;
	@UiField Button sendBtn;

	interface WsTestUiBinder extends UiBinder<Widget, WsTest> {
	}

	public WsTest() {
		initWidget(uiBinder.createAndBindUi(this));
		sendBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (!isConnected) {
					webSocket =WebSocket.create("ws://127.0.0.1:9000/ws");
					statusEcho("Trying to connect...");
					//On Open...
					webSocket.setOnOpen(new OpenHandler() {
						
						@Override
						public void onOpen(WebSocket webSocket) {
							statusEcho("CONNECTED!!!");
							isConnected=true;
						}
					});
					
					//On Close...
					webSocket.setOnClose(new CloseHandler() {
						
						@Override
						public void onClose(WebSocket webSocket) {
							statusEcho("CLOSED!!!");
							isConnected=false;
						}
					});
					
					//On Error...
					webSocket.setOnError(new ErrorHandler() {
						
						@Override
						public void onError(WebSocket webSocket) {
							statusEcho("ERROR!!!");
							isConnected=false;
						}
					});
					
					//On Message
					webSocket.setOnMessage(new MessageHandler() {

						@Override
						public void onMessage(WebSocket webSocket,
								MessageEvent event) {
							statusEcho("SERVER> "+ event.getData());
						}
						
					});
				} else {
					webSocket.send(cmd.getText());
					statusEcho("SENT> "+cmd.getText());
				}
			}
		});
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setText(String text) {
		// TODO Auto-generated method stub
		
	}
    private void statusEcho(String text) {
    	status.setText(status.getText() + text + "\n");
    }
}
