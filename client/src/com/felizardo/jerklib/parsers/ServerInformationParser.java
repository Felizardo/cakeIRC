package com.felizardo.jerklib.parsers;

import com.felizardo.jerklib.Session;
import com.felizardo.jerklib.events.IRCEvent;
import com.felizardo.jerklib.events.ServerInformationEvent;


public class ServerInformationParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		Session session = event.getSession();
		session.getServerInformation().parseServerInfo(event.getRawEventData());
		return new ServerInformationEvent(session, event.getRawEventData(), session.getServerInformation());
	}
}
