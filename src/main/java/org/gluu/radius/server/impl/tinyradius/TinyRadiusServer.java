package org.gluu.radius.server.impl.tinyradius;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.gluu.radius.config.GluuRadiusServerConfig;
import org.gluu.radius.server.GluuRadiusServer;
import org.gluu.radius.server.GluuRadiusServerException;
import org.gluu.radius.util.ThreadUtil;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusServer;

public class TinyRadiusServer extends GluuRadiusServer {

	private static final String auththreadname = "Radius Auth Listener";
	private static final String acctthreadname = "Radius Acct Listener";

	public class TinyRadiusServerImpl extends RadiusServer {


		public TinyRadiusServerImpl() {

		}

		@Override
		public String getUserPassword(String username) {

			return null;
		}

		@Override
		public String getSharedSecret(InetSocketAddress client) {

			String clientip = client.getAddress().getHostAddress();
			return TinyRadiusServer.this.getSharedSecret(clientip);
		}

		@Override
		public RadiusPacket accessRequestReceived(AccessRequest request,InetSocketAddress client) {

			RadiusPacket response = null;
			TinyRadiusAccessRequestContext ctx = new TinyRadiusAccessRequestContext(request,client);
			if(TinyRadiusServer.this.onAccessRequest(ctx))
				response = new RadiusPacket(RadiusPacket.ACCESS_ACCEPT,request.getPacketIdentifier());
			else
				response = new RadiusPacket(RadiusPacket.ACCESS_REJECT,request.getPacketIdentifier());
			copyProxyState(request,response);
			return response;
		}


		@Override
		public RadiusPacket accountingRequestReceived(AccountingRequest request,InetSocketAddress client) {

			RadiusPacket response = new RadiusPacket(RadiusPacket.ACCOUNTING_RESPONSE,request.getPacketIdentifier());
			TinyRadiusAccountingRequestContext ctx = new TinyRadiusAccountingRequestContext(request,client);
			TinyRadiusServer.this.onAccountingRequest(ctx);
			copyProxyState(request,response);
			return response;
		}


	}


	private TinyRadiusServerImpl serverimpl;

	public TinyRadiusServer(GluuRadiusServerConfig config) {

		serverimpl = new TinyRadiusServerImpl();
		serverimpl.setAuthPort(config.getAuthPort());
		serverimpl.setAcctPort(config.getAcctPort());
		try {
			serverimpl.setListenAddress(InetAddress.getByName(config.getListenAddress()));
		}catch(UnknownHostException e) {
			throw new GluuRadiusServerException("TinyRadiusServer initialization failed",e);
		}
	}

	@Override 
	public TinyRadiusServer run() {
		
		serverimpl.start(true,true);
		//hack to check if the server is running 
		long sleeptime = 1000; // 1 seconds 
		int checkcount = 5; // we will check five times
		boolean stillrunning = true;
		do {
			try {
				Thread.currentThread().sleep(sleeptime);
			}catch(InterruptedException e) {
				serverimpl.stop();
				throw new GluuRadiusServerException("Server unexpectedly interrupted");
			}
			stillrunning = areServerThreadsRunning();
		}while(--checkcount > 0);

		if(!stillrunning) {
			serverimpl.stop();
			throw new GluuRadiusServerException("TinyRadiusServer stopped unexpectedly.");
		}
		return this;
	}

	@Override
	public TinyRadiusServer shutdown() {

		serverimpl.stop();
		return this;
	}


	private boolean areServerThreadsRunning() {

		Thread acct_thread = ThreadUtil.findRunningThread(acctthreadname);
		Thread auth_thread = ThreadUtil.findRunningThread(auththreadname);
		return acct_thread!=null && auth_thread!=null;
	}
}