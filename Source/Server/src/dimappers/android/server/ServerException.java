package dimappers.android.server;

public class ServerException extends Exception 
{
	private ExceptionType exceptionType;
	
	public ServerException(ExceptionType t)
	{
		super();
		
		exceptionType = t;
	}
	
	public ExceptionType GetExceptionType()
	{
		return exceptionType;
	}
}

enum ExceptionType
{
	ReadingObjectNewEvent,
	CastingObjectNewEvent,
	StreamCorruptedNewEvent,
	SendingErrorBack,
	UnknownError
}