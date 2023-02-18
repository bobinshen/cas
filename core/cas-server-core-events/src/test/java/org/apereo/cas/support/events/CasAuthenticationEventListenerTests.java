package org.apereo.cas.support.events;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.security.auth.login.FailedLoginException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAuthenticationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasAuthenticationEventListenerTests.EventTestConfiguration.class,
    CasCoreEventsConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
})
@Tag("Events")
public class CasAuthenticationEventListenerTests {
    public static final String REMOTE_ADDR_IP = "123.456.789.000";
    public static final String LOCAL_ADDR_IP = "123.456.789.000";
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository casEventRepository;

    @BeforeEach
    public void initialize() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr(REMOTE_ADDR_IP);
        request.setLocalAddr(LOCAL_ADDR_IP);
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @Test
    public void verifyCasAuthenticationWithNoClientInfo() {
        ClientInfoHolder.setClientInfo(null);
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        applicationContext.publishEvent(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasAuthenticationTransactionFailureEvent() {
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        applicationContext.publishEvent(event);
        val savedEventOptional = casEventRepository.load().findFirst();
        assertFalse(savedEventOptional.isEmpty());
        val savedEvent = savedEventOptional.get();
        assertEquals(CasAuthenticationTransactionFailureEvent.class.getSimpleName(), savedEvent.getEventId());
    }

    @Test
    public void verifyTicketGrantingTicketCreated() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt);
        applicationContext.publishEvent(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasAuthenticationPolicyFailureEvent() {
        val event = new CasAuthenticationPolicyFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            new DefaultAuthenticationTransaction(CoreAuthenticationTestUtils.getService(),
                CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())),
            CoreAuthenticationTestUtils.getAuthentication());
        applicationContext.publishEvent(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasRiskyAuthenticationDetectedEvent() {
        val event = new CasRiskyAuthenticationDetectedEvent(this,
            CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getRegisteredService(),
            new Object());
        applicationContext.publishEvent(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasTicketGrantingTicketDestroyed() {
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
            new MockTicketGrantingTicket("casuser"));
        applicationContext.publishEvent(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }
    @Test
    public void verifyEventRepositoryHasOneEventOnly() {
        clearEventRepository();
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
                new MockTicketGrantingTicket("casuser"));
        applicationContext.publishEvent(event);
        assertEquals("verify repo has 1 event",1,casEventRepository.load().collect(Collectors.toList()).size());
    }

    private void clearEventRepository(){
        SimpleCasEventRepository eventRepository = (SimpleCasEventRepository)  casEventRepository;
        eventRepository.clearEvents();
    }
   /* @Test
    public void verifyCasTicketGrantingTicketDestroyedHasClientInfo() {
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
                new MockTicketGrantingTicket("casuser"));
        applicationContext.publishEvent(event);

        assertFalse(casEventRepository.load().filt().isEmpty());
    }*/

    @TestConfiguration(value = "EventTestConfiguration", proxyBeanMethods = false)
    public static class EventTestConfiguration {
        @Bean
        public CasEventRepository casEventRepository() {
            return new SimpleCasEventRepository(CasEventRepositoryFilter.noOp());
        }
    }
}
