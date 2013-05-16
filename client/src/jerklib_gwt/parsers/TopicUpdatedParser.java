package jerklib_gwt.parsers;

import jerklib_gwt.events.IRCEvent;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


public class TopicUpdatedParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		RegExp p = RegExp.compile("^.+?TOPIC\\s+(.+?)\\s+.*$");
		if (p.test(event.getRawEventData())) {
			MatchResult m = p.exec(event.getRawEventData());
			event.getSession().sayRaw("TOPIC " + m.getGroup(1));
		}
		return event;
	}
}
