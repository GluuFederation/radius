package org.gluu.radius.services.impl;


import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import org.gluu.radius.config.GluuRadiusClientConfig;
import org.gluu.radius.services.GluuRadiusServiceException;


public class GluuRadiusClientConfigList {
	
	private List<GluuRadiusClientConfig> clientconfiglist;


	public GluuRadiusClientConfigList() {

		this.clientconfiglist = null;
	}

	@JsonSetter("clients")
	public GluuRadiusClientConfigList setClientConfigList(List<GluuRadiusClientConfig> clientconfiglist) {

		this.clientconfiglist = clientconfiglist;
		return this;
	}

	public GluuRadiusClientConfig getClientConfig(String ipaddress) {

		if(clientconfiglist == null)
			return null;
		for(GluuRadiusClientConfig config : clientconfiglist) {
			if(config.isIpAddress(ipaddress))
				return config;
		}
		return null;
	}


	public static GluuRadiusClientConfigList fromJson(String jsondata) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(jsondata,GluuRadiusClientConfigList.class);
		}catch(IOException e) {
			throw new GluuRadiusServiceException("I/O error while unserializing the client list",e);
		}
	}

}