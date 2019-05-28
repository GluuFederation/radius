package org.gluu.radius.server;

import java.util.List;
import java.util.ArrayList;

import org.gluu.radius.model.ServerConfiguration;

public class RunConfiguration {

    private String  listenInterface;
    private Integer authListenPort;
    private Integer acctListenPort;
    private List<AccessRequestFilter> accessRequestFilters;
    private List<AccountingRequestFilter> accountingRequestFilters;
    private List<RadiusClientMatcher> clientMatchers;

    public RunConfiguration() {

        this.listenInterface = ServerConfiguration.LISTEN_ON_ALL_INTERFACES;
        this.authListenPort  = ServerConfiguration.DEFAULT_RADIUS_AUTH_PORT;
        this.acctListenPort  = ServerConfiguration.DEFAULT_RADIUS_ACCT_PORT;
        this.accessRequestFilters = new ArrayList<AccessRequestFilter>();
        this.accountingRequestFilters = new ArrayList<AccountingRequestFilter>();
        this.clientMatchers = new ArrayList<RadiusClientMatcher>(); 
    }

    public String getListenInterface() {

        return this.listenInterface;
    }

    public RunConfiguration setListenInterface(String listenInterface) {

        this.listenInterface = listenInterface;
        return this;
    }

    public Integer getAuthListenPort() {

        return this.authListenPort;
    }

    public RunConfiguration setAuthListenPort(Integer authListenPort) {

        this.authListenPort = authListenPort;
        return this;
    }

    public Integer getAcctListenPort() {

        return this.acctListenPort;
    }

    public RunConfiguration setAcctListenPort(Integer acctListenPort) {

        this.acctListenPort = acctListenPort;
        return this;
    }

    public List<AccessRequestFilter> getAccessRequestFilters() {

        return this.accessRequestFilters;
    }

    public RunConfiguration addAccessRequestFilter(AccessRequestFilter accessRequestFilter) {

        accessRequestFilters.add(accessRequestFilter);
        return this;
    }

    public List<AccountingRequestFilter> getAccountingRequestFilters() {

        return this.accountingRequestFilters;
    }

    public RunConfiguration addAccountingRequestFilter(AccountingRequestFilter accountingRequestFilter) {

        accountingRequestFilters.add(accountingRequestFilter);
        return this;
    }

    public List<RadiusClientMatcher> getClientMatchers() {

        return this.clientMatchers;
    }

    public RunConfiguration addClientMatcher(RadiusClientMatcher clientMatcher) {

        clientMatchers.add(clientMatcher);
        return this;
    }


    public static final RunConfiguration fromServerConfiguration(ServerConfiguration serverConfig) {

        RunConfiguration runConfig = new RunConfiguration();
        runConfig.setListenInterface(serverConfig.getListenInterface());
        runConfig.setAuthListenPort(serverConfig.getAuthPort());
        runConfig.setAcctListenPort(serverConfig.getAcctPort());
        return runConfig;
    }
}