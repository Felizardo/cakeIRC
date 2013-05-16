package jerklib_gwt.parsers;

import jerklib_gwt.Session;
import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.NickChangeEvent;



public class NickParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		Session session = event.getSession();
		return new NickChangeEvent
		(
				event.getRawEventData(), 
				session, 
				event.getNick(), // old
				event.arg(0)// new nick
		); 
	}
}
