package jerklib_gwt.parsers;

import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.NickInUseEvent;

public class NickInUseParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		return new NickInUseEvent
		(
				event.arg(1),
				event.getRawEventData(), 
				event.getSession()
		); 
	}
}
