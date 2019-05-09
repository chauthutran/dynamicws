package psi.ws.action;

import org.json.JSONObject;

import psi.ws.exception.ActionException;

public class ActionOutput
{
    private int responseCode = 200;

    private String actionEval;
    private String outputMsg = "";
    ActionJSEngine actionJsEngine;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ActionOutput( ActionJSEngine actionJsEngine )
    {
        super();
        this.actionJsEngine = actionJsEngine;
    }

    public ActionOutput( String outputMsg, int responseCode )
    {
        super();
        this.outputMsg = outputMsg;
        this.responseCode = responseCode;
    }

    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public int getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode( int responseCode )
    {
        this.responseCode = responseCode;
    }

    /**
     * If outputMsg is a string, then convert it to JSONObject, like this
     * {"output" : this.outputMsg }. Then convert JSONObject to String If
     * outputMsg is a JSONObject, then return this outputMsg
     * **/
    public String getOutputMsg()
    {
        String msg = this.outputMsg;
        JSONObject data = null;
        try
        {
            data = new JSONObject( msg );
        }
        catch ( Exception ex )
        {
            data = new JSONObject();
            data.put( "output", msg );
        }

        return data.toString();
    }

    public JSONObject getOutputJson()
    {
        return new JSONObject( this.getOutputMsg() );
    }

    public void setOutputMsg( String outputMsg )
    {
        this.outputMsg = outputMsg;
    }
    
    public void setActionEval( String actionEval )
    {
        this.actionEval = actionEval;
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public void evalOutput( String actionName )
        throws ActionException
    {
        this.actionJsEngine.addVariable( actionName, this.getOutputJson() );
        if( this.actionEval != null )
        {
            this.actionJsEngine.runScript( actionName, this.getOutputMsg(), true, this.actionEval );
            this.outputMsg = this.actionJsEngine.getJSONValue( actionName ).toString();
        }
    }
}
