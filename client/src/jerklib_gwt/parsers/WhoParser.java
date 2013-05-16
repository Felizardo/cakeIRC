package jerklib_gwt.parsers;

import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.WhoEvent;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


public class WhoParser implements CommandParser
{
	public IRCEvent createEvent(IRCEvent event)
	{
		String data = event.getRawEventData();
		RegExp p = RegExp.compile("^:.+?\\s+352\\s+.+?\\s+(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+?):(\\d+)\\s+(.+)$");
		if (p.test(data))
		{
			MatchResult m = p.exec(data);

			boolean away = m.getGroup(6).charAt(0) == 'G';
			return new WhoEvent(m.getGroup(1), // channel
					Integer.parseInt(m.getGroup(7)), // hop count
					m.getGroup(3), // hostname
					away, // status indicator
					m.getGroup(5), // nick
					data, // raw event data
					m.getGroup(8), // real name
					m.getGroup(4), // server name
					event.getSession(), // session
					m.getGroup(2) // username
			);
		}
		return event;
	}
}
