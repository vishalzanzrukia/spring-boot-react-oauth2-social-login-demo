package com.example.springsocial.converter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Original class OAuth2AccessTokenResponseHttpMessageConverter
 */
public class CustomOAuth2AccessTokenResponseHttpMessageConverter extends OAuth2AccessTokenResponseHttpMessageConverter {

    private MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();

    private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE =
            new ParameterizedTypeReference<Map<String, Object>>() {
            };

    //in original class it's Map<String, String> which does not work for all response
    protected Converter<Map<String, Object>, OAuth2AccessTokenResponse> tokenResponseConverter =
            new CustomOAuth2AccessTokenResponseConverter();

    @Override
    public OAuth2AccessTokenResponse readInternal(Class<? extends OAuth2AccessTokenResponse> clazz, HttpInputMessage inputMessage)
            throws HttpMessageNotReadableException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponseParameters = (Map<String, Object>) jackson2HttpMessageConverter.read(
                    PARAMETERIZED_RESPONSE_TYPE.getType(), null, inputMessage);
            return this.tokenResponseConverter.convert(tokenResponseParameters);
        } catch (Exception ex) {
            throw new HttpMessageNotReadableException("An error occurred reading the OAuth 2.0 Access Token Response: " +
                    ex.getMessage(), ex, inputMessage);
        }
    }

    /**
     * Read response from token exchange end-point and prepare {@link OAuth2AccessTokenResponse}
     * <p>
     * Refer OAuth2AccessTokenResponseHttpMessageConverter --> OAuth2AccessTokenResponseConverter for original class
     */
    private static class CustomOAuth2AccessTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {
        private static final Set<String> TOKEN_RESPONSE_PARAMETER_NAMES = new HashSet<>(Arrays.asList(
                OAuth2ParameterNames.ACCESS_TOKEN,
                OAuth2ParameterNames.TOKEN_TYPE,
                OAuth2ParameterNames.EXPIRES_IN,
                OAuth2ParameterNames.REFRESH_TOKEN,
                OAuth2ParameterNames.SCOPE
        ));

        @Override
        public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
            String accessToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);

            OAuth2AccessToken.TokenType accessTokenType = null;
            if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase((String)
                    tokenResponseParameters.get(OAuth2ParameterNames.TOKEN_TYPE))) {
                accessTokenType = OAuth2AccessToken.TokenType.BEARER;
            }

            long expiresIn = 0;
            if (tokenResponseParameters.containsKey(OAuth2ParameterNames.EXPIRES_IN)) {
                try {
                    expiresIn = Long.valueOf((Integer) tokenResponseParameters.get(OAuth2ParameterNames.EXPIRES_IN));
                } catch (NumberFormatException ex) {
                    System.err.println("Error while reading expire in from response : " + ex.getMessage());
                }
            }

            Set<String> scopes = Collections.emptySet();
            if (tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)) {
                String scope = (String) tokenResponseParameters.get(OAuth2ParameterNames.SCOPE);
                scopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
            }

            String refreshToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.REFRESH_TOKEN);

            Map<String, Object> additionalParameters = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : tokenResponseParameters.entrySet()) {
                if (!TOKEN_RESPONSE_PARAMETER_NAMES.contains(entry.getKey())) {
                    additionalParameters.put(entry.getKey(), entry.getValue());
                }
            }

            if (tokenResponseParameters.containsKey("data")) {
                additionalParameters.putAll((Map<String, String>) tokenResponseParameters.get("data"));
            }

            return OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(accessTokenType)
                    .expiresIn(expiresIn)
                    .scopes(scopes)
                    .refreshToken(refreshToken)
                    .additionalParameters(additionalParameters)
                    .build();
        }
    }
}
