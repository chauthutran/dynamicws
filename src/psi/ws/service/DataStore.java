package psi.ws.service;

import java.util.ArrayList;

import org.json.JSONObject;

import psi.ws.util.JSONUtil;

public class DataStore
{

    // ===========================
    // --- Variables ------------

    // Retrieved Response Data
    public int responseCode = 200; // Initially, set it as success.

    public String output = "";

    public JSONObject data = new JSONObject();

    public String referenceId = "";

    // send data
    public String sendStr = "";

    public String voucherCode = "";

    public String CUIC = "";

    public JSONObject outDataJson = new JSONObject();

    // Composed Return Message
    public String outMessage = "";

    public JSONObject outputJson = null;

    public String detailActionMsg = "";

    // NEW ONES
    public String oneCallMsg = "";

    public String errorMsg = ""; // Might be collection

    public String userMsg = ""; // User Friendly Message

    // OVERRIDE VARIABLES
    public boolean noDebug = false;

    // public int requestTimeout = Util.REQUEST_TIMEOUT;

    // ??
    public ArrayList<DataStore> callList = new ArrayList<DataStore>();

    // ===========================
    // --- Constructor ------------

    public DataStore()
    {
        super();
    }

    public DataStore( int responseCode )
    {
        super();
        this.responseCode = responseCode;
    }

    public DataStore( int responseCode, String outMessage )
    {
        super();
        this.responseCode = responseCode;
        this.outMessage = outMessage;
    }

    public DataStore( int responseCode, JSONObject outputJson )
    {
        super();
        this.responseCode = responseCode;
        this.outputJson = outputJson;
        // this.outMessage = outMessageJson.toString();
    }

    public static DataStore createWithOutputJsonMsg( int responseCode, String jsonMsg )
    {
        JSONObject outputJson = new JSONObject();
        outputJson.put( "msg", jsonMsg );

        return new DataStore( responseCode, outputJson );
    }

    // ===========================
    // --- Print Out ------------

    @Override
    public String toString()
    {
        return "DataStore: responseCode=" + responseCode + ", output=" + output + ", data=" + data + ", referenceId="
            + referenceId + ", sendStr=" + sendStr + ", voucherCode=" + voucherCode + ", CUIC=" + CUIC
            + ", outMessage=" + outMessage + ", detailActionMsg=" + detailActionMsg + ", outputJson="
            + JSONUtil.jsonToStr( outputJson );
    }

    // ===========================
    // --- Other Methoeds ------------

    public void clearContent()
    {
        this.responseCode = 200;
        this.output = "";
        this.data = new JSONObject();
        this.referenceId = "";

        // send data
        this.sendStr = "";
        this.voucherCode = "";
        this.CUIC = "";
        this.outDataJson = new JSONObject();

        // Composed Return Message
        this.outMessage = "";
        this.detailActionMsg = "";
        this.outputJson = null;

        // NEW ONES
        this.oneCallMsg = "";
        this.errorMsg = ""; // Might be collection
        this.userMsg = ""; // User Friendly Message

        this.noDebug = false;
    }

    public void setResponseOutputWithExThrow( int responseCodeInput, String outMessageInput )
        throws Exception
    {

        if ( responseCodeInput > responseCode )
            responseCode = responseCodeInput;

        if ( !outMessageInput.isEmpty() )
        {
            outMessage += (outMessage.length() > 0) ? ", " : "";
            outMessage += outMessageInput;
        }

        throw new Exception( outMessage );
    }

}
