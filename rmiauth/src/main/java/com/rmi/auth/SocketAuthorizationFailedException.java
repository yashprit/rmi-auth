package com.rmi.auth;

import java.io.IOException;

/**
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 */
public class SocketAuthorizationFailedException extends IOException {

    SocketAuthorizationFailedException(String message) {
        super(message);
    }

}
