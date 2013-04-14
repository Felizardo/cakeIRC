package com.felizardo.jerklib;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;

import com.felizardo.jerklib.Session.State;
import com.felizardo.jerklib.events.IRCEvent;
import com.felizardo.jerklib.events.IRCEvent.Type;
import com.felizardo.jerklib.listeners.IRCEventListener;
import com.felizardo.jerklib.listeners.WriteRequestListener;
import com.felizardo.jerklib.parsers.DefaultInternalEventParser;
import com.felizardo.jerklib.parsers.InternalEventParser;
import com.felizardo.jerklib.tasks.Task;

/**
 * This class is used to control/store Sessions/Connections.
 * Request new connections with this class.
 * 
 * @author mohadib 
 * 
 */
public class ConnectionManager
{
	/* maps to index sessions by name and socketchannel */
	final Map<String, Session> sessionMap = new HashMap<String, Session>();

	/* event listener lists */
	private final List<WriteRequestListener> writeListeners = new ArrayList<WriteRequestListener>(1);

	/* event queues */
	private final List<IRCEvent> eventQueue = new ArrayList<IRCEvent>();
	private final List<IRCEvent> relayQueue = new ArrayList<IRCEvent>();
	private final List<WriteRequest> requestForWriteListenerEventQueue = new ArrayList<WriteRequest>();

	/* internal event parser */
	// private InternalEventParser parser = new InternalEventParserImpl(this);
	private IRCEventListener internalEventHandler = new DefaultInternalEventHandler(this);
	private InternalEventParser internalEventParser = new DefaultInternalEventParser();

	/* default user profile to use for new connections */
	private Profile defaultProfile;


	/**
	 * Takes a profile to use as default profile for new
	 * Connections
	 * 
	 * @param defaultProfile default user profile
	 * @see Profile
	 */
	public ConnectionManager(Profile defaultProfile)
	{
		this.defaultProfile = defaultProfile;

	}

	/**
	 * This is for testing purposes only.
	 * Do not use unless testing. 
	 * 
	 */
	ConnectionManager()
	{
	}

	
	
	
	/*private boolean autoReCon = true;
	private int reconTries = 10;
	/**
	 * FEATURE DISABLED FOR NOW
	 * @param bool
	 *
	public void setAutoReconnect(boolean bool)
	{
		this.autoReCon = bool;
	}
	
	public void setAutoReconnect(int amountTries)
	{
		autoReCon = true;
		reconTries = amountTries;
	}*/
	
	/**
	 * get a list of Sessions
	 * 
	 * @return Session list
	 */
	public List<Session> getSessions()
	{
		return Collections.unmodifiableList(new ArrayList<Session>(sessionMap.values()));
	}

	/**
	 * gets a session by name
	 * 
	 * @param name session name - the hostname of the server this session is for
	 * @return Session or null if no Session with name exists
	 */
	public Session getSession(String name)
	{
		return sessionMap.get(name);
	}

	/**
	 * Adds a listener to be notified of all writes
	 * 
	 * @param listener to be notified
	 */
	public void addWriteRequestListener(WriteRequestListener listener)
	{
		writeListeners.add(listener);
	}

	/**
	 * gets an unmodifiable list of WriteListeners
	 * 
	 * @return listeners
	 */
	public List<WriteRequestListener> getWriteListeners()
	{
		return Collections.unmodifiableList(writeListeners);
	}

	/**
	 * request a new connection to a host with the default port of 6667
	 * 
	 * @param hostName DNS name or IP of host to connect to
	 * @return the {@link Session} for this connection
	 */
	public Session requestConnection(String hostName)
	{
		return requestConnection(hostName, 6667);
	}

	/**
	 * request a new connection to a host
	 * 
	 * @param hostName DNS name or IP of host to connect to
	 * @param port port to use for connection
	 * @return the {@link Session} for this connection
	 */
	public Session requestConnection(String hostName, int port)
	{
		return requestConnection(hostName, port, defaultProfile.clone());
	}

	/**
	 * request a new connection to a host
	 * 
	 * @param hostName DNS name or IP of host to connect to
	 * @param port port to use for connection
	 * @param profile profile to use for this connection
	 * @return the {@link Session} for this connection
	 */
	public Session requestConnection(String hostName, int port, Profile profile)
	{
		RequestedConnection rCon = new RequestedConnection(hostName, port, profile);

		Session session = new Session(rCon , this);
		session.setInternalParser(internalEventParser);
		sessionMap.put(hostName, session);
		connect(session);
		//new IdentServer(defaultProfile.getName());
		
		return session;
	}

	/**
	 * Closes all connections and shuts down manager
	 * 
	 * @param quitMsg quit message
	 */
	public synchronized void quit(String quitMsg)
	{

		for (Session session : new ArrayList<Session>(sessionMap.values()))
		{
			session.close(quitMsg);
		}

		sessionMap.clear();


	}

	/**
	 * Closes all Sessions and exits library
	 */
	public synchronized void quit()
	{
		quit("");
	}

	/**
	 * gets the default profile used for new connections
	 * 
	 * @return default profile
	 */
	public Profile getDefaultProfile()
	{
		return defaultProfile;
	}

	/**
	 * sets the default profile to use for new connections
	 * 
	 * @param profile
	 *          default profile to use for connections
	 */
	public void setDefaultProfile(Profile profile)
	{
		this.defaultProfile = profile;
	}

	/**
	 * Sets the InternalEventHandler to use for this Session.
	 * This IRCEventListener is responsible for getting internal house keeping done - like nick caches, channel caches.
	 * This Listener is also responsible for redispatching events to other listeners if you choose to.
	 * 
	 * 
	 * @see IRCEventListener
	 * @see DefaultInternalEventHandler
	 * 
	 * @param handler
	 */
	public void setDefaultInternalEventHandler(IRCEventListener handler)
	{
		internalEventHandler = handler;
	}
	
	/**
	 * Gets the InternalEventHandler to use for this Session.
	 * @return default Event Handler
	 */
	public IRCEventListener getDefaultEventHandler()
	{
		return internalEventHandler;
	}
	
	/**
	 * Set the InternalEventParser used for this Session.
	 * 
	 * @param parser
	 */
	public void setDefaultInternalEventParser(InternalEventParser parser)
	{
		internalEventParser = parser;
	}

	/**
	 * Get the InternalEventParser used for this Session.
	 * @return InternalEventParser for Session
	 */
	public InternalEventParser getDefaultInternalEventParser()
	{
		return internalEventParser;
	}
	
	/**
	 * Remove a session
	 * 
	 * @param session
	 */
	void removeSession(Session session)
	{
		sessionMap.remove(session.getRequestedConnection().getHostName());
		/*for (Iterator<Session> it = socChanMap.values().iterator(); it.hasNext();)
		{
			if (it.next().equals(session))
			{
				it.remove();
				return;
			}
		}*/
	}

	/**
	 * Add an event to the EventQueue to be parsed and dispatched to Listeners
	 * 
	 * @param event
	 */
	void addToEventQueue(IRCEvent event)
	{
		eventQueue.add(event);
	}

	/**
	 * Add an event to be dispatched to Listeners(will not be parsed)
	 * 
	 * @param event
	 */
	void addToRelayList(IRCEvent event)
	{
		if (event == null)
		{
			new Exception().printStackTrace();
			quit("Null Pointers ?? In my Code??! :(");
			return;
		}

		synchronized (relayQueue)
		{
			relayQueue.add(event);
		}
	}


	/**
	 * Update internal states and do input/output
	 * This is called by the socket when some message arrive.
	 */
	
	void updateState()
	{
		parseEvents();
		relayEvents();
		checkServerConnections();
		notifyWriteListeners();
	}
	

	/**
	 * Check livelyness of server connections
	 */
	void checkServerConnections()
	{
		synchronized (sessionMap)
		{
			for (Iterator<Session> it = sessionMap.values().iterator(); it.hasNext();)
			{
				Session session = it.next();
				State state = session.getState();

				if (state == State.MARKED_FOR_REMOVAL)
				{
					it.remove();
				}
				else if (state == State.NEED_TO_PING)
				{
					session.getConnection().ping();
				}
			}
		}
	}

	/**
	 * Parse Events
	 */
	void parseEvents()
	{
		synchronized (eventQueue)
		{
			if (eventQueue.isEmpty()) { return; }
			for (IRCEvent event : eventQueue)
			{
				IRCEvent newEvent = event.getSession().getInternalEventParser().receiveEvent(event);
				internalEventHandler.receiveEvent(newEvent);
			}
			eventQueue.clear();
		}
	}
	
	/**
	 * Remove Cancelled Tasks for a Session
	 * @param session
	 * @return remanding valid tasks
	 */
	Map<Type, List<Task>> removeCanceled(Session session)
	{
		Map<Type, List<Task>> tasks = session.getTasks();
		synchronized (tasks)
		{
			for (Iterator<List<Task>> it = tasks.values().iterator(); it.hasNext();)
			{
				List<Task> thisTasks = it.next();
				for (Iterator<Task> x = thisTasks.iterator(); x.hasNext();)
				{
					Task rmTask = x.next();
					if (rmTask.isCanceled())
					{
						x.remove();
					}
				}
			}
		}
		return tasks;
	}

	/**
	 * Relay events to Listeners/Tasks
	 */
	void relayEvents()
	{
		List<IRCEvent> events = new ArrayList<IRCEvent>();
		List<IRCEventListener> templisteners = new ArrayList<IRCEventListener>();
		Map<Type, List<Task>> tempTasks = new HashMap<Type, List<Task>>();

		synchronized (relayQueue)
		{
			events.addAll(relayQueue);
			relayQueue.clear();
		}

		for (IRCEvent event : events)
		{
			Session s = event.getSession();

			// if session is null , this means the session has been removed or
			// quit() in Session has been called , but not before a few
			// events could queue up for that session. So we should continue
			// to the next event
			if (s == null)
			{
				continue;
			}

			Collection<IRCEventListener> listeners = s.getIRCEventListeners();
			synchronized (listeners)
			{
				templisteners.addAll(listeners);
			}

			tempTasks.putAll(removeCanceled(s));

			List<Task> typeTasks = tempTasks.get(event.getType());
			if (typeTasks != null)
			{
				templisteners.addAll(typeTasks);
			}

			List<Task> nullTasks = tempTasks.get(null);
			if (nullTasks != null)
			{
				templisteners.addAll(nullTasks);
			}

			for (IRCEventListener listener : templisteners)
			{
				try
				{
					listener.receiveEvent(event);
				}
				catch (Exception e)
				{
					System.err.println("jerklib:Cought Client Exception");
					e.printStackTrace();
				}
			}

			templisteners.clear();
			tempTasks.clear();
		}
	}

	/**
	 * Relay write requests to listeners
	 */
	void notifyWriteListeners()
	{
		List<WriteRequestListener> list = new ArrayList<WriteRequestListener>();
		List<WriteRequest> wRequests = new ArrayList<WriteRequest>();

		synchronized (requestForWriteListenerEventQueue)
		{
			if (requestForWriteListenerEventQueue.isEmpty()) { return; }
			wRequests.addAll(requestForWriteListenerEventQueue);
			requestForWriteListenerEventQueue.clear();
		}

		synchronized (writeListeners)
		{
			list.addAll(writeListeners);
		}

		for (WriteRequestListener listener : list)
		{
			for (WriteRequest request : wRequests)
			{
				listener.receiveEvent(request);
			}
		}
	}
	/**
	 * Connect a Session to a server
	 * 
	 * @param session
	 */
	void connect(Session session)
	{

		Connection con = new Connection(this, session);
		session.setConnection(con);

	}
}
