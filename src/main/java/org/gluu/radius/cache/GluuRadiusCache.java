package org.gluu.radius.cache;

import org.gluu.radius.config.GluuRadiusCacheConfig;

public interface GluuRadiusCache<K,T> {
	public void configure(GluuRadiusCacheConfig config);
	public T getCacheEntry(K key);
	public void setCacheEntry(K key,T value);
}