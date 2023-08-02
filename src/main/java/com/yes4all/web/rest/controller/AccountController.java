package com.yes4all.web.rest.controller;

import com.yes4all.domain.model.AdminUserDTO;
import com.yes4all.domain.model.UserDTO;
import com.yes4all.service.impl.UserService;
import com.yes4all.web.rest.payload.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountController {

    private static class AccountResourceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final UserService userService;
    private static final String AUTHORIZATION_HEADER = "Authorization";

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @param request the current user; resolves to {@code null} if not authenticated.
     * @return the current user.
     * @throws AccountResourceException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    @SuppressWarnings("unchecked")
    public AdminUserDTO getAccount(HttpServletRequest request) {
        final String authorizationHeaderValue = request.getHeader(AUTHORIZATION_HEADER);
        String jwtToken ="";
        if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer")) {
            jwtToken = authorizationHeaderValue.substring(7, authorizationHeaderValue.length());
         }
        if(jwtToken.isEmpty() || jwtToken.isBlank()){
            throw new AccountResourceException("Can't get token from request");
        }
        return  userService.getUserFromAuthentication(jwtToken);


    }


    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    @GetMapping("/roles/{role}")
    public ResponseEntity<RestResponse<Object>> findAllWithRole(@PathVariable String role) {
        List<UserDTO> response = userService.getAllUserWithRole(role);
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }
    @GetMapping("/account/{login}")
    public ResponseEntity<RestResponse<Object>> findOneWithLogin(@PathVariable String login) {
        boolean response = userService.getOneUserWithLogin(login);
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }
    @GetMapping("/account/info/{email}")
    public ResponseEntity<RestResponse<Object>> findOneInfoWithEmail(@PathVariable String email) {
        UserDTO response = userService.getInfoOneUserWithEmail(email);
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }
//    @GetMapping("/account/info/testMail")
//    public ResponseEntity<RestResponse<Object>> testMail() {
//        UserDTO response = userService.testMail();
//        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
//    }
}
