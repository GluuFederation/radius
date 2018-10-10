package org.gluu.radius.util;

import java.io.InputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	
	public static final int HTTP_OK = 200;

	public static int getStatusCode(HttpResponse response) {

		return (response.getStatusLine()!=null?response.getStatusLine().getStatusCode():0);
	}


	public static boolean isHttpOk(HttpResponse response) {

		return getStatusCode(response) == HTTP_OK;
	}


	public static InputStream getResponseContent(HttpResponse response) throws IOException {

		if(response.getEntity()!=null)
			return response.getEntity().getContent();
		else
			return null;
	}


	public static String getResponseContentAsString(HttpResponse response) throws IOException {

		return EntityUtils.toString(response.getEntity());
	}
}