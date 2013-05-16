package jerklib_gwt.parsers;

import jerklib_gwt.events.ChannelListEvent;
import jerklib_gwt.events.IRCEvent;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


public class ChanListParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		String data = event.getRawEventData();
		RegExp p = RegExp.compile("^:\\S+\\s322\\s\\S+\\s(\\S+)\\s(\\d+)\\s:(.*)$");
		
		if (p.test(data)) 
		{ 
			MatchResult m = p.exec(data);
			return new ChannelListEvent
			(
				data, 
				m.getGroup(1), 
				m.getGroup(3), 
				Integer.parseInt(m.getGroup(2)), 
				event.getSession()
			); 
		}
		return event;
	}
}
