package com.felizardo.jerklib.parsers;

import com.felizardo.jerklib.events.IRCEvent;

public interface InternalEventParser
{
	public IRCEvent receiveEvent(IRCEvent e);
}
