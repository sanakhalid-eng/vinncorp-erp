package com.vinncorp.erp.shared.security;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface JwtKeyProvider {
    PrivateKey getPrivateKey();
    PublicKey getPublicKey();
}
