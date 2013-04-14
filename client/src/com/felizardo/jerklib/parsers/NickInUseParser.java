package com.felizardo.jerklib.parsers;

import com.felizardo.jerklib.events.IRCEvent;
import com.felizardo.jerklib.events.NickInUseEvent;

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
