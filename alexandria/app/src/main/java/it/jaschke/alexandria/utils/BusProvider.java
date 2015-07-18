package it.jaschke.alexandria.utils;


import it.jaschke.alexandria.events.AndroidBus;

// Provided by Square under the Apache License
public final class BusProvider {
  private static final AndroidBus BUS = new AndroidBus();
 
	public static AndroidBus getInstance() {
		return BUS;
	}
 
	private BusProvider() {
		// No instances.
	}
}