package jerklib_gwt.parsers;

import jerklib_gwt.Session;
import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.ServerInformationEvent;



public class ServerInformationParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		Session session = event.getSession();
		session.getServerInformation().parseServerInfo(event.getRawEventData());
		return new ServerInformationEvent(session, event.getRawEventData(), session.getServerInformation());
	}
}
