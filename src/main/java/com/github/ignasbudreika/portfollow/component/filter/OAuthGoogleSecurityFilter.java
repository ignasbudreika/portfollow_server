package com.github.ignasbudreika.portfollow.component.filter;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class OAuthGoogleSecurityFilter implements Filter {
    private GoogleIdTokenVerifier verifier;

    @Autowired
    private UserService userService;

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String path = ((HttpServletRequest) servletRequest).getRequestURI();
        if (path.startsWith("/api/oauth2/token")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.info("missing or invalid authorization header");
            throw new UnauthorizedException();
        }

        GoogleIdToken verified = null;
        try {
            verified = verifier.verify(authorizationHeader.replace("Bearer ", ""));
        } catch (GeneralSecurityException | IOException e) {
            log.error("failed id token verification", e.getMessage());
            throw new UnauthorizedException();
        }

        if (verified == null) {
            throw new UnauthorizedException();
        }

        if (!userService.existsByGoogleId(verified.getPayload().getSubject())) {
            throw new UnauthorizedException();
        }
        User user = userService.getByGoogleId(verified.getPayload().getSubject());

        // todo improve this flow
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getGoogleId(),
                "", List.of(new SimpleGrantedAuthority("USER")));

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken
                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
