package jerklib_gwt.parsers;

import java.util.List;

import jerklib_gwt.Channel;
import jerklib_gwt.Session;
import jerklib_gwt.events.IRCEvent;
import jerklib_gwt.events.QuitEvent;



public class QuitParser implements CommandParser
{
	public QuitEvent createEvent(IRCEvent event)
	{
		Session session = event.getSession();
		String nick = event.getNick();
		List<Channel> chanList = event.getSession().removeNickFromAllChannels(nick);
		return new QuitEvent
		(
			event.getRawEventData(), 
			session, 
			event.arg(0), // message
			chanList
		);
	}
}
