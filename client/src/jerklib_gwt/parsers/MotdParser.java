package jerklib_gwt.parsers;

import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.MotdEvent;

public class MotdParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		return new MotdEvent
		(
			event.getRawEventData(), 
			event.getSession(), 
			event.arg(1), 
			event.prefix()
		);
	}
}
