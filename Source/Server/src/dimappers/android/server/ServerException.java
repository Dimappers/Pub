package dimappers.android.server;

public class ServerException extends Exception 
{
	private ExceptionType exceptionType;
	private Exception originalException;
	
	public ServerException(ExceptionType t)
	{
		super();
		exceptionType = t;
		originalException = null;
	}
	
	public ServerException(ExceptionType t, Exception e)
	{
		super();
		originalException = e;
		exceptionType = t;
	}
	
	public ExceptionType GetExceptionType()
	{
		return exceptionType;
	}
	
	public Exception GetOriginalException()
	{
		return originalException;
	}
}

enum ExceptionType
{
	//MessageReceived errors
	MessageReceivedReadingObject,
	MessageReceivedCastingObject,
	MessageReceivedStreamCorrupted,
	MessageReceivedUnknownError,

	//NewEvent errors
	NewEventReadingObject,
	NewEventCastingObject,
	NewEventStreamCorrupted,
	NewEventSendingAcknoledgementBack,
	NewEventSendingErrorBack,
	NewEventUnknownError,
	
	//Refresh errors
	RefreshReadingObject,
	RefreshCastingObject,
	RefreshStreamCorrupted,
	RefreshUnknownError,
	
	//Respond errors
	RespondReadingObject,
	RespondStreamCorrupted,
	RespondCastingObject,
	RespondUnknownError,
	
	//Update errors
	UpdateReadingObject,
	UpdateStreamCorrupted,
	UpdateCastingObject,
	UpdateUnknownError,
	
	//EventManager errors
	EventManagerNoSpace,
	EventManagerNoSuchEvent,
	EventManagerErrorCreatingDatabase,
	EventManagerErrorOpeningDatabase,
	EventManagerErrorReadingDatabase,
	
	//UserManager errors
	UserManagerMaxUsers,
	UserManagerNoSuchUser,
	UserManagerErrorCreatingDatabase,
	UserManagerErrorOpeningDatabase,
	UserManagerErrorReadingDatabase,
	
	//ServerUser errors
	ServerUserNoSuchEvent
}