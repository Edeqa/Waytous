package ru.wtg.whereaminowserver.interfaces;

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

}
