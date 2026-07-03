package com.gym.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ClientIpResolver {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";

    private final List<IpRange> trustedProxies;

    public ClientIpResolver(@Value("${rate-limit.trusted-proxies:}") String trustedProxies) {
        this.trustedProxies = parseTrustedProxies(trustedProxies);
    }

    public String resolve(HttpServletRequest request) {
        String immediateRemoteAddress = normalizeOrOriginal(request.getRemoteAddr());
        InetAddress immediateRemote = parseAddress(request.getRemoteAddr());
        if (immediateRemote == null || !isTrustedProxy(immediateRemote)) {
            return immediateRemoteAddress;
        }

        ForwardedForResult forwardedForResult = resolveForwardedFor(request.getHeader(X_FORWARDED_FOR));
        if (forwardedForResult.malformed()) {
            return immediateRemoteAddress;
        }
        if (forwardedForResult.clientIp() != null) {
            return forwardedForResult.clientIp();
        }

        String xRealIp = request.getHeader(X_REAL_IP);
        String resolvedRealIp = normalizeOrNull(xRealIp);
        return resolvedRealIp != null ? resolvedRealIp : immediateRemoteAddress;
    }

    private ForwardedForResult resolveForwardedFor(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return ForwardedForResult.absent();
        }

        String[] rawEntries = headerValue.split(",");
        List<InetAddress> chain = new ArrayList<>(rawEntries.length);
        for (String rawEntry : rawEntries) {
            String entry = rawEntry.trim();
            if (entry.isEmpty()) {
                return ForwardedForResult.invalid();
            }
            InetAddress address = parseAddress(entry);
            if (address == null) {
                return ForwardedForResult.invalid();
            }
            chain.add(address);
        }

        // Walk from the trusted proxy side and select the nearest untrusted hop.
        for (int i = chain.size() - 1; i >= 0; i--) {
            InetAddress candidate = chain.get(i);
            if (!isTrustedProxy(candidate)) {
                return ForwardedForResult.resolved(candidate.getHostAddress());
            }
        }
        return ForwardedForResult.absent();
    }

    private boolean isTrustedProxy(InetAddress address) {
        for (IpRange trustedProxy : trustedProxies) {
            if (trustedProxy.contains(address)) {
                return true;
            }
        }
        return false;
    }

    private static List<IpRange> parseTrustedProxies(String configuredProxies) {
        if (configuredProxies == null || configuredProxies.isBlank()) {
            return List.of();
        }

        List<IpRange> ranges = new ArrayList<>();
        for (String rawProxy : configuredProxies.split(",")) {
            String proxy = rawProxy.trim();
            if (!proxy.isEmpty()) {
                ranges.add(IpRange.parse(proxy));
            }
        }
        return List.copyOf(ranges);
    }

    private static String normalizeOrOriginal(String value) {
        String normalized = normalizeOrNull(value);
        return normalized != null ? normalized : value;
    }

    private static String normalizeOrNull(String value) {
        InetAddress address = parseAddress(value);
        return address == null ? null : address.getHostAddress();
    }

    private static InetAddress parseAddress(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (!isIpLiteral(normalized)) {
            return null;
        }
        try {
            return InetAddress.getByName(normalized);
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private static boolean isIpLiteral(String value) {
        if (value.indexOf(':') >= 0) {
            return value.matches("[0-9A-Fa-f:.]+");
        }
        return value.matches("[0-9.]+");
    }

    private record IpRange(InetAddress network, int prefixLength) {

        private static IpRange parse(String value) {
            String[] parts = value.split("/", -1);
            InetAddress address = parseAddress(parts[0]);
            if (address == null || parts.length > 2) {
                throw new IllegalArgumentException("Invalid rate-limit trusted proxy address/CIDR: " + value);
            }

            int bitLength = address.getAddress().length * 8;
            int prefix = bitLength;
            if (parts.length == 2) {
                try {
                    prefix = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid rate-limit trusted proxy CIDR prefix: " + value, ex);
                }
            }
            if (prefix < 0 || prefix > bitLength) {
                throw new IllegalArgumentException("Invalid rate-limit trusted proxy CIDR prefix: " + value);
            }
            return new IpRange(address, prefix);
        }

        private boolean contains(InetAddress address) {
            byte[] candidateBytes = address.getAddress();
            byte[] networkBytes = network.getAddress();
            if (candidateBytes.length != networkBytes.length) {
                return false;
            }

            BigInteger candidate = new BigInteger(1, candidateBytes);
            BigInteger networkValue = new BigInteger(1, networkBytes);
            int bitLength = networkBytes.length * 8;
            int hostBits = bitLength - prefixLength;
            BigInteger mask = BigInteger.ONE.shiftLeft(bitLength).subtract(BigInteger.ONE)
                    .shiftRight(hostBits)
                    .shiftLeft(hostBits);
            return candidate.and(mask).equals(networkValue.and(mask));
        }
    }

    private record ForwardedForResult(String clientIp, boolean malformed) {

        private static ForwardedForResult resolved(String clientIp) {
            return new ForwardedForResult(clientIp, false);
        }

        private static ForwardedForResult absent() {
            return new ForwardedForResult(null, false);
        }

        private static ForwardedForResult invalid() {
            return new ForwardedForResult(null, true);
        }
    }
}
