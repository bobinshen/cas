package org.apereo.cas.oidc.token;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is {@link OidcIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class OidcIdTokenGeneratorService {

    private final CasConfigurationProperties casProperties;
    private final OidcIdTokenSigningAndEncryptionService signingService;
    private final ServicesManager servicesManager;

    /**
     * Generate string.
     *
     * @param request           the request
     * @param response          the response
     * @param accessTokenId     the access token id
     * @param timeoutInSeconds  the timeoutInSeconds
     * @param responseType      the response type
     * @param registeredService the registered service
     * @return the string
     */
    public String generate(final HttpServletRequest request,
                           final HttpServletResponse response,
                           final AccessToken accessTokenId,
                           final long timeoutInSeconds,
                           final OAuth20ResponseTypes responseType,
                           final OAuthRegisteredService registeredService) {

        if (!(registeredService instanceof OidcRegisteredService)) {
            throw new IllegalArgumentException("Registered service instance is not an OIDC service");
        }

        val oidcRegisteredService = (OidcRegisteredService) registeredService;
        val context = Pac4jUtils.getPac4jJ2EContext(request, response);
        val manager = Pac4jUtils.getPac4jProfileManager(request, response);
        val profile = (Optional<CommonProfile>) manager.get(true);

        if (!profile.isPresent()) {
            throw new IllegalArgumentException("Unable to determine the user profile from the context");
        }

        LOGGER.debug("Attempting to produce claims for the id token [{}]", accessTokenId);
        val claims = produceIdTokenClaims(request, accessTokenId, timeoutInSeconds,
            oidcRegisteredService, profile.get(), context, responseType);
        LOGGER.debug("Produce claims for the id token [{}] as [{}]", accessTokenId, claims);

        return this.signingService.encode(oidcRegisteredService, claims);
    }

    /**
     * Produce id token claims jwt claims.
     *
     * @param request          the request
     * @param accessTokenId    the access token id
     * @param timeoutInSeconds the timeoutInSeconds
     * @param service          the service
     * @param profile          the user profile
     * @param context          the context
     * @param responseType     the response type
     * @return the jwt claims
     */
    protected JwtClaims produceIdTokenClaims(final HttpServletRequest request,
                                             final AccessToken accessTokenId,
                                             final long timeoutInSeconds,
                                             final OidcRegisteredService service,
                                             final UserProfile profile,
                                             final J2EContext context,
                                             final OAuth20ResponseTypes responseType) {
        val authentication = accessTokenId.getAuthentication();
        val principal = authentication.getPrincipal();
        val oidc = casProperties.getAuthn().getOidc();

        val claims = new JwtClaims();
        claims.setJwtId(getOAuthServiceTicket(accessTokenId.getTicketGrantingTicket()).getKey());
        claims.setIssuer(oidc.getIssuer());
        claims.setAudience(service.getClientId());

        val expirationDate = NumericDate.now();
        expirationDate.addSeconds(timeoutInSeconds);
        claims.setExpirationTime(expirationDate);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(oidc.getSkew());
        claims.setSubject(principal.getId());

        val mfa = casProperties.getAuthn().getMfa();
        val attributes = authentication.getAttributes();

        if (attributes.containsKey(mfa.getAuthenticationContextAttribute())) {
            val val = CollectionUtils.toCollection(attributes.get(mfa.getAuthenticationContextAttribute()));
            claims.setStringClaim(OidcConstants.ACR, val.iterator().next().toString());
        }
        if (attributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS)) {
            val val = CollectionUtils.toCollection(attributes.get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
            claims.setStringListClaim(OidcConstants.AMR, val.toArray(new String[]{}));
        }

        claims.setClaim(OAuth20Constants.STATE, attributes.get(OAuth20Constants.STATE));
        claims.setClaim(OAuth20Constants.NONCE, attributes.get(OAuth20Constants.NONCE));
        claims.setClaim(OidcConstants.CLAIM_AT_HASH, generateAccessTokenHash(accessTokenId, service));

        principal.getAttributes().entrySet().stream()
            .filter(entry -> oidc.getClaims().contains(entry.getKey()))
            .forEach(entry -> claims.setClaim(entry.getKey(), entry.getValue()));

        if (!claims.hasClaim(OidcConstants.CLAIM_PREFERRED_USERNAME)) {
            claims.setClaim(OidcConstants.CLAIM_PREFERRED_USERNAME, profile.getId());
        }

        return claims;
    }

    private Entry<String, Service> getOAuthServiceTicket(final TicketGrantingTicket tgt) {
        val oAuthCallbackUrl = getOAuthCallbackUrl();

        val oAuthServiceTicket = Stream.concat(
            tgt.getServices().entrySet().stream(),
            tgt.getProxyGrantingTickets().entrySet().stream())
            .filter(e -> servicesManager.findServiceBy(e.getValue()).getServiceId().equals(oAuthCallbackUrl))
            .findFirst();
        Preconditions.checkState(oAuthServiceTicket.isPresent(), "Cannot find service ticket issued to "
            + oAuthCallbackUrl + " as part of the authentication context");
        return oAuthServiceTicket.get();
    }

    /**
     * Gets o auth callback url.
     *
     * @return the o auth callback url
     */
    public String getOAuthCallbackUrl() {
        return casProperties.getServer().getPrefix()
            + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
    }

    private String generateAccessTokenHash(final AccessToken accessTokenId,
                                           final OidcRegisteredService service) {
        val tokenBytes = accessTokenId.getId().getBytes(StandardCharsets.UTF_8);
        val hashAlg = getSigningHashAlgorithm();
        LOGGER.debug("Digesting access token hash via algorithm [{}]", hashAlg);
        val digested = DigestUtils.rawDigest(hashAlg, tokenBytes);
        val hashBytesLeftHalf = Arrays.copyOf(digested, digested.length / 2);
        return EncodingUtils.encodeUrlSafeBase64(hashBytesLeftHalf);
    }

    private String getSigningHashAlgorithm() {
        if (AlgorithmIdentifiers.RSA_USING_SHA512.equalsIgnoreCase(signingService.getJsonWebKeySigningAlgorithm())) {
            return MessageDigestAlgorithms.SHA_512;
        }
        return MessageDigestAlgorithms.SHA_256;
    }
}

