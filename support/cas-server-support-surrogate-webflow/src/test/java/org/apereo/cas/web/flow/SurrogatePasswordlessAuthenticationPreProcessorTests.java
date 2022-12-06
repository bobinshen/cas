package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.SurrogatePrincipal;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.config.SurrogateAuthenticationPasswordlessConfiguration;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.action.BaseSurrogateAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogatePasswordlessAuthenticationPreProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    SurrogateAuthenticationPasswordlessConfiguration.class,
    BaseSurrogateAuthenticationTests.SharedTestConfiguration.class
},
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
public class SurrogatePasswordlessAuthenticationPreProcessorTests extends BaseSurrogateAuthenticationTests {

    @Autowired
    @Qualifier("surrogatePasswordlessAuthenticationPreProcessor")
    private PasswordlessAuthenticationPreProcessor surrogatePasswordlessAuthenticationPreProcessor;

    @Test
    public void verifyOperation() {
        val uid = "casuser";
        val builder = new DefaultAuthenticationResultBuilder().collect(CoreAuthenticationTestUtils.getAuthentication(uid));
        val account = PasswordlessUserAccount.builder().username(uid).build();

        val credential = new BasicIdentifiableCredential(uid);
        val results = surrogatePasswordlessAuthenticationPreProcessor.process(builder, account,
            RegisteredServiceTestUtils.getService(), credential,
            PasswordlessAuthenticationToken.builder().username(uid).build().property("surrogateUsername", "cassurrogate"));
        assertTrue(credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).isPresent());
        val authns = new ArrayList<>(results.getAuthentications());
        assertTrue(authns.get(0).getPrincipal() instanceof SimplePrincipal);
        assertTrue(authns.get(1).getPrincipal() instanceof SurrogatePrincipal);
    }
}
