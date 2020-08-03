package edu.sharif.survey.usermanagement.details;


import java.util.ArrayList;

import edu.sharif.survey.usermanagement.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.sharif.survey.usermanagement.model.entity.User;

import lombok.Getter;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Getter
    private User user;

    @Override
    public UserDetails loadUserByUsername(String id) {
        User targetedUser = userDao.findById(Integer.valueOf(id))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        this.user = targetedUser;
        return new org.springframework.security.core.userdetails.User(targetedUser.getId().toString(), targetedUser.getPassword(), new ArrayList<>());
    }

}
