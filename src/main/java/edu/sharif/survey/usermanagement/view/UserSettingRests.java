package edu.sharif.survey.usermanagement.view;

import edu.sharif.survey.domain.AddUserDomain;
import edu.sharif.survey.domain.AuthenticationResponse;
import edu.sharif.survey.domain.SimpleUserDomain;
import edu.sharif.survey.domain.UserDomain;
import edu.sharif.survey.model.ResetRequest;
import edu.sharif.survey.service.EmailService;
import edu.sharif.survey.service.PasswordService;
import edu.sharif.survey.service.ResetRequestService;
import edu.sharif.survey.service.UserService;
import edu.sharif.survey.usermanagement.details.UserDetailsServiceImpl;
import edu.sharif.survey.usermanagement.model.entity.User;
import edu.sharif.survey.usermanagement.model.objects.AuthenticationRequest;
import edu.sharif.survey.usermanagement.utils.TokenUtil;
import edu.sharif.survey.util.exceptions.Message;
import edu.sharif.survey.util.exceptions.MessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static edu.sharif.survey.configuration.AuthenticationFilter.COOKIE_NAME;
import static edu.sharif.survey.configuration.AuthenticationFilter.TOKEN_PREFIX;

@Slf4j
@Transactional
@RestController
@RequestMapping("/user")
public class UserSettingRests {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private UserDetailsServiceImpl detailsService;
    @Autowired
    private ResetRequestService requestService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenUtil tokenUtil;

    @Value("${server_ip}")
    private String ip;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) throws MessageException {
        log.info(authenticationRequest.getEmail() + " wants to login *_*");
        String password = authenticationRequest.getPassword();
        User user = userService.findUserByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new MessageException(Message.EMAIL_NOT_FOUND));
        userService.authenticate(user.getId(), password);
        final UserDetails userDetails = detailsService.loadUserByUsername(String.valueOf(user.getId()));
        final String token = tokenUtil.generateToken(userDetails);
        log.info("the token will expired : " + tokenUtil.getExpirationDateFromToken(token));
        userService.addLoginInfo(user);
        assignCookie(token, response);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    private void assignCookie(String token, HttpServletResponse response) {
        response.addHeader("cookie", COOKIE_NAME + "=" + token);
    }

    @PostMapping("/edit")
    public ResponseEntity<SimpleUserDomain> edit(@RequestBody SimpleUserDomain userDomain) {
        log.info(detailsService.getUser().getName() + " with id " + detailsService.getUser().getId() + " try to edit his name or email");
        String email = userDomain.getEmail();
        String name = userDomain.getName();
        if (email != null)
            email = email.toLowerCase();
        User user = userService.edit(detailsService.getUser(), name, email);
        log.info(user.getName() + " changed his/her infos :)");
        SimpleUserDomain simpleUserDomain = new SimpleUserDomain(user.getName(), user.getEmail());
        return new ResponseEntity<>(simpleUserDomain, HttpStatus.OK);
    }

    @RequestMapping(value = "/edit/password", method = RequestMethod.POST)
    public ResponseEntity<SimpleUserDomain> setNewPassword(@RequestParam("oPassword") String oPassword, @RequestParam("nPassword") String nPassword) {
        log.info(detailsService.getUser().getEmail() + " tries to change password");
        User user = detailsService.getUser();
        if (nPassword == null || oPassword == null || nPassword.length() < 6) {
            log.error("Invalid password input to be changed.");
            throw new MessageException(Message.PASSWORD_TOO_LOW);
        }
        if (!passwordService.getPasswordEncoder().matches(oPassword, user.getPassword())) {
            log.error("OldPassword doesn't match user's password");
            throw new MessageException(Message.PASSWORD_INCORRECT);
        }
        log.info("Password changed for : " + detailsService.getUser().getEmail());
        return new ResponseEntity<>(userService.changePassword(user, nPassword), HttpStatus.OK);
    }

    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<SimpleUserDomain> add(@RequestBody AddUserDomain userDomain) {
        log.info("someone try to sign up ._.");
        if (userDomain.getName() == null || userDomain.getName().length() < 1)
            throw new MessageException(Message.NAME_NULL);
        if (userDomain.getEmail() == null)
            throw new MessageException(Message.EMAIL_NULL);
        if (userDomain.getPassword() == null)
            throw new MessageException(Message.PASSWORD_TOO_LOW);
        userDomain.setEmail(userDomain.getEmail().toLowerCase());
        SimpleUserDomain user = userService.addUser(userDomain.getName(), userDomain.getEmail(), userDomain.getPassword());
        log.info(user.getName() + " added to DB :)");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDomain> one() {
        log.info(detailsService.getUser().getName() + " with id " + detailsService.getUser().getId() + " try to get him/her infos");
        UserDomain user = userService.toUserDomain(detailsService.getUser());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @RequestMapping(value = "/forgot", method = RequestMethod.POST)
    public ResponseEntity<SimpleUserDomain> processForgotPasswordForm(@RequestParam("email") String userEmail, HttpServletRequest request) {
        userEmail = userEmail.toLowerCase();
        log.info(userEmail + " has just requested for password recovery email.");
        Optional<User> optional = userService.findUserByEmail(userEmail);
        if (!optional.isPresent()) {
            log.error("There's not account found for " + userEmail);
            throw new MessageException(Message.EMAIL_NOT_FOUND);
        } else {
            User user = optional.get();
            String appUrl = request.getScheme() + "://" + ip;
            try {
                emailService.sendPassRecoveryMail(user, appUrl, requestService.registerResetRequest(user));
                log.info("A password reset link has been sent to " + userEmail + " @" +
                        new Date());
            } catch (Exception e) {
                log.error("Failed to send email to " + userEmail + " ," + e);
            }
            return new ResponseEntity<>(new SimpleUserDomain(user.getName(), userEmail), HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public ResponseEntity<SimpleUserDomain> setNewPassword(@RequestParam Map<String, String> requestParams) {
        String token = requestParams.get("token"), password = requestParams.get("validPassword");
        log.info("Reset password request received for token :\"" + token + "\"");
        ResetRequest request = requestService.findByToken(token).orElseGet(() -> {
            log.error("No request recorded for token: \"" + token + "\"");
            throw new MessageException(Message.TOKEN_NOT_FOUND);
        });
        User resetUser = request.getUser();
        if (resetUser != null) {
            if (password == null || password.length() < 6)
                throw new MessageException(Message.PASSWORD_TOO_LOW);
            if (password.length() > 100)
                throw new MessageException(Message.PASSWORD_TOO_HIGH);
            requestService.removeRequest(request);
            log.info(resetUser.getEmail() + " successfully reset his/her password");
            return new ResponseEntity<>(userService.changePassword(resetUser, password), HttpStatus.OK);
        } else {
            log.error("Invalid password reset link.");
            throw new MessageException(Message.NOT_RECORDED_REQUEST);
        }
    }

}
