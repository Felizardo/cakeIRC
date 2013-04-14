package com.felizardo.core;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CakeIRC implements EntryPoint {


	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		JerkTest test = new JerkTest();
		RootPanel.get().add(test);
	}
}
