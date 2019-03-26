package psi.ws.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONUtil
{
    public static JSONObject convertJSONData( String jsonStr )
    {
        JSONObject result = new JSONObject();
        
        try
        {
            JSONObject jsonData = new JSONObject( jsonStr.trim() );
            
            result.put( "status", "SUCCESS" );
            result.put(  "data", jsonData );
            
        }
        catch( Exception ex )
        {
            result.put( "status", "ERROR" );
            result.put(  "errorMsg", ex.getMessage() );
            result.put(  "data", JSONObject.NULL );
        }
        
        return result;
    }
    
    // Convert InputStream to String
    public static JSONObject getJsonFromInputStream( InputStream is ) throws Exception
    {
            JSONObject jsonData = new JSONObject(); // Default blank Json
             
            try 
            { 
                    String contentStr = readInputStream( is );
                     
                    if ( !contentStr.isEmpty() )
                    {
                            jsonData = new JSONObject( contentStr );                                        
                    }
            } 
            catch (IOException e) 
            {
                    Util.outputErr( "Failed on getJsonFromInputStream" );
                    e.printStackTrace();
                    throw e;
            } 
                            
    return jsonData;
    }
    
    // 
    
    public static String getJSONStrVal( JSONObject jsonDataInput, String key )
    {
        String output = "";

        if ( jsonDataInput != null && jsonDataInput.has( key ) )
        {
            // output = jsonDataInput.getString( key );
            output = jsonDataInput.get( key ).toString();
        }

        return output;
    }
    
    
    public static JSONObject getJsonObject_Create( JSONObject jsonObj, String propName )
    {
            JSONObject propJson = getJsonObject( jsonObj, propName );

            if ( propJson == null )
            {
                    propJson = new JSONObject();
                    jsonObj.put( propName, propJson );
            }
            
            return propJson;
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

    public static int getJsonValInt( JSONObject jsonObj, String key, int defaultVal )
    {
        int output = defaultVal;

        if ( jsonObj != null && jsonObj.has( key ) )
        {
            try
            {
                output = (int) jsonObj.get( key );
            }
            catch ( Exception ex )
            {
                Util.outputErr( "Error in Util.getJsonValInt: " + ex.getMessage() );
            }
        }

        return output;
    }

    public static String getJsonFormattedStr( String input )
    {
        String output = "{}";

        try
        {
            // If 'input' is emtpy string, use above '{}'. If not, check by
            // creating new JSONObject.
            if ( !input.isEmpty() )
            {
                JSONObject inputJson = new JSONObject( input );

                output = inputJson.toString();
            }
        }
        catch ( Exception ex )
        {
        }

        return output;
    }
    
    public static String jsonStrFormat( String input )
    {
            return input.replace( "\"", "'" );
    }

    public static JSONObject getJsonObject( JSONObject jsonObj, String propName )
    {
        JSONObject propJson = null;

        if ( jsonObj != null && jsonObj.has( propName ) )
        {
            propJson = jsonObj.getJSONObject( propName );
        }

        return propJson;
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

    public static JSONObject getJsonObject( JSONArray jsonObjArr, int index )
    {
        JSONObject foundJson = null;

        if ( jsonObjArr != null && jsonObjArr.length() > index )
        {
            foundJson = jsonObjArr.getJSONObject( index );
        }

        return foundJson;
    }

    public static JSONObject getJsonObject( JSONObject jsonObj, int propIndex )
        throws Exception
    {
        JSONObject propJson = null;

        if ( jsonObj != null )
        {
            Iterator<?> keys = jsonObj.keys();

            int count = 0;

            while ( keys.hasNext() )
            {
                String key = (String) keys.next();

                if ( propIndex == count )
                {
                    propJson = JSONUtil.getJsonObject( jsonObj, key );
                }

                count++;
            }
        }

        return propJson;
    }
    
    public static JSONArray getJSONArraySubArr( JSONArray jsonList, Integer startIndex, Integer endIndex )
    {
        JSONArray newList = new JSONArray();

        if ( jsonList != null )
        {
            if ( endIndex == null )
                endIndex = jsonList.length() - 1;

            for ( int i = 0; i < jsonList.length(); i++ )
            {
                if ( i >= startIndex && i <= endIndex )
                {
                    newList.put( jsonList.get( i ) );
                }
            }
        }

        return newList;
    }
    

    // if already on target (the string value), it does not move from source..
    public static String getJsonObjValuesStr( JSONObject inputJson )
    {
        String outputStr = "";
            
        for ( String key: JSONObject.getNames( inputJson ) ) 
        {
            Object value = inputJson.get(key);
    
            outputStr += ( ( outputStr.isEmpty() ) ? "" : " - " ) + value.toString();
        }
        
        return outputStr;
    }
    
    
    // -------------------------------------------------------------------------
    // Merge Related

    // if already on target (the string value), it does not move from source..
    public static void jsonMerge( JSONObject source, JSONObject target )
        throws Exception
    {
        jsonMerge( source, target, false );
    }

    public static void jsonMerge( JSONObject source, JSONObject target, boolean bOverwrite )
        throws Exception
    {
        for ( String key : JSONObject.getNames( source ) )
        {
            Object value = source.get( key );

            if ( !target.has( key ) )
            {
                target.put( key, value );
            }
            else
            {
                // If already exists on target,
                // existing value for "key" - recursively deep merge:
                if ( value instanceof JSONObject )
                {
                    JSONObject valueJson = (JSONObject) value;

                    Object targetVal = target.get( key );

                    if ( targetVal instanceof JSONObject )
                    {
                        jsonMerge( valueJson, (JSONObject) targetVal, bOverwrite );
                    }
                }
                else if ( value instanceof String )
                {
                    if ( bOverwrite )
                        target.put( key, value );
                }
            }
        }
        // return target;
    }

    // TODO: NEED TESTING!!! CONFIRMATION OF WORKING PROPERLY
    public static void jsonArrValueSetMerge( JSONArray destinationJsonArr, JSONArray additionalJsonArr,
        String comparePropName )
    {
        for ( int i = 0; i < additionalJsonArr.length(); i++ )
        {
            JSONObject additionAttrJson = additionalJsonArr.getJSONObject( i );

            String additionAttrId = JSONUtil.getJSONStrVal( additionAttrJson, comparePropName );

            JSONObject existingAttrJson = JSONUtil.getJsonObject( destinationJsonArr, comparePropName, additionAttrId );

            if ( existingAttrJson == null )
            {
                JSONObject clonedAttrJson = new JSONObject( additionAttrJson.toString() );

                destinationJsonArr.put( clonedAttrJson );
            }
            else
            {
                // write the additionalJson value
                existingAttrJson.put( "value", JSONUtil.getJSONStrVal( additionAttrJson, "value" ) );
            }
        }
    }

    public static String jsonToStr( JSONObject dataJson )
    {
        String output = "";

        try
        {
            if ( dataJson != null )
                output = dataJson.toString();
        }
        catch ( Exception ex )
        {
            Util.outputErr( "Error in Util.jsonToStr: " + ex.getMessage() );
        }

        return output;
    }
    
    private static String readInputStream( InputStream stream ) throws Exception 
    {
            StringBuilder builder = new StringBuilder();
        
            try ( BufferedReader in = new BufferedReader( new InputStreamReader( stream, Util.ENCODING_UTF8 ) ) ) {

                    String line;
            
                    while ( ( line = in.readLine() ) != null ) 
                    {
                builder.append(line); // + "\r\n"(no need, json has no line breaks!)
            }
            
            in.close();
        }
                
            return builder.toString();
    }
}
