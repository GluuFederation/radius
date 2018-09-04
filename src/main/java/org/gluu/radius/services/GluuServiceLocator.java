package org.gluu.radius.services;

import java.util.HashMap;
import java.util.Map;


public class GluuServiceLocator {
	
	private static GluuServiceLocator INSTANCE = null;

	private Map<String,Object> servicemap;

	private static GluuServiceLocator getInstance() {

		if(INSTANCE == null) {
			INSTANCE = new GluuServiceLocator();
		}

		return INSTANCE;
	}

	private GluuServiceLocator() {

		servicemap = new HashMap<String,Object>();
	}


	public static void registerService(String serviceid,Object service) {

		getInstance().servicemap.put(serviceid,service);
	}

	@SuppressWarnings("unchecked")
	public static <T> T  getService(String serviceid) {

		return (T) getInstance().servicemap.get(serviceid);
	}


}