package psi.ws.action;

import psi.ws.configuration.Configuration;
import psi.ws.exception.ActionException;

public abstract class ActionRequest
{
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    abstract public String getRequestType();
    
    abstract public Configuration getConfiguration();
    
    abstract public void sendRequest( Action action ) throws ActionException;
    
}
