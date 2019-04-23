package psi.ws.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.util.JSONUtil;
import psi.ws.util.Util;

public class ActionInput
{
    private static String PARAMS_PAYLOAD = "PAYLOAD";
    private static String PARAMS_REQUEST = "request";
    private static String PARAMS_ACTIONNAME = "actionName";
    
    private static String REGEXP_REQUEST = "\\{\\s*(\\%\\%" + ActionInput.PARAMS_REQUEST + "\\%\\%)\\.([^\\}]+)+\\s*\\}";
    private static String REGEXP_ACTIONNAME = "\\{\\s*\\\"(" + ActionInput.PARAMS_ACTIONNAME + ")\\\"\\s*:\\s*\\\"%%(\\w+)%%\\\"\\s*\\}";
    
    private static JSONObject requestData;
    
    private String inputStr;
    
    public ActionInput( String inputStr, HttpServletRequest request, JSONObject configActionListByActionName ) throws IOException, Exception
    {
        super();

        String resolvedInput = resolved( inputStr, request );
        resolvedInput = resolved( resolvedInput, configActionListByActionName );
      
        this.inputStr = resolvedInput;
    }
    
    public JSONObject getInputJson() throws IOException, Exception
    {
        return JSONUtil.convertJSONData( this.inputStr );
    }
    
    public String getInputStr() throws IOException, Exception
    {
        return this.inputStr;
    }
    
    // -------------------------------------------------------------------------
    
    /**
        @inputStr "PAYLOAD"
        OR
        {
            "firstName": {%%request%%.firstName},
            "lastName": {%%request%%.lastName}
        }
    **/
    private String resolved( String inputStr, HttpServletRequest request )
    {
        String resolvedInput = inputStr;
        
        
        try
        {
            JSONObject requestData = ActionInput.getResquestData( request );
            
            resolvedInput = resolvedInput.replaceAll( ActionInput.PARAMS_PAYLOAD, requestData.toString() );
            JSONArray parameters = Util.parseData( inputStr, ActionInput.REGEXP_REQUEST );
            
            for( int i = 0; i< parameters.length(); i++ )
            {   
                JSONObject param = parameters.getJSONObject( i );
                String realStr = param.getString( "realStr" );
                String paramKey = param.getString( "param" );
                String jsonPath = param.getString( "key" );
                if( paramKey.equals( ActionInput.PARAMS_REQUEST ) )
                {
                    String value = JSONUtil.getValueFromJsonPath( requestData, jsonPath );
                    resolvedInput = resolvedInput.replaceAll( realStr, value );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
       
        
        return resolvedInput;
    }
    
    /** 
     * @inputStr {"actionName" : "%%action_1%%"}
     * @return
          --> Result  [
            {
                 "realStr": {%%actionName%% : "action_1"}
                 "param": actionName,
                 "key": action_1
            }
        ]
    **/
    private String resolved( String inputStr, JSONObject actionList )
        throws IOException, Exception
    {
        String resolvedInput = inputStr;

        if ( actionList != null && actionList.length() > 0 )
        {
            JSONArray parameters = Util.parseData( inputStr, ActionInput.REGEXP_ACTIONNAME );
            for ( int i = 0; i < parameters.length(); i++ )
            {
                JSONObject param = parameters.getJSONObject( i );
                
                String realStr = param.getString( "realStr" );
                String paramKey = param.getString( "param" );
                String actionName = param.getString( "key" );
                if ( paramKey.equals( ActionInput.PARAMS_ACTIONNAME ) )
                {
                    Action action = (Action) actionList.get( actionName );
                    String value = action.getOutput().getOutputMsg();
                    resolvedInput = resolvedInput.replace( realStr, value );
                }
            }
        }
        
        return resolvedInput;
    }
    

    private static JSONObject getResquestData( HttpServletRequest request ) throws IOException, Exception
    {
        if( requestData == null )
        {
            requestData = new JSONObject();
            requestData = JSONUtil.getJsonFromInputStream( request.getInputStream() );
            return requestData;
        }
        
        return requestData;
    }
    
}
