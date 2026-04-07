package com.naveen.api.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "system:properties",
        "classpath:config.properties"
})
public interface ApiConfig extends Config {

    // Base URL
    @Key("base.url")
    @DefaultValue("https://restful-booker.herokuapp.com")
    String baseUrl();

    // Auth
    @Key("admin.username")
    @DefaultValue("admin")
    String adminUsername();

    @Key("admin.password")
    @DefaultValue("password123")
    String adminPassword();

    // SLA Thresholds (milliseconds)
    @Key("sla.response.time.ms")
    @DefaultValue("2000")
    long slaResponseTimeMs();

    @Key("sla.auth.response.time.ms")
    @DefaultValue("3000")
    long slaAuthResponseTimeMs();

    // OWASP ZAP
    @Key("zap.enabled")
    @DefaultValue("false")
    boolean zapEnabled();

    @Key("zap.proxy.host")
    @DefaultValue("localhost")
    String zapProxyHost();

    @Key("zap.proxy.port")
    @DefaultValue("8080")
    int zapProxyPort();
}