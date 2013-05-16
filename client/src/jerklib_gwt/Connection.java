package jerklib_gwt;


import java.util.ArrayList;
import java.util.List;

import jerklib_gwt.Session.State;
import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.IRCEvent.Type;
import jerklib_gwt.listeners.WriteRequestListener;


import net.zschech.gwt.websockets.client.CloseHandler;
import net.zschech.gwt.websockets.client.ErrorHandler;
import net.zschech.gwt.websockets.client.MessageEvent;
import net.zschech.gwt.websockets.client.MessageHandler;
import net.zschech.gwt.websockets.client.OpenHandler;
import net.zschech.gwt.websockets.client.WebSocket;

/**
 * A class for reading and writing to an IRC connection.
 * This class will also handle PING/PONG.
 * 
 * @author mohadib
 *
 */
class Connection implements MessageHandler, CloseHandler, ErrorHandler, OpenHandler
{
	/* PROXY ADDRESS */
	public static String PROXY_ADDR = "ws://127.0.0.1:9999/";
	
	/* WEB SOCKET */
	WebSocket webSocket;
	
	/* ConnectionManager for this Connection */
	private final ConnectionManager manager;

	/* A Buffer for write request */
	final List<WriteRequest> writeRequests = new ArrayList<WriteRequest>();

	/* indicates if an event fragment is waiting */
	private boolean gotFragment;

	/* buffer for event fragments */
	private final StringBuffer stringBuff = new StringBuffer();

	/* actual hostname connected to */
	private String actualHostName;
	
	/* Session Connection belongs to */
	private final Session session;

	/**
	 * @param manager
	 * @param socChannel - socket channel to read from
	 * @param session - Session this Connection belongs to
	 */
	Connection(ConnectionManager manager, Session session)
	{
		this.manager = manager;
		webSocket =WebSocket.create(PROXY_ADDR + session.getRequestedConnection().getHostName() + ":" + session.getRequestedConnection().getPort());
		webSocket.setOnMessage(this);
		webSocket.setOnClose(this);
		webSocket.setOnError(this);
		webSocket.setOnOpen(this);
		this.session = session;
	}

	/**
	 * Get profile use for this Connection
	 * 
	 * @return the Profile
	 */
	Profile getProfile()
	{
		return session.getRequestedConnection().getProfile();
	}

	/**
	 * Sets the actual host name of this Connection.
	 * @param name
	 */
	void setHostName(String name)
	{
		actualHostName = name;
	}

	/**
	 * Gets actual hostname for Connection.
	 * 
	 * @return hostname
	 */
	String getHostName()
	{
		return actualHostName;
	}

	/**
	 * Adds a listener to be notified of all data written via this Connection
	 * 
	 * @param request
	 */
	void addWriteRequest(WriteRequest request)
	{
		writeRequests.add(request);
		System.out.println("Add write request: "+request.getMessage());
		doWrites();
	}

	/**
	 * Called to finish the Connection Process
	 * 
	 * @return true if fincon is successfull
	 */
	boolean finishConnect()
	{
		webSocket.close();
		return true;
	}

	
	
	/**
	 * Writes all requests in queue to server
	 * 
	 * @return number bytes written
	 */
	long lastWrite = System.currentTimeMillis();
	int bursts=0;
	int maxBurst = 5;
	long nextWrite = -1;
	/*
	 *  if lastwrite was less than a second ago:
	 *  	if burst == limit return;
	 *  	else burst++; write packet; recordtime;
	 *  else
	 *  	burst = 0;
	 *  	write packet; record time;
	 * 
	 * 
	 */

	void doWrites()
	{
		if (webSocket.getReadyState() != WebSocket.OPEN) {
			 //Nothing to write
			writeRequests.clear();
			return;
		}
		
		if(writeRequests.isEmpty())
		{
			return;
		}
		
		WriteRequest req = writeRequests.remove(0);
		
		
		String data;
		if(req.getType() == WriteRequest.Type.CHANNEL_MSG)
		{
			if(req.getMessage().length() > 100)
			{
				writeRequests.add(0, new WriteRequest(req.getMessage().substring(100) , req.getChannel() , req.getSession()));
				data = "PRIVMSG " + req.getChannel().getName() + " :" + req.getMessage().substring(0,100) + "\r\n";
			}
			else
			{
				data = "PRIVMSG " + req.getChannel().getName() + " :" + req.getMessage() + "\r\n";
			}
		}
		else if(req.getType() == WriteRequest.Type.PRIVATE_MSG)
		{
			if(req.getMessage().length() > 255)
			{
				writeRequests.add(0, new WriteRequest(req.getMessage().substring(100) , req.getSession() , req.getNick()));
				data = "PRIVMSG " + req.getNick() + " :" + req.getMessage().substring(0,100) + "\r\n";
			}
			else
			{
				data = "PRIVMSG " + req.getNick() + " :" + req.getMessage() + "\r\n";
			}
		}
		else
		{
			data = req.getMessage();
			if (!data.endsWith("\r\n"))
			{
				data += "\r\n";
			}
		}
		
		webSocket.send(data);

		 if (session.getState() != State.DISCONNECTED) 
			 fireWriteEvent(req);

		//Recursively write remaining write requests
		doWrites();		
		
		return;
	}
	
	
	/**
	 * Send a ping
	 */
	void ping()
	{
		addWriteRequest(new WriteRequest("PING " + actualHostName + "\r\n", session));
		session.pingSent();
	}

	/**
	 * Send a pong
	 * 
	 * @param event , the Ping event
	 */
	void pong(IRCEvent event)
	{
		session.gotResponse();
		String data = event.getRawEventData().substring(event.getRawEventData().lastIndexOf(":") + 1);
		addWriteRequest(new WriteRequest("PONG " + data + "\r\n", session));
	}

	/**
	 * Alert connection a pong was received
	 */
	void gotPong()
	{
		session.gotResponse();
	}

	/**
	 * Close connection
	 * 
	 * @param quitMessage
	 */
	void quit(String quitMessage)
	{
		if (quitMessage == null) quitMessage = "";
		WriteRequest request = new WriteRequest("QUIT :" + quitMessage + "\r\n", session);
		addWriteRequest(request);
		// clear out write queue
		doWrites();
		this.finishConnect();
	}

	/**
	 * Fires a write request to all write listeners
	 * 
	 * @param request
	 */
	void fireWriteEvent(WriteRequest request)
	{
		for (WriteRequestListener listener : manager.getWriteListeners())
		{
			listener.receiveEvent(request);
		}
	}
	
	/**
	 * Reads from connection and creates default IRCEvents that 
	 * are added to the ConnectionManager for relaying
	 * 
	 * @return bytes read
	 */
	@Override
	public void onMessage(WebSocket webSocket, MessageEvent event) {

		String tmpStr = event.getData();

		// read did not contain a \r\n
		if (tmpStr.indexOf("\r\n") == -1)
		{
			// append whole thing to buffer and set fragment flag
			stringBuff.append(tmpStr);
			gotFragment = true;
			return;
		}

		// this read had a \r\n in it

		if (gotFragment)
		{
			// prepend fragment to front of current message
			tmpStr = stringBuff.toString() + tmpStr;
			stringBuff.delete(0, stringBuff.length());
			gotFragment = false;
		}

		String[] strSplit = tmpStr.split("\r\n");

		for (int i = 0; i < (strSplit.length - 1); i++)
		{
			manager.addToEventQueue(new IRCEvent(strSplit[i],session,Type.DEFAULT));
		}

		String last = strSplit[strSplit.length - 1];

		if (!tmpStr.endsWith("\r\n"))
		{
			// since string did not end with \r\n we need to
			// append the last element in strSplit to a stringbuffer
			// for next read and set flag to indicate we have a fragment waiting
			stringBuff.append(last);
			gotFragment = true;
		}
		else
		{
			manager.addToEventQueue(new IRCEvent(last , session , Type.DEFAULT));
		}
		
		manager.updateState();
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		session.login();
	}

	@Override
	public void onError(WebSocket webSocket) {
		session.disconnected(new Exception("Websocket closed by error"));
		manager.updateState();
	}

	@Override
	public void onClose(WebSocket webSocket) {
		session.disconnected(new Exception("Websocket closed"));
		manager.updateState();
	}
}
