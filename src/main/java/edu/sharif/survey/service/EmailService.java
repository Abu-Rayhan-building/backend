package edu.sharif.survey.service;

import edu.sharif.survey.usermanagement.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@EnableAsync
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPassRecoveryMail(User user, String appUrl, String token) throws MessagingException {
        String userEmail = user.getEmail();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("fivegears.rahnema@gmail.com");
        helper.setTo(userEmail);
        String link = appUrl + "/users/reset?token=" + token;
        String text = "<html>\n" +
                "\n" +
                "<body dir=\"rtl\">\n" +
                "    <div>\n" +
                "        <h1>\n" +
                user.getName() +
                "            عزیز! سلام.\n" +
                "\n" +
                "        </h1>\n" +
                "        <h3>\n" +
                "            جهت بازنشانی رمز عبور خود بر روی لینک زیر کلیک کنید.\n" +
                "        </h3>\n" +
                "        <h2 dir=\"ltr\">\n" +
                link +
                "        </h2>\n" +
                "        <h3>\n" +
                "            در صورتی که شما درخواست بازنشانی نداده اید این ایمیل را نادیده بگیرید.\n" +
                "            <br>\n" +
                "            یاعلی. در پناه حق!\n" +
                "        </h3>\n" +
                "    </div>\n" +
                "\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";
        helper.setText(text, true);
        helper.setSubject("بازنشانی رمزعبور AucApp");
        mailSender.send(message);
    }

}
