package org.anax.framework.reporting.authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtClaims {
    protected String iss;
    protected long iat;
    protected long exp;
    protected String qsh;
    protected String sub;
}
