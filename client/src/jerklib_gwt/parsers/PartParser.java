package jerklib_gwt.parsers;

import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.PartEvent;

/**
 * @author mohadib
 *
 */
public class PartParser implements CommandParser
{
	public PartEvent createEvent(IRCEvent event)
	{
			return new PartEvent
			(
					event.getRawEventData(), 
					event.getSession(),
					event.getNick(), // who
					event.getSession().getChannel(event.arg(0)), 
					event.args().size() == 2? event.arg(1) : ""
			);
	}
}
