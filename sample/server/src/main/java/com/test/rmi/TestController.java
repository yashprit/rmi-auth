package com.test.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 */
public interface TestController extends Remote {

    public static final String RMI_BINDING_NAME = "Test";
    public static final int PORT = 45223;

    public String f() throws RemoteException;

}
