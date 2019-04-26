package psi.ws.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import psi.ws.exception.ActionException;
import psi.ws.exception.ActionInputException;
import psi.ws.exception.ActionPropertyException;
import psi.ws.util.Util;

public class Action
{
    public static final String CONFIG_PARAM_SIGN_START = "[";
    public static final String CONFIG_PARAM_SIGN_END = "]";
    public static final String REQEXP_NAME = "^[a-zA-Z_][a-zA-Z0-9_]*";
    
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
    }
    
    
    /** {
        "name": "1_ClientCreate",
        "type": "DhisApi",
        "URL": "/api/trackedEntityInstances",
        "input": {"actionName":"PAYLOAD"},
        "RESTType": "POST",
        "actionEval": "if( [OUTPUT].response.status == \"SUCCESS\") { [OUTPUT].trackedEntityInstance = [OUTPUT].response.importSummaries[0].reference; } else { [OUTPUT].errorMsg = '{ERROR}-Client Create Failed: {OUT_ERR}' };",
        "goTo": "( [OUTPUT].response.status == \"SUCCESS\" ) ? {[2_ClientGet]} : {[5_END]}"
    } 
     * @throws ActionException 
    **/
    public Action( HttpServletRequest httpRequest, JSONObject actionData, Action prevAction, JSONObject actionList, ActionJSEngine actionJsEngine, String serverName, String username, String password ) throws ActionException 
    {
        // Get name && type
        if( !actionData.has( "name" ) )
        {
            throw new ActionPropertyException("name");
        }
        else
        {
            String name = actionData.getString( "name" );
            boolean valid = checkNameValid( name );
            if( !valid )
            {
                throw new ActionException("The name of action is invalid. Please provide a name which is not start as a number; no white space in the name and no special characters");
            }
        }
        
        if( !actionData.has( "type" ) )
        {
            throw new ActionPropertyException("type");
        }
        
        this.name = actionData.getString( "name" );
        this.type = actionData.getString( "type" );
        this.input = createInput( actionData, prevAction, actionList, httpRequest );
        this.request = createRequest( actionData, serverName, username, password, actionJsEngine );
        this.output = createOutput( actionData, prevAction, actionJsEngine );
        this.goTo = createGoTo( actionData, actionJsEngine );
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public ActionInput getInput()
    {
        return input;
    }

    public ActionOutput getOutput()
    {
        return output;
    }

    public ActionRequest getRequest()
    {
        return request;
    }
    
    public ActionGoTo getGoTo()
    {
        return goTo;
    }

    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public void run( JSONObject actionList ) throws ActionException
    {
        if( this.request != null )
        {
            if( this.type.equals( Action.ACTION_TYPE_DHIS ) )
            {
                try
                {
                    Util.sendRequest( this, actionList );
                }
                catch( Exception ex )
                {
                    throw new ActionException( "", ex );
                }
            }
            
        }
        else if( this.type.equals( Action.ACTION_TYPE_JAVASCRIPT ) )
        {
            // this.output.evalOutput();
        }
        
        this.output.evalOutput( this.name );
    }
    
    // -------------------------------------------------------------------------
    // Supportive method
    // -------------------------------------------------------------------------

    /**
     * An valid name is a string which :
     * - Not start as a number
     * - No white space in the name
     * - No special characters
     * **/
    private boolean checkNameValid( String name )
    {
        return name.matches( Action.REQEXP_NAME );
    }
    
    private ActionInput createInput( JSONObject actionData, Action prevAction, JSONObject actionList, HttpServletRequest httpRequest ) throws ActionInputException
    {
        // ---------------------------------------------------------------------
        // ActionInput
        
        // Generate "input" property from previous action if there is not defination
        // "input" will get from outPut of previous action. Don't need to do this one for the first action in configuration
        if( actionData.getInt( "index" ) > 0 && !actionData.has( "input" ) && prevAction != null )
        {
            JSONObject inputDefined = new JSONObject();
            inputDefined.put( "actionName", "[" + prevAction.getName() + "]" );
            actionData.put( "input", inputDefined );
        }
        
        // Create ActionInput object
        ActionInput input = null;
        
        if( actionData.has( "input" ))
        {
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
            
            input = new ActionInput( inputStr, httpRequest, actionList );
        }
        
        return input;
    }
    
    private ActionRequest createRequest( JSONObject actionData, String serverName, String username, String password, ActionJSEngine actionJsEngine ) throws ActionException
    {
        ActionRequest actionRequest = null;
        String type = actionData.getString( "type" );
        if ( type.equals( Action.ACTION_TYPE_DHIS ) )
        {
            String link = actionData.getString( "URL" );
            String requestType = actionData.getString( "RESTType" );
            actionRequest = new ActionDhisRequest( serverName, link, requestType, username, password, actionJsEngine );
        }

        return actionRequest;
    }
    
    private ActionOutput createOutput( JSONObject actionData, Action prevAction, ActionJSEngine actionJsEngine )
    {
        ActionOutput output = new ActionOutput( actionJsEngine );

        String type = actionData.getString( "type" );
        if ( type.equals( Action.ACTION_TYPE_JAVASCRIPT ) )
        {
            if( prevAction != null )
            {
                output.setOutputMsg( prevAction.getOutput().getOutputMsg() );
            }
            else
            {
                output.setOutputMsg( this.input.getInputStr() );
            }
        }
        
        // Get "actionEval" property and set for output object if any
        if( actionData.has( "actionEval" ) )
        {
            output.setActionEval( actionData.getString( "actionEval" ) );
        }
        
        return output;
    }
    
    private ActionGoTo createGoTo( JSONObject actionData, ActionJSEngine actionJsEngine )
    {
        ActionGoTo goTo = new ActionGoTo( actionData.getInt( "index" ), actionJsEngine ); // This property value is added from "ActionSet" class

        if( actionData.has( "goTo" ) )
        {
            goTo.setGoToDefination( actionData.getString( "goTo" ) );
        }
        
        return goTo;
    }
}
