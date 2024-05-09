package com.odysseusinc.athena.security.hmac;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class HmacVerifyingFilter extends OncePerRequestFilter {

    public static final String ATTRIBUTE_HMAC_VALID = "HMAC_VALID";

    public static final List<String> METHODS = ImmutableList.of("GET", "HEAD", "POST", "PUT", "DELETE");
    public static final List<String> BODY_METHODS = ImmutableList.of("POST", "PUT");

    public static final String HEADER_CLIENT_ID = "X-Athena-Client-Id";
    public static final String HEADER_HMAC = "X-Athena-Hmac";
    public static final String HEADER_NONCE = "X-Athena-Nonce";

    @Autowired
    private ApiClients clients;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String clientId = request.getHeader(HEADER_CLIENT_ID);
        if (clientId != null) {
            if (METHODS.contains(method)) {
                if (BODY_METHODS.contains(method)) {
                    byte[] payload = StreamUtils.copyToByteArray(request.getInputStream());
                    boolean result = verify(request, clientId, Optional.of(payload));
                    request.setAttribute(ATTRIBUTE_HMAC_VALID, result);
                    CachedPayloadRequestWrapper wrapper = new CachedPayloadRequestWrapper(request, payload);
                    filterChain.doFilter(wrapper, response);
                } else {
                    boolean result = verify(request, clientId, Optional.empty());
                    request.setAttribute(ATTRIBUTE_HMAC_VALID, result);
                    filterChain.doFilter(request, response);
                }
            } else {
                throw new HttpRequestMethodNotSupportedException("HTTP method [" + method + "] is not supported with [" + HEADER_CLIENT_ID + "] authentication mode");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean verify(HttpServletRequest request, String clientId, Optional<byte[]> payload) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String signature = request.getHeader(HEADER_HMAC);
        try {
            String nonce = verifyNonce(request);
            log.info("Verifying signature for method: {}, URI: {}, query string: {}, nonce: {}", method, uri, queryString, nonce);
            Stream<byte[]> meta = Stream.of(method, uri, queryString, nonce).filter(Objects::nonNull).map(s -> s.getBytes(StandardCharsets.UTF_8));
            List<byte[]> factors = payload.map(body -> Stream.concat(meta, Stream.of(body))).orElse(meta).collect(Collectors.toList());
            Boolean verificationResult = clients.getSignatureVerifier(clientId).apply(factors, signature);
            log.info("Signature verification result: {}", verificationResult);
            return verificationResult;
        } catch (BadCredentialsException e) {
            log.info("Signature verification failed for [" + uri + "]: " + e.getMessage());
            return false;
        }
    }

    private String verifyNonce(HttpServletRequest request) {
        Instant now = Instant.now();
        String nonce = Optional.ofNullable(request.getHeader(HEADER_NONCE)).orElseThrow(() ->
                new BadCredentialsException("Missing [" + HEADER_NONCE + "] header, reference value [" + now.toString() + "]")
        );
        try {
            Instant parse = Instant.parse(nonce);
            TemporalAmount tolerance = clients.getTimeTolerance();
            if (now.plus(tolerance).isBefore(parse) && now.minus(tolerance).isAfter(parse)) {
                log.info("NONCE {} is too far from current time {}, possible replay attack or remote system clock desync", nonce, now);
                throw new BadCredentialsException("Invalid noonce [" + nonce + "]");
            }
            return nonce;
        } catch (DateTimeParseException e) {
            log.info("Unparseable nonce [{}], valid musth be within tolerance from [{}]", nonce, now);
            throw new BadCredentialsException("Unparseable nonce [" + nonce + "]");
        }
    }


}