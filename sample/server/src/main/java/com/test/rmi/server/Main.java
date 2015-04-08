package com.test.rmi.server;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import com.rmi.auth.AuthData;
import com.rmi.auth.AuthRMIClientSocketFactory;
import com.rmi.auth.AuthRMIServerSocketFactory;
import com.rmi.auth.Authorizer;
import com.test.rmi.TestController;

public class Main {

    private static final AuthRMIClientSocketFactory csf = new AuthRMIClientSocketFactory();
    private static final AuthRMIServerSocketFactory ssf = new AuthRMIServerSocketFactory(new Authorizer() {

        @Override
        public boolean authorize(AuthData authData) {
            return authData.login.equals("login") && authData.password.equals("password");
        }
    });

    public static void main(String[] args) throws Throwable {
        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        Naming.rebind(TestController.RMI_BINDING_NAME,
                new TestControllerRmiImpl(TestController.PORT, csf, ssf));

        while (true) {
            Thread.sleep(100);
        }
    }
}
