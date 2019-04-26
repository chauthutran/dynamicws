package psi.ws.exception;

public class ActionPropertyException extends ActionException
{
    private static final long serialVersionUID = 89277196386272588L;

    public ActionPropertyException( String propertyName )
    {
        super( "An action is missing the property '" + propertyName + "'" );
    }

}
