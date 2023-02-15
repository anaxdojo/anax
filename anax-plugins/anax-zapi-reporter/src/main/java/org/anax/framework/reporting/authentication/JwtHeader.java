package org.anax.framework.reporting.authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtHeader {
    protected String alg;
    protected String typ;
}
