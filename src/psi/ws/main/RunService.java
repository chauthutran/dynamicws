package psi.ws.main;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import psi.ws.action.ActionSet;

public class RunService
    extends HttpServlet
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    //

    protected void doPost( HttpServletRequest request, HttpServletResponse response )
    {
        if ( request.getPathInfo() != null && request.getPathInfo().split( "/" ).length >= 2 )
        {
            String[] queryPathList = request.getPathInfo().split( "/" );
            String key = queryPathList[1];
            
            ActionSet actionSet = new ActionSet( request, response, key );
            actionSet.run();
        }
    }
}
