/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Created: July 30, 2026
 *
 */

package com.odysseusinc.athena.security.hmac;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The signature troubleshooting aid writes a <em>correct</em> signature for a rejected request
 * into the application log. That line is enough to replay or forge the request, so it must not
 * be reachable by configuration alone — it is now gated on {@code api.debug-signatures}, which
 * defaults to false.
 * <p>
 * These assertions read the log output rather than the flag, because the log is the thing that
 * actually leaks.
 * <p>
 * JUnit 4 on purpose.
 */
public class ApiClientsDebugSignatureTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @Before
    public void captureLogging() {

        logger = (Logger) LoggerFactory.getLogger(ApiClients.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);
    }

    @After
    public void releaseLogging() {

        logger.detachAppender(appender);
    }

    private String loggedOutput() {

        StringBuilder all = new StringBuilder();
        for (ILoggingEvent event : appender.list) {
            all.append(event.getFormattedMessage()).append('\n');
        }
        return all.toString();
    }

    /** A client with both halves of a DSA keypair configured — the troubleshooting setup. */
    private ApiClients clientsWithPrivateKey(boolean debugSignatures) throws Exception {

        KeyPair pair = KeyPairGenerator.getInstance("DSA").generateKeyPair();

        ApiClients.ApiClient client = new ApiClients.ApiClient();
        client.setAlgorithm("SHA256withDSA");
        client.setPublicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        client.setPrivateKey(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));

        ApiClients clients = new ApiClients();
        ReflectionTestUtils.setField(clients, "clients", Collections.singletonMap("pd", client));
        clients.setDebugSignatures(debugSignatures);
        return clients;
    }

    private void submitABadSignature(ApiClients clients) throws Exception {

        List<byte[]> factors = Collections.singletonList("GET/api/s2s/vocabularies".getBytes("UTF-8"));
        String wrong = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3});

        assertFalse("the request must still be rejected",
                clients.getSignatureVerifier("pd").apply(factors, wrong));
    }

    @Test
    public void doesNotLogACorrectSignatureByDefault() throws Exception {

        ApiClients clients = clientsWithPrivateKey(false);

        submitABadSignature(clients);

        assertFalse("a private key alone must not produce a forgeable log line: " + loggedOutput(),
                loggedOutput().contains("Correct signature"));
    }

    @Test
    public void logsACorrectSignatureOnlyWhenExplicitlyEnabled() throws Exception {

        ApiClients clients = clientsWithPrivateKey(true);

        submitABadSignature(clients);

        assertTrue("the aid must still work when asked for: " + loggedOutput(),
                loggedOutput().contains("Correct signature"));
    }

    /** A private key left in a deployed configuration is called out rather than used. */
    @Test
    public void warnsThatAConfiguredPrivateKeyIsIgnored() throws Exception {

        ApiClients clients = clientsWithPrivateKey(false);

        clients.init();

        assertTrue(loggedOutput(), loggedOutput().contains("It is being ignored"));
    }

    @Test
    public void warnsLoudlyWhenTheAidIsEnabled() throws Exception {

        ApiClients clients = clientsWithPrivateKey(true);

        clients.init();

        assertTrue(loggedOutput(), loggedOutput().contains("api.debug-signatures is enabled"));
    }

    /**
     * A client with no public key used to have a keypair generated for it and printed. That
     * never made the client work — it cannot sign with a key it was never given — so outside
     * the debug mode the misconfiguration is reported instead, with no key material.
     */
    @Test
    public void doesNotGenerateAKeypairForAMisconfiguredClientByDefault() {

        ApiClients.ApiClient client = new ApiClients.ApiClient();
        client.setAlgorithm("SHA256withDSA");

        ApiClients clients = new ApiClients();
        ReflectionTestUtils.setField(clients, "clients", Collections.singletonMap("pd", client));

        clients.init();

        assertFalse("no keypair should be fabricated", loggedOutput().contains("generated a transient pair"));
        assertTrue(loggedOutput(), loggedOutput().contains("No public key configured"));
        assertFalse("no private key should exist to leak", client.getPrivateKey() != null);
    }
}
