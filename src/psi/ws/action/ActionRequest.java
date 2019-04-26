package psi.ws.action;

import org.json.JSONObject;

import psi.ws.exception.ActionException;

public abstract class ActionRequest
{
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    abstract public String getRequestType();
    
    abstract public String getUsername();
    
    abstract public String getPassword();
    
    abstract public String generateRequestURL( JSONObject actionList ) throws ActionException; 
    
}
