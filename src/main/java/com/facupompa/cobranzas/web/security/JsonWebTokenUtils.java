package com.facupompa.cobranzas.web.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JsonWebTokenUtils {
    private final Algorithm algorithm;

    public JsonWebTokenUtils(@Value ("${security.jwt.secret}") String secretKey) {
        this.algorithm = Algorithm.HMAC256(secretKey);
    }

    public String create (String username) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(900);

        return JWT.create()
                .withSubject(username)
                .withIssuer("cobranzas-api")
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .sign(algorithm);
    }

    public String verify (String token) {
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("cobranzas-api")
                .withClaimPresence("exp")
                .withClaimPresence("sub")
                .build();

        DecodedJWT verifiedToken = verifier.verify(token);

        return verifiedToken.getSubject();
    }
}
