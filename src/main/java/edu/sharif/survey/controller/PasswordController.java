package edu.sharif.survey.controller;

import edu.sharif.survey.model.ResetRequest;
import edu.sharif.survey.service.ResetRequestService;
import edu.sharif.survey.util.exceptions.Message;
import edu.sharif.survey.util.exceptions.MessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import javax.websocket.server.PathParam;
import java.util.Optional;

@Controller
@Transactional
public class PasswordController {

    private final ResetRequestService requestService;
    private final Logger log;

    @Autowired
    public PasswordController(ResetRequestService requestService) {
        this.requestService = requestService;
        this.log = LoggerFactory.getLogger(PasswordController.class);
    }

    @RequestMapping(value = "/users/reset", method = RequestMethod.GET)
    public String displayResetPasswordPage(@PathParam("token") String token, RedirectAttributes redirectAttributes) {
        log.info("Validation check for token :\"" + token + "\" is requested.");
        Optional<ResetRequest> request = requestService.findByToken(token);
        if (request.isPresent()) {
            redirectAttributes.addAttribute("token", token);
            log.info("Redirecting to passwordReset page for token : \"" + token + "\"");
            return "index";
        } else {
            log.error("Token: \"" + token + "\" is invalid to reset password");
            System.err.println("errorMessage : Oops!  This is an invalid validPassword reset link.");
            throw new MessageException(Message.INVALID_RESET_LINK);
        }
    }


}
