package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.pac4j.discovery.DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnCasFeatureModule;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationDynamicDiscoveryExecutionAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link DelegatedAuthenticationDynamicDiscoverySelectionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Configuration(value = "DelegatedAuthenticationDynamicDiscoverySelectionConfiguration", proxyBeanMethods = false)
@ConditionalOnCasFeatureModule(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "dynamic-discovery")
public class DelegatedAuthenticationDynamicDiscoverySelectionConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pac4j.core.discovery-selection.selection-type")
        .havingValue("DYNAMIC");

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedAuthenticationDynamicDiscoveryProviderLocator")
    public DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
        final DelegatedClientAuthenticationConfigurationContext configContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(DelegatedAuthenticationDynamicDiscoveryProviderLocator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator(
                configContext.getDelegatedClientIdentityProvidersProducer(), configContext.getClients(), casProperties))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION)
    @Bean
    public Action delegatedAuthenticationProviderDynamicDiscoveryExecutionAction(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
        final DelegatedClientAuthenticationConfigurationContext configContext,
        @Qualifier("delegatedAuthenticationDynamicDiscoveryProviderLocator")
        final DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator) {
        return BeanSupplier.of(Action.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DelegatedClientAuthenticationDynamicDiscoveryExecutionAction(
                configContext, delegatedAuthenticationDynamicDiscoveryProviderLocator))
            .otherwise(() -> ConsumerExecutionAction.NONE)
            .get();
    }
}
