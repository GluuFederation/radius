package org.gluu.radius.config;


public class GluuRadiusCacheConfig {
	
	enum GluuRadiusCachePolicy {
		CachingDisabled, // caching disabled 
		NoExpiry, // always keep entries , including expired ones 
		Normal, // use cache interval to determine which entries to keep 
	}

	private Integer cacheinterval;
	private GluuRadiusCachePolicy policy;

	private GluuRadiusCacheConfig(Integer cacheinterval,GluuRadiusCachePolicy policy) {

		this.cacheinterval = cacheinterval;
		this.policy = policy;
	}

	public boolean isCachingDisabledPolicy() {

		return policy == GluuRadiusCachePolicy.CachingDisabled;
	}

	public boolean isNoExpiryPolicy() {

		return policy == GluuRadiusCachePolicy.NoExpiry;
	}

	public boolean isNormalPolicy() {

		return policy == GluuRadiusCachePolicy.Normal;
	}

	public long cacheIntervalAsLong() {

		return cacheinterval.longValue();
	}

	public Integer getCacheInterval() {

		return this.cacheinterval;
	}


	public static GluuRadiusCacheConfig createCachingDisabledConfig() {

		return new GluuRadiusCacheConfig(0,GluuRadiusCachePolicy.CachingDisabled);
	}


	public static GluuRadiusCacheConfig createNoExpiryConfig() {

		return new GluuRadiusCacheConfig(0,GluuRadiusCachePolicy.NoExpiry);
	}


	public static GluuRadiusCacheConfig createNormalConfig(Integer cacheinterval) {

		return new GluuRadiusCacheConfig(cacheinterval,GluuRadiusCachePolicy.Normal);
	} 

}