package jerklib_gwt.parsers;

import jerklib_gwt.events.IRCEvent;

public interface CommandParser
{
	public IRCEvent createEvent(IRCEvent event);
}
