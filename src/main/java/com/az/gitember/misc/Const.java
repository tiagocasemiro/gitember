package com.az.gitember.misc;

import org.eclipse.jgit.lib.Constants;

/**
 * Created by Igor_Azarny on Dec - 17- 2016.
 */
public interface Const {

    String GIT_FOLDER = ".git";

    String TEMP_FILE_PREFIX = "gitember";

    String TITLE = "Gitember 1.6 ";

    String DIFF_EXTENSION = "diff";

    String ICON = "/icon/GE-icon.png";

    String KEYWORDS_CSS = "/styles/keywords.css";

    String DEFAULT_CSS = "/styles/styles.css";

    String MERGED = "MERGED";

    int SEARCH_LIMIT_CHAR = 2;

    String SYSTEM_PROXY_HOST = "http.proxyHost";
    String SYSTEM_PROXY_PORT = "http.proxyPort";
    String SYSTEM_PROXY_USER = "http.proxyUser";
    String SYSTEM_PROXY_PASSWORD = "http.proxyPassword";

    String PROP_FOLDER = ".gitember";
    String PROP_FILE_NAME = "gitember.json";


    String REMOTE_PREFIX = Constants.R_REMOTES  + Constants.DEFAULT_REMOTE_NAME +  '/';

    int ALERT_WIDTH = 600;


    interface Msg {
        String TRANSPORT_SSL_ISSUE = "could not be established because of SSL problems";
        String TRANSPORT_CRED_PROVIDER_ISSUE = "Authentication is required but no CredentialsProvider has been registered";
        String TRANSPORT_CRED_WRONG_ISSUE = "not authorized";
    }

    interface Config {
        String HOME = "home";
        String HTTP = "http";
        String SLL_VERIFY = "sslVerify";
    }




}
