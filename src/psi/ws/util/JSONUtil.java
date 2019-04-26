package psi.ws.util;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;

public class JSONUtil
{
    public static JSONObject convertJSONData( String jsonStr )
    {
        return new JSONObject( jsonStr.trim() );
    }
    
    // Convert InputStream to String
    public static JSONObject getJsonFromInputStream( InputStream is ) throws Exception
    {
        JSONObject jsonData = new JSONObject(); // Default blank JSON

        String contentStr = Util.readInputStream( is );

        if ( !contentStr.isEmpty() )
        {
            jsonData = new JSONObject( contentStr );
        }

        return jsonData;
    }
    
    public static String getJSONStrVal( JSONObject jsonDataInput, String key )
    {
        String output = "";

        if ( jsonDataInput != null && jsonDataInput.has( key ) )
        {
            output = jsonDataInput.get( key ).toString();
        }

        return output;
    }
    
    public static JSONArray getJsonArray( JSONObject jsonObj, String propName )
    {
        JSONArray propJsonArr = new JSONArray();

        if ( jsonObj != null && jsonObj.has( propName ) )
        {
            propJsonArr = jsonObj.getJSONArray( propName );
        }

        return propJsonArr;
    }

    public static JSONObject getJsonObject( JSONArray jsonObjArr, String propName, String propVal )
    {
        JSONObject propJson = null;

        if ( jsonObjArr != null && jsonObjArr.length() > 0 )
        {
            for ( int i = 0; i < jsonObjArr.length(); i++ )
            {
                JSONObject jsonObj = jsonObjArr.getJSONObject( i );

                String propValStr = JSONUtil.getJSONStrVal( jsonObj, propName );

                if ( propValStr.equals( propVal ) )
                {
                    propJson = jsonObj;
                    break;
                }
            }
        }

        return propJson;
    }
    
    public static String getValueFromJsonPath( JSONObject json, String jsonPath )
    {
       return JsonPath.read( json.toString() , "$." + jsonPath ).toString();
    }
}
