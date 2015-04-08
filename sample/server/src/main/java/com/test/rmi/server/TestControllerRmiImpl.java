package com.test.rmi.server;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import com.test.rmi.TestController;

/**
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 */
public class TestControllerRmiImpl extends UnicastRemoteObject implements TestController {

    private static final long serialVersionUID = 2662328879202619398L;

    public TestControllerRmiImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);
    }

    @Override
    public String f() throws RemoteException {
        return "Success";
    }

}
