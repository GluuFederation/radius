package org.gluu.radius.cache.impl;

import java.util.Map;
import java.util.HashMap;

import org.gluu.radius.cache.GluuRadiusCache;
import org.gluu.radius.config.GluuRadiusCacheConfig;
import org.gluu.radius.config.GluuRadiusClientConfig;

public class SimpleGluuRadiusCache<K,T> implements GluuRadiusCache<K,T> {
	
	private static final Integer DEFAULT_CACHE_EXPIRY_INTERVAL =  5 * 60 * 1000; // 5 minutes
	
	private class CacheEntry {

		private long creationtime;
		private T value;

		public CacheEntry(long creationtime,T value) {

			this.creationtime = creationtime;
			this.value = value;
		}

		public long getCreationTime() {

			return this.creationtime;
		}

		public T getValue() {

			return this.value;
		}
	}

	private GluuRadiusCacheConfig config;
	private Map<K,CacheEntry> cachestorage;


	public SimpleGluuRadiusCache() {

		this.config = GluuRadiusCacheConfig.createNormalConfig(DEFAULT_CACHE_EXPIRY_INTERVAL);
		this.cachestorage = new HashMap<K,CacheEntry>();
	}

	public SimpleGluuRadiusCache(GluuRadiusCacheConfig config) {

		this.config = config;
		this.cachestorage = new HashMap<K,CacheEntry>();
	}


	@Override
	public synchronized void configure(GluuRadiusCacheConfig cacheconfig) {
		
		this.config = cacheconfig;		
	}


	@Override
	public synchronized T getCacheEntry(K key) {

		
		CacheEntry ccentry = cachestorage.get(key);
		if(config.isNoExpiryPolicy() && ccentry != null)
			return ccentry.getValue();
		else if(config.isNormalPolicy() && !cacheEntryExpired(ccentry))
			return ccentry.getValue();
		else if(ccentry!=null && key!=null && config.isNormalPolicy() && cacheEntryExpired(ccentry))
			cachestorage.remove(key);
		return null;
	}


	@Override
	public synchronized void setCacheEntry(K key,T value) {

		
		if(config!=null && key!=null)
			cachestorage.put(key,new CacheEntry(System.currentTimeMillis(),value));
	}

	private boolean cacheEntryExpired(CacheEntry entry) {

		if(entry == null)
			return true;

		long currtime = System.currentTimeMillis();
		long diff = currtime - entry.getCreationTime();
		return (diff > config.cacheIntervalAsLong());
	}

}