package com.github.ignasbudreika.portfollow.helper;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import com.github.ignasbudreika.portfollow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDetailsHelper {
    private static final String USER_GOOGLE_ID = "2b8467a5-019a-4783-97fc-cac608081efb";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PortfolioRepository portfolioRepository;

    public void setUpUser(String userId, String userEmail, String username, String title, String description, boolean isPublic, boolean isHiddenValue) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(USER_GOOGLE_ID,
                "", List.of(new SimpleGrantedAuthority("USER")));

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken
                .setDetails(new WebAuthenticationDetailsSource());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        User user = userRepository.save(User.builder().id(userId).email(userEmail).username(username).googleId(USER_GOOGLE_ID).build());
        portfolioRepository.save(Portfolio.builder()
                .user(user)
                .title(title)
                .description(description)
                .published(isPublic)
                .hiddenValue(isHiddenValue)
                .build());
    }
}
