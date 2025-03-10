package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasEmbeddedApacheTomcatProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Setter
@Accessors(chain = true)
public class CasEmbeddedApacheTomcatProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -99143821503580896L;

    /**
     * Controls the {@code server} attribute of the tomcat connector.
     */
    private String serverName = "Apereo CAS";

    /**
     * Embedded container socket settings.
     * The NIO and NIO2 implementation support the Java TCP socket
     * attributes in addition to the common Connector and HTTP attributes.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatSocketProperties socket = new CasEmbeddedApacheTomcatSocketProperties();

    /**
     * Embedded container AJP settings.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatAjpProperties ajp = new CasEmbeddedApacheTomcatAjpProperties();

    /**
     * Embedded container HTTP port settings as an additional option.
     */
    private List<CasEmbeddedApacheTomcatHttpProperties> http = new ArrayList<>();

    /**
     * Embedded container tomcat clustering options.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatClusteringProperties clustering = new CasEmbeddedApacheTomcatClusteringProperties();

    /**
     * Http proxy configuration properties.
     * In the event that you decide to run CAS without any SSL configuration in the embedded Tomcat container and on a non-secure
     * port yet wish to customize the connector configuration that is linked to the running port (i.e. 8080), this setting may apply.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatHttpProxyProperties httpProxy = new CasEmbeddedApacheTomcatHttpProxyProperties();

    /**
     * Embedded container's SSL valve setting.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatSslValveProperties sslValve = new CasEmbeddedApacheTomcatSslValveProperties();

    /**
     * Embedded container's rewrite valve setting.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatRewriteValveProperties rewriteValve = new CasEmbeddedApacheTomcatRewriteValveProperties();

    /**
     * Embedded container's remote-user valve setting.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatRemoteUserValveProperties remoteUserValve = new CasEmbeddedApacheTomcatRemoteUserValveProperties();

    /**
     * Configuration properties for access logging beyond defaults.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatExtendedAccessLogProperties extAccessLog = new CasEmbeddedApacheTomcatExtendedAccessLogProperties();

    /**
     * Enable Tomcat's RemoteAddress filter.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatRemoteAddressProperties remoteAddr = new CasEmbeddedApacheTomcatRemoteAddressProperties();

    /**
     * Enable Tomcat's CSRF filter.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatCsrfProperties csrf = new CasEmbeddedApacheTomcatCsrfProperties();

    /**
     * Enable Tomcat's session initialization filter.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatSessionInitializationProperties sessionInitialization = new CasEmbeddedApacheTomcatSessionInitializationProperties();
    
    /**
     * Enable basic authentication for the embedded tomcat.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatBasicAuthenticationProperties basicAuthn = new CasEmbeddedApacheTomcatBasicAuthenticationProperties();
}
