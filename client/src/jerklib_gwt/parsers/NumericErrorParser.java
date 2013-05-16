package jerklib_gwt.parsers;

import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.NumericErrorEvent;

public class NumericErrorParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		return new NumericErrorEvent
		(
				event.arg(0), 
				event.getRawEventData(), 
				event.getSession()
		); 
	}
}
