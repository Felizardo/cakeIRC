package com.felizardo.jerklib.parsers;

import com.felizardo.jerklib.Session;
import com.felizardo.jerklib.events.IRCEvent;
import com.felizardo.jerklib.events.NickChangeEvent;


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
