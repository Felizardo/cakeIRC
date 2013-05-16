package jerklib_gwt.examples;

import jerklib_gwt.ConnectionManager;
import jerklib_gwt.Profile;
import jerklib_gwt.Session;
import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.JoinCompleteEvent;
import jerklib_gwt.events.MessageEvent;
import jerklib_gwt.events.IRCEvent.Type;
import jerklib_gwt.listeners.IRCEventListener;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;



public class JerkTest extends Composite implements HasText, EntryPoint {

	private ConnectionManager manager;
	
	private static WsTestUiBinder uiBinder = GWT.create(WsTestUiBinder.class);
	@UiField TextArea status;
	@UiField TextBox cmd;
	@UiField Button sendBtn;
	
	Session session = null;
	
	interface WsTestUiBinder extends UiBinder<Widget, JerkTest> {
	}

	public JerkTest() {
		initWidget(uiBinder.createAndBindUi(this));

		/*
		 * ConnectionManager takes a Profile to use for new connections.
		 */
		manager = new ConnectionManager(new Profile("scripy"));
		
		
		sendBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//statusEcho("Conectando...");
				if (session == null || !session.isConnected()) {
					/*
					 * One instance of ConnectionManager can connect to many IRC networks.
					 * ConnectionManager#requestConnection(String) will return a Session object.
					 * The Session is the main way users will interact with this library and IRC
					 * networks
					 */
					session = manager.requestConnection("irc.freenode.net");

					/*
					 * JerkLib fires IRCEvents to notify users of the lib of incoming events
					 * from a connected IRC server.
					 */
					session.addIRCEventListener(new IRCEventListener() {

						@Override
						/*
						 * This method is for implementing an IRCEventListener. This method will be
						 * called anytime Jerklib parses an event from the Session its attached to.
						 * All events are sent as IRCEvents. You can check its actual type and cast it
						 * to a more specific type.
						 */
						public void receiveEvent(IRCEvent e)
						{


							if (e.getType() == Type.CONNECT_COMPLETE)
							{
								//e.getSession().join("#jerklib");
								statusEcho("CONNECTED!!!");
							}
							else if (e.getType() == Type.CONNECTION_LOST) 
							{
								statusEcho("CONNECTION LOST :(!!!");
							}
							else if (e.getType() == Type.CHANNEL_MESSAGE)
							{
								MessageEvent me = (MessageEvent) e;
								statusEcho(me.getNick() + ":" + me.getMessage());
								//me.getChannel().say("Modes :" + me.getChannel().getUsersModes(me.getNick()).toString());
							}
							else if (e.getType() == Type.JOIN_COMPLETE)
							{
								JoinCompleteEvent jce = (JoinCompleteEvent) e;
								/* say hello */
								jce.getChannel().say("Hello from Jerklib!");
							}
							else
							{
								statusEcho(e.getType() + " " + e.getRawEventData());
							}
						}

					});
			
				} else {
					session.sayRaw(cmd.getText());
					statusEcho(">>> "+cmd.getText());
					cmd.setText("");
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

	@Override
	public void onModuleLoad() {
		JerkTest test = new JerkTest();
		RootPanel.get().add(test);
	}
    
    
}
