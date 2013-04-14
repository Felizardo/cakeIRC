package com.felizardo.jerklib.parsers;

import com.felizardo.jerklib.events.IRCEvent;

public interface CommandParser
{
	public IRCEvent createEvent(IRCEvent event);
}
