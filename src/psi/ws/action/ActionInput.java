package psi.ws.action;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.exception.ActionException;
import psi.ws.util.JSONUtil;
import psi.ws.util.Util;

public class ActionInput
{
    private static String PARAMS_PAYLOAD = "PAYLOAD";
    
    private static String REGEXP_REQUEST = "\\{\\s*\\[(" + Action.PARAMS_REQUEST + ")\\]\\.([^\\}]+)+\\s*\\}";
    private static String REGEXP_ACTIONNAME = "\\{\\s*\\\"(" + Action.PARAMS_ACTIONNAME + ")\\\"\\s*:\\s*\\\"\\[(\\w+)\\]\\\"\\s*\\}";
    private static String REGEXP_ACTIONNAME_PARAMETER = "\\{\\s*\\[(\\w+)\\]\\.([^\\}]+)+\\s*\\}";
    
    private String inputStr;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    
    public ActionInput( String inputStr, JSONObject requestData, JSONObject actionList ) throws ActionException
    {
        super();

        // Replace by request data
        String resolvedInput = resolvedWithRequestData( inputStr, requestData );
        // Replace by data from action ( ActionOut data )
        resolvedInput = resolvedWithActionData( resolvedInput, actionList );
      
        this.inputStr = resolvedInput;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    
    public JSONObject getInputJson()
    {
        return JSONUtil.convertJSONData( this.inputStr );
    }
    
    public String getInputStr()
    {
        return this.inputStr;
    }
    

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------
    
    /**
        @throws ActionException 
     * @inputStr "PAYLOAD"
        OR
        {
            "firstName": "{[request].firstName}",
            "lastName": "{[request].lastName}"
        }
    **/
    private String resolvedWithRequestData( String inputStr, JSONObject requestData )
    {
        String resolvedInput = inputStr;
        
        resolvedInput = resolvedInput.replaceAll( ActionInput.PARAMS_PAYLOAD, requestData.toString() );
        JSONArray parameters = Util.parseData( resolvedInput, ActionInput.REGEXP_REQUEST );
        for( int i = 0; i< parameters.length(); i++ )
        {   
            JSONObject param = parameters.getJSONObject( i );
            String realStr = param.getString( "realStr" );
            String paramKey = param.getString( "param" );
            String jsonPath = param.getString( "key" );
            if( paramKey.equals( Action.PARAMS_REQUEST ) )
            {
                String value = JSONUtil.getValueFromJsonPath( requestData, jsonPath );
                resolvedInput = resolvedInput.replace( realStr, value );
            }
        }
        
        return resolvedInput;
    }
    
    /** 
     * @inputStr {"actionName" : "[action_1]"} OR { firstName: {[action_1].response.importSummaries[0].reference}, ... }
     * @return
          --> Result  [
            {
                 "realStr": {[actionName] : "action_1"}
                 "param": actionName,
                 "key": action_1
            }
        ]
     * @throws ActionException 
    **/
    private String resolvedWithActionData( String inputStr, JSONObject actionList ) throws ActionException
    {
        String resolvedInput = inputStr;
        
        if ( actionList != null && actionList.length() > 0 )
        {
            // {"actionName" : "[action_1]"}
            resolvedInput = resolvedWithActionName( resolvedInput, actionList );
            
            // {[action_1].response.importSummaries[0].reference}
            resolvedInput = resolvedWithActionParametter( resolvedInput, actionList);
        }
        
        return resolvedInput;
    }
    
    // {"actionName" : "[action_1]"}
    private String resolvedWithActionName( String inputStr, JSONObject actionList ) throws ActionException
    {
        String resolvedInput = inputStr;
    
        JSONArray parameters = Util.parseData( inputStr, ActionInput.REGEXP_ACTIONNAME );
        for ( int i = 0; i < parameters.length(); i++ )
        {
            JSONObject param = parameters.getJSONObject( i );
            
            String realStr = param.getString( "realStr" );
            String paramKey = param.getString( "param" );
            String actionName = param.getString( "key" );
            if ( paramKey.equals( Action.PARAMS_ACTIONNAME ) )
            {
                Action action = (Action) actionList.get( actionName );
                String value = action.getOutput().getOutputMsg();
                resolvedInput = resolvedInput.replace( realStr, value );
            }
        }
        
        return resolvedInput;
    }
    
    // {[action_1].response.importSummaries[0].reference}
    private String resolvedWithActionParametter( String inputStr, JSONObject actionList ) throws ActionException
    {
        String resolvedInput = inputStr;
        
        JSONArray parameters = Util.parseData( inputStr, ActionInput.REGEXP_ACTIONNAME_PARAMETER );
        for ( int i = 0; i < parameters.length(); i++ )
        {
            JSONObject param = parameters.getJSONObject( i );
            
            String realStr = param.getString( "realStr" );
            String actionName = param.getString( "param" );
            String jsonPath = param.getString( "key" );

            Action action = (Action) actionList.get( actionName );
            if( action != null )
            {
                String value = JSONUtil.getValueFromJsonPath( action.getOutput().getOutputJson(), jsonPath );
                resolvedInput = resolvedInput.replace( realStr, value );
            }
            else
            {
                throw new ActionException("Cannot find the action with name " + actionName );
            }
        }
        
        return resolvedInput;
    }
    
}
