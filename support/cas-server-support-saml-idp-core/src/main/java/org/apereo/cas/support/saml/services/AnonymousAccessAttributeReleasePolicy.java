package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.CollectionUtils;
import java.io.Serial;
import java.util.Map;

/**
 * This is {@link AnonymousAccessAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AnonymousAccessAttributeReleasePolicy extends BaseEntityCategoryAttributeReleasePolicy {
    /**
     * Map of allowed attributes by this policy in the form of attribute name linked
     * to its equivalent urn value.
     */
    public static final Map<String, String> ALLOWED_ATTRIBUTES = CollectionUtils.wrap(
        "schacHomeOrganization", "urn:oid:1.3.6.1.4.1.25178.1.2.9",
        "eduPersonScopedAffiliation", "urn:oid:1.3.6.1.4.1.5923.1.1.1.9");
    
    @Serial
    private static final long serialVersionUID = 4885600380662937851L;

    @Override
    protected Map<String, String> getEntityCategoryAttributes() {
        return ALLOWED_ATTRIBUTES;
    }
}
