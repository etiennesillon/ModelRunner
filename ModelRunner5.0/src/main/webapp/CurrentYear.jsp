<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Display Today Date using JSP - A Dynamic Projcet Using JSP</title></head>
<body>Welcome to sitenol.com !<br><br>
<%

java.util.Calendar calendar = new java.util.GregorianCalendar();
out.println("<b>Current Year is (YYYY): </b>"+calendar.get(calendar.YEAR));

%>

</body>
</html>
