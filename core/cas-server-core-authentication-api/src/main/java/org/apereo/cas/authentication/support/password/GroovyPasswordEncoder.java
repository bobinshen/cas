package org.apereo.cas.authentication.support.password;

import lombok.RequiredArgsConstructor;
import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.springframework.security.crypto.password.AbstractPasswordEncoder;

/**
 * This is {@link GroovyPasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyPasswordEncoder extends AbstractPasswordEncoder {

    private final String scriptFile;

    @Override
    protected byte[] encode(final CharSequence rawPassword, final byte[] salt) {
        val resource = ApplicationContextProvider.getResourceLoader().getResource(this.scriptFile);
        final Object[] args = {rawPassword, salt, LOGGER, ApplicationContextProvider.getApplicationContext()};
        val result = ScriptingUtils.executeGroovyScript(resource, args, byte[].class);
        return result;
    }
}
