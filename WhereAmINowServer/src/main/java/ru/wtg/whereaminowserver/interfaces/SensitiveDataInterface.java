package ru.wtg.whereaminowserver.interfaces;

import org.json.JSONObject;

/**
 * Created 1/13/17.
 */

public interface SensitiveDataInterface {

    String getFCMServerKey();
    String getFCMSenderKey();
    String getFCMServerName();
    String getFBPrivateKeyFile();
    String getFBDatabaseUrl();
    String getSSLCertificatePassword();
    JSONObject getFirebaseConfig();
    String getLogin();
    String getPassword();


    String getWsServerHost();
    int getWsServerPortDedicated();
    int getWssServerPortDedicated();
    int getWsServerPortFB();
    int getWssServerPortFB();
    String getHttpServerHost();
    int getHttpServerPort();
    int getHttpsServerPort();

    String getWebRootDirectory();
    String getKeystoreFilename();
    boolean isDebugMode();
}
