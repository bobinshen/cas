package org.apereo.cas.scim.v2;

import org.apereo.cas.services.RegisteredService;
import de.captaingoldfish.scim.sdk.client.ScimRequestBuilder;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import java.util.Optional;

/**
 * This is {@link ScimService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface ScimService {
    /**
     * The default bean name.
     */
    String BEAN_NAME = "defaultScimService";

    /**
     * Gets scim service.
     *
     * @param givenService the given service
     * @return the scim service
     */
    ScimRequestBuilder getScimService(Optional<RegisteredService> givenService);

    /**
     * Find user server response.
     *
     * @param scimService the scim service
     * @param uid         the uid
     * @return the server response
     */
    ServerResponse<ListResponse<User>> findUser(ScimRequestBuilder scimService, String uid);
}
