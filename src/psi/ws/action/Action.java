package psi.ws.action;

import java.util.List;

import org.json.JSONObject;

import psi.ws.action.request.ActionMongodbRequest;
import psi.ws.action.request.ActionWebServiceRequest;
import psi.ws.configuration.Configuration;
import psi.ws.exception.ActionException;
import psi.ws.exception.ActionPropertyException;

public class Action
{
    public static final String CONFIG_PARAM_SIGN_START = "[";
    public static final String CONFIG_PARAM_SIGN_END = "]";
    public static final String REQEXP_NAME = "^[a-zA-Z_][a-zA-Z0-9_]*";
    
    public static final String ACTION_TYPE_DHIS = "DHIS";
    public static final String ACTION_TYPE_MONGO = "MONGO";
    public static final String ACTION_TYPE_JAVASCRIPT = "JS";

    public static String PARAMS_REQUEST = "request";
    public static String PARAMS_ACTIONNAME = "actionName";
    
    private String name;
    private String type;
    private ActionInput input;
    private ActionRequest request;
    private ActionOutput output;
    private ActionGoTo goTo;
    private List<Action> ranAction;
    private JSONObject actionListByName;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    
    public Action( String name, JSONObject httpRequestData, ActionInput input, ActionRequest request, ActionGoTo goTo )
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
     * @throws ActionException s
    **/
    public Action( JSONObject actionData, JSONObject httpRequestData, List<Action> ranAction, JSONObject actionListByName, ActionJSEngine actionJsEngine, Configuration configuration ) throws ActionException
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
        
        Action prevAction = null;
        if( ranAction.size() > 0 )
        {
            prevAction = ranAction.get( ranAction.size() - 1 ); 
        }
        
        this.name = actionData.getString( "name" );
        this.type = actionData.getString( "type" );
        this.input = createInput( actionData, prevAction, httpRequestData, actionListByName );
        this.request = createRequest( actionData, configuration, httpRequestData, actionJsEngine );
        this.output = createOutput( actionData, prevAction, actionJsEngine );
        this.goTo = createGoTo( actionData, actionJsEngine );
        

        this.ranAction = ranAction;
        this.actionListByName = actionListByName;
        
        
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
            this.request.sendRequest( this );
            
        }
        else if( this.type.equals( Action.ACTION_TYPE_JAVASCRIPT ) )
        {
            // this.output.evalOutput();
        }
        
        // Put the ran action in list
        this.ranAction.add( this );
        this.actionListByName.put( this.name, this );
        
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
    
    private ActionInput createInput( JSONObject actionData, Action prevAction, JSONObject httpRequestData, JSONObject actionList ) throws ActionException
    {
        // ---------------------------------------------------------------------
        // ActionInput
        
        // Generate "input" property from previous action if there is not definition
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
            
            input = new ActionInput( inputStr, httpRequestData, actionList );
        }
        
        return input;
    }
    
    private ActionRequest createRequest( JSONObject actionData, Configuration configuration, JSONObject httpRequestData, ActionJSEngine actionJsEngine ) throws ActionException
    {
        ActionRequest actionRequest = null;
        String type = actionData.getString( "type" );
        if ( type.equals( Action.ACTION_TYPE_DHIS ) )
        {
            String link = actionData.getString( "URL" );
            String requestType = actionData.getString( "RESTType" );
            actionRequest = new ActionWebServiceRequest( configuration, httpRequestData, link, requestType, actionJsEngine, this.actionListByName );
        }
        else if( type.equals( Action.ACTION_TYPE_MONGO ) )
        {
            String requestType = actionData.getString( "RESTType" );
            actionRequest = new ActionMongodbRequest( configuration, requestType );
        }

        return actionRequest;
    }
    
    private ActionOutput createOutput( JSONObject actionData, Action prevAction, ActionJSEngine actionJsEngine )
    {
        ActionOutput output = new ActionOutput( actionJsEngine );

        String type = actionData.getString( "type" );
        if ( type.equals( Action.ACTION_TYPE_JAVASCRIPT ) )
        {
            if( this.input.getInputStr() != null )
            {
                output.setOutputMsg( this.input.getInputStr() );
            }
            else if( prevAction != null )
            {
                output.setOutputMsg( prevAction.getOutput().getOutputMsg() );
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
