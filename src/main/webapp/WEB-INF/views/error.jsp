<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title></title>
</head>
<body>
<h1>Error Page</h1>
<p>Application has encountered an error.</p>

<pre>
Date: ${datetime}
Failed URL: ${url}
Exception:  ${exception.message}
<c:forEach items="${exception.stackTrace}" var="ste">    ${ste}
</c:forEach>
</pre>

</body>
</html>