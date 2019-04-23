
function Util() {}

/** 
 * Result : 2017-01-30T10:30:15
 * **/
Util.getCurrentDateTime = function()
{
	var date = Util.getLastNDate( 0 );
	
	var day = date.getDate();
	day = ( day < 10 ) ? "0" + day : day;
	
	var month = date.getMonth() + 1;
	month = ( month < 10 ) ? "0" + month : month;
	
	var year = date.getFullYear();
	
	var hours = date.getHours();
	var minutes = date.getMinutes();
	var seconds = date.getSeconds();
	
	return year + "-" + month + "-" + day + "T" + hours + ":" + minutes + ":" + seconds;
};

/** 
 * Get a past date object from a special date 
 * Param  : dateStr ( '2017-01-02' )
 * Result : Object Date( converted from server time to local time )
 * **/
Util.getLastXDateFromDateStr = function( dateStr, noDays )
{
	var date = Util.convertDateStrToObj( dateStr );
    date.setDate( date.getDate() - noDays );
    
    return date;
};

/** 
 * Get a past date object from current date
 * Params : A number
 * Result : Date object( converted from local time to UTM time )
 * **/
Util.getLastNDate = function( noDays )
{
	var date = new Date();
    date.setDate( date.getDate() - noDays );
    
    return date;
};


Util.convertDateStrToObj = function( dateStr ) {
	var year = dateStr.substring( 0, 4 );
	var month = eval( dateStr.substring( 5,7 ) ) - 1;
	var day = eval( dateStr.substring( 8, 10 ) );
	
	var hour = "00";
	var minute = "00";
	var second = "00";
	if( serverdate.length > 10 )
	{
		hour = serverdate.substring( 11, 13);
		minute = serverdate.substring( 14, 16 );
		second = serverdate.substring( 17, 19 );
	}

	var date = new Date( year, month, day, hour, minute, second );
	
    return new Date(date);
};

/** 
 * dateObj 
 * Result : "2017-02-07T09:56:10.298"
 * **/
Util.formatDateObj_DbDateTime = function( dateObj )
{	
	var year = dateObj.getFullYear();
	var month = dateObj.getMonth() + 1;
	month = ( month < 10 ) ? "0" + month : month;
	
	var dayInMonth = dateObj.getDate();
	dayInMonth = ( dayInMonth < 10 ) ? "0" + dayInMonth : dayInMonth;
	
	var hours = dateObj.getHours();
	hours = ( hours < 10 ) ? "0" + hours : "" + hours;
	
	var minutes = dateObj.getMinutes();
	minutes = ( minutes < 10 ) ? "0" + minutes : "" + minutes;
	
	var seconds = dateObj.getSeconds();
	seconds = ( seconds < 10 ) ? "0" + seconds : "" + seconds;
	
	return year + "-" + month + "-" + dayInMonth + "T" + hours + ":" + minutes + ":" + seconds;
};