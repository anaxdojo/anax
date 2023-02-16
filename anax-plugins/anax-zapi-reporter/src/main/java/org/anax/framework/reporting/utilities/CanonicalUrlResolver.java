package org.anax.framework.reporting.utilities;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

public class CanonicalUrlResolver {

    public static String getCanonicalUrl(HttpMethod httpMethod, String requestUrl, String baseUrlToRemove) {
        String canonical = requestUrl;
        try {
            URL url = new URL(requestUrl);
            StringBuilder sortedQueryBuilder = new StringBuilder();
            if (StringUtils.hasLength(url.getQuery()) && url.getQuery().split("&").length != 0) {
                Arrays.stream(url.getQuery().split("&")).sorted().forEach(query -> sortedQueryBuilder.append(query).append("&"));
                sortedQueryBuilder.deleteCharAt(sortedQueryBuilder.lastIndexOf("&"));
            }
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), sortedQueryBuilder.toString(), url.getRef());
            canonical = uri.toString().replace(baseUrlToRemove, "").replace("?", "&");
            canonical = httpMethod + "&" + canonical;
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return canonical;
    }
}
