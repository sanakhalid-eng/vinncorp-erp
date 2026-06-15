package com.vinncorp.erp.shared.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

@Component
public class WebhookUrlValidator {

    private static final Logger log = LoggerFactory.getLogger(WebhookUrlValidator.class);

    private static final String[] BLOCKED_PROTOCOLS = {"file:", "ftp:", "ftps:", "sftp:", "ldap:", "ldaps:", "jar:", "javascript:"};

    private static final int[][] PRIVATE_RANGES = {
        {0x0A000000, 0x0AFFFFFF},   // 10.0.0.0/8
        {0x7F000000, 0x7FFFFFFF},   // 127.0.0.0/8
        {0xA9FE0000, 0xA9FEFFFF},   // 169.254.0.0/16
        {0xAC100000, 0xAC1FFFFF},   // 172.16.0.0/12
        {0xC0A80000, 0xC0A8FFFF},   // 192.168.0.0/16
        {0x00000000, 0x0000FFFF},   // 0.0.0.0/8 (current network)
    };

    public void validate(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("URL must have a scheme: " + url);
        }

        for (String blocked : BLOCKED_PROTOCOLS) {
            if (scheme.equalsIgnoreCase(blocked.replace(":", ""))) {
                throw new IllegalArgumentException("Protocol '" + scheme + "' is not allowed for webhooks");
            }
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Only http and https protocols are allowed for webhooks");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL must have a host: " + url);
        }

        String lowerHost = host.toLowerCase();
        if (lowerHost.equals("localhost") || lowerHost.equals("127.0.0.1") || lowerHost.equals("0.0.0.0")
                || lowerHost.equals("255.255.255.255") || lowerHost.equals("::1")) {
            throw new IllegalArgumentException("Webhook URL cannot point to localhost or loopback address");
        }

        try {
            InetAddress resolved = InetAddress.getByName(host);
            if (resolved instanceof Inet4Address) {
                int ip = ipToInt(resolved.getAddress());
                for (int[] range : PRIVATE_RANGES) {
                    if (ip >= range[0] && ip <= range[1]) {
                        throw new IllegalArgumentException("Webhook URL points to a private or internal IP range");
                    }
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Webhook URL host could not be resolved: " + host);
        }
    }

    private int ipToInt(byte[] address) {
        return ((address[0] & 0xFF) << 24) |
               ((address[1] & 0xFF) << 16) |
               ((address[2] & 0xFF) << 8) |
               (address[3] & 0xFF);
    }
}

