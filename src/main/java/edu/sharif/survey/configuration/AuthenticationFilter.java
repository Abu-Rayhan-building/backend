package edu.sharif.survey.configuration;

import edu.sharif.survey.usermanagement.details.UserDetailsServiceImpl;
import edu.sharif.survey.usermanagement.utils.JwtTokenUtil;
import edu.sharif.survey.usermanagement.utils.TokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static edu.sharif.survey.util.ConvertUtil.emptyIfNull;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private UserDetailsService userDetailsService;
    private TokenUtil tokenUtil;

    public static final String COOKIE_NAME = "JSESSIONID";
    public static final String TOKEN_PREFIX = "Bearer";

    @Autowired
    public AuthenticationFilter(UserDetailsServiceImpl userDetailsService, JwtTokenUtil tokenUtil) {
        this.userDetailsService = userDetailsService;
        this.tokenUtil = tokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<Cookie> tokenContainedCookie = Arrays.stream(
                emptyIfNull(request.getCookies(), Cookie.class))
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()) &&
                        cookie.getValue() != null)
                .findAny();
        if (tokenContainedCookie.isPresent()) {
            extractUserId(tokenContainedCookie.get(), request);
        } else
            logger.warn("No cookie in expected form was found");
        chain.doFilter(request, response);
    }

    private void extractUserId(Cookie cookie, HttpServletRequest request) {
        String id = null;
        final String jwtToken = cookie.getValue();
        try {
            id = tokenUtil.getIdFromToken(jwtToken);
        } catch (IllegalArgumentException e) {
            logger.warn("Unable to get JWT Token");
        } catch (ExpiredJwtException e) {
            logger.warn("JWT Token has expired");
        }
        if (id != null)
            validateUser(id, jwtToken, request);
        else
            logger.warn("Could not extract user from token");
    }

    private void validateUser(String id, String jwtToken, HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(id);
            // if token is valid configure Spring Security to manually set authentication
            if (tokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
    }
}
