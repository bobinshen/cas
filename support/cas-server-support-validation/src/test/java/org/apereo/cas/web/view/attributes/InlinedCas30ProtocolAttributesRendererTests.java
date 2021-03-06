package org.apereo.cas.web.view.attributes;

import lombok.val;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link InlinedCas30ProtocolAttributesRendererTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class InlinedCas30ProtocolAttributesRendererTests {
    @Test
    public void verifyAction() {
        val r = new InlinedCas30ProtocolAttributesRenderer();
        val results = (Map) CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap();
        assertFalse(r.render(results).isEmpty());
    }
}
