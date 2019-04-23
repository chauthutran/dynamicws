package psi.ws.action;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import psi.ws.util.Util;

public class Action
{
    public static final String ACTION_TYPE_DHIS = "DHIS";
    public static final String ACTION_TYPE_FOCUSONE = "FOCUSONE";
    public static final String ACTION_TYPE_LOCAL_SMS_JSON = "LOCAL_SMS_JSON";
    public static final String ACTION_TYPE_LOCAL_SMS_POST_FORM = "LOCAL_SMS_POST_FORM";
    public static final String ACTION_TYPE_JAVASCRIPT = "JS";
    
    private String name;
    private String type;
    private ActionInput input;
    private ActionRequest request;
    private ActionOutput output;
    private ActionGoTo goTo;
    

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Action( String name, ActionInput input, ActionRequest request, ActionGoTo goTo )
    {
        super();
        this.name = name;
        this.input = input;
        this.request = request;
        this.goTo = goTo;
        this.output = new ActionOutput();
    }
    
    
    /** {
        "name": "1_ClientCreate",
        "type": "DhisApi",
        "URL": "/api/trackedEntityInstances",
        "input": {"actionName":"PAYLOAD"},
        "RESTType": "POST",
        "actionEval": "if( %%OUTPUT%%.response.status == \"SUCCESS\") { %%OUTPUT%%.trackedEntityInstance = %%OUTPUT%%.response.importSummaries[0].reference; } else { %%OUTPUT%%.errorMsg = '{ERROR}-Client Create Failed: {OUT_ERR}' };",
        "goTo": "( %%OUTPUT%%.response.status == \"SUCCESS\" ) ? {%%2_ClientGet%%} : {%%5_END%%}"
    } **/
    public Action( HttpServletRequest httpRequest, JSONObject actionData, Action prevAction, JSONObject configActionListByName, ScriptEngine jsEngine, String serverName, String username, String password ) throws IOException, Exception
    {
        // ---------------------------------------------------------------------
        // Get name
        String name = actionData.getString( "name" );
        String type = actionData.getString( "type" );
       
       
        // ---------------------------------------------------------------------
        // ActionInput
        
        // Generate "input" property from previous action if there is not defination
        // "input" will get from outPut of previous action
        if( !actionData.has( "input" ) && prevAction != null )
        {
            JSONObject inputDefined = new JSONObject();
            inputDefined.put( "actionName", "%%" + prevAction.getName() + "%%" );
            actionData.put( "input", inputDefined );
        }
        
        // Create ActionInput object
        ActionInput input = null;
        String inputStr = null;
        Object inputData = actionData.get( "input" );
        if( inputData instanceof String )
        {
            inputStr = inputData.toString();
        }
        else if( inputData instanceof JSONObject )
        {
            inputStr = ( ( JSONObject ) inputData ).toString();
        }
        
        input = new ActionInput( inputStr, httpRequest, configActionListByName );
        
        // ---------------------------------------------------------------------
        // ActionRequest && Output
        ActionRequest actionRequest = null;
        this.output = new ActionOutput( jsEngine );
        
        if( type.equals( Action.ACTION_TYPE_DHIS ) )
        {
            String link = actionData.getString( "URL" );
            String requestType = actionData.getString( "RESTType" );
            actionRequest = new ActionDhisRequest( serverName, link, requestType, username, password );
        }
        else if( type.equals( Action.ACTION_TYPE_JAVASCRIPT ) )
        {
            if( prevAction != null )
            {
                this.output.setOutputMsg( prevAction.getOutput().getOutputMsg() );
            }
        }
        
        // Get "actionEval" property and set for output object if any
        if( actionData.has( "actionEval" ) )
        {
            this.output.setActionEval( actionData.getString( "actionEval" ) );
        }
       
        
        // ---------------------------------------------------------------------
        // ActionGoTo 
        ActionGoTo goTo = new ActionGoTo( actionData.getInt( "currentIndex" ) ); // This property value is added from "ActionSet" class

        if( actionData.has( "goTo" ) )
        {
            goTo.setGoToDefination( actionData.getString( "goTo" ) );
        }
        
        this.name = name;
        this.input = input;
        this.request = actionRequest;
        this.goTo = goTo;
        this.type = type;
    }

    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public ActionInput getInput()
    {
        return input;
    }

    public void setInput( ActionInput input )
    {
        this.input = input;
    }

    public ActionOutput getOutput()
    {
        return output;
    }

    public void setOutput( ActionOutput output )
    {
        this.output = output;
    }

    public ActionRequest getRequest()
    {
        return request;
    }
    
    public void setRequest( ActionRequest request )
    {
        this.request = request;
    }

    public ActionGoTo getGoTo()
    {
        return goTo;
    }

    public void setGoTo( ActionGoTo goTo )
    {
        this.goTo = goTo;
    }

    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public void run( JSONObject actionList ) throws IOException, Exception
    {
        if( this.request != null )
        {
            if( this.type.equals( Action.ACTION_TYPE_DHIS ) )
            {
                Util.sendRequest( this, actionList );
            }
            
        }
        else if( this.type.equals( Action.ACTION_TYPE_JAVASCRIPT ) )
        {
            // this.output.evalOutput();
        }

        this.output.evalOutput();
    }

    
}
