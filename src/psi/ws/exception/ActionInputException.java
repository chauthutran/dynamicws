package psi.ws.exception;

public class ActionInputException extends ActionException {

    private static final long serialVersionUID = -5618376895228463899L;
    
    public ActionInputException( Throwable err )
    {
        super( "Failed to get data from request", err );
    }

 }
