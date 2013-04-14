package com.felizardo.jerklib.parsers;

import java.util.List;

import com.felizardo.jerklib.Channel;
import com.felizardo.jerklib.Session;
import com.felizardo.jerklib.events.IRCEvent;
import com.felizardo.jerklib.events.QuitEvent;


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
