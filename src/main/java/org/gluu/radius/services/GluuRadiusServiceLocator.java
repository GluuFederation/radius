package org.gluu.radius.services;

import java.util.HashMap;
import java.util.Map;


public class GluuRadiusServiceLocator {
	
	private static GluuRadiusServiceLocator INSTANCE = null;

	private Map<String,Object> servicemap;

	private static GluuRadiusServiceLocator getInstance() {

		if(INSTANCE == null) {
			INSTANCE = new GluuRadiusServiceLocator();
		}

		return INSTANCE;
	}

	private GluuRadiusServiceLocator() {

		servicemap = new HashMap<String,Object>();
	}


	public static void registerService(String serviceid,Object service) {

		getInstance().servicemap.put(serviceid,service);
	}

	public static void registerService(GluuRadiusKnownService serviceid,Object service) {

		getInstance().servicemap.put(serviceid.getId(),service);
	}

	@SuppressWarnings("unchecked")
	public static <T> T  getService(String serviceid) {

		return (T) getInstance().servicemap.get(serviceid);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(GluuRadiusKnownService serviceid) {

		return (T) getInstance().servicemap.get(serviceid.getId());
	}


}