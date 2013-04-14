package com.felizardo.jerklib.parsers;

import com.felizardo.jerklib.events.IRCEvent;
import com.felizardo.jerklib.events.NumericErrorEvent;

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
