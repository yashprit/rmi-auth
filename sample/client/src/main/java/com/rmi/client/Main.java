package com.rmi.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import com.rmi.auth.AuthData;
import com.rmi.auth.AuthRMIClientSocketFactory;
import com.test.rmi.TestController;

public class Main {

    public static void main(String[] args) throws Throwable {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("login: ");
        String login = br.readLine();
        System.out.println();

        System.out.print("password: ");
        String password = br.readLine();
        System.out.println();

        AuthRMIClientSocketFactory.setHostAuthData("127.0.0.1",
                new AuthData(login, password));

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        TestController test = (TestController) Naming.lookup(TestController.RMI_BINDING_NAME);

        System.out.println(test.f());
    }
}
