package edu.sharif.survey.configuration;

import edu.sharif.survey.usermanagement.model.entity.User;
import edu.sharif.survey.service.UserService;
import edu.sharif.survey.usermanagement.utils.JwtTokenUtil;
import edu.sharif.survey.util.exceptions.MessageException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtTokenUtil tokenUtil;
    @Autowired
    private UserService userService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/auction", "/app") //socket_subscriber
                .enableSimpleBroker("/app", "/auction"); //socket_publisher
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/socket")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (headerAccessor != null && StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
                    log.info("try to connect");
                    String jwtToken = Objects.requireNonNull(headerAccessor.getFirstNativeHeader("auth")).substring(7);
                    String deviceId = Objects.requireNonNull(headerAccessor.getFirstNativeHeader("deviceID"));
                    if (deviceId == null)
                        throw new MessageException(edu.sharif.survey.util.exceptions.Message.USER_NOT_FOUND);
                    String id = tokenUtil.getIdFromToken(jwtToken);
                    if (tokenUtil.isTokenExpired(jwtToken))
                        throw new MessageException(edu.sharif.survey.util.exceptions.Message.TOKEN_NOT_FOUND);
                    User user = userService.findUserId(Integer.valueOf(id));
//                    bidService.addDeviceId(deviceId, user);
                    Authentication u = new UsernamePasswordAuthenticationToken(deviceId, user.getPassword(), new ArrayList<>());
                    headerAccessor.setUser(u);
                    log.info("the user with id " + user.getId() + " connected with deviceId " + deviceId);
                }
                if (headerAccessor != null && StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
                    System.err.println(headerAccessor.getDestination());
                }
                  /*
                if (headerAccessor != null && StompCommand.DISCONNECT.equals(headerAccessor.getCommand()) && headerAccessor.getUser() != null) {

                    Integer userId = bidService.getUserId(headerAccessor.getUser().getName());
                    if (userId == null)
                        return message;
                    User user = userService.findUserId(userId);
                    bidService.removeFromAllAuction(user);
                    bidService.removeDeviceId(headerAccessor.getUser().getName());


                }
                    */

                return message;
            }
        });
    }

}

