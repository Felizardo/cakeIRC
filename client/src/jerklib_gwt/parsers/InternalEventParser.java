package jerklib_gwt.parsers;

import jerklib_gwt.events.IRCEvent;

public interface InternalEventParser
{
	public IRCEvent receiveEvent(IRCEvent e);
}
