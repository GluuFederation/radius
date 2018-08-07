package org.gluu.radius.util;

import java.util.Properties;

import org.gluu.radius.GluuRadiusException;

public class PropertyUtil {
	
	public static final String getStringProperty(Properties props,String key,String defaultvalue) {

		return props.getProperty(key,defaultvalue);
	}

	public static final String getStringProperty(Properties props,String key) {

		return getStringProperty(props,key,null);
	}

	public static final Integer getIntProperty(Properties props,String key,Integer defaultvalue) {

		Integer ret = defaultvalue;
		String strval  = props.getProperty(key);
		if(strval!=null) {
			try {
				ret = Integer.parseInt(strval);
			}catch(NumberFormatException nfe) {
				throw new GluuRadiusException("could not get property value " + key,nfe);
			} 
		}
		return ret;
	}


	public static final Integer getIntProperty(Properties props,String key) {

		return getIntProperty(props,key,null);
	}

	public static final Boolean getBooleanProperty(Properties props,String key,Boolean defaultvalue) {

		Boolean ret = defaultvalue;
		String strval = props.getProperty(key);
		if(strval!=null)
			ret = Boolean.parseBoolean(strval);
		return ret;
	}

	public static final Boolean getBooleanProperty(Properties props,String key) {

		return getBooleanProperty(props,key,null);
	}
}