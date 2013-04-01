<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="en" ng-app="myApp">
<head>
  <meta charset="utf-8">
  <title>My AngularJS App</title>
  <link rel="stylesheet" href="<c:url value="/resources/css/app.css"/>"/>
</head>
<body>
  <ul class="menu">
    <li><a href="#/view1">view1</a></li>
    <li><a href="#/view2">view2</a></li>
  </ul>

  <div ng-view></div>

  <div>Angular seed app: v<span app-version></span></div>

  <!-- In production use:
  <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.4/angular.min.js"></script>
  -->
  <script src="<c:url value="/resources/lib/angular/angular.js"/>"></script>
  <script src="<c:url value="/resources/js/app.js"/>"></script>
  <script src="<c:url value="/resources/js/services.js"/>"></script>
  <script src="<c:url value="/resources/js/controllers.js"/>"></script>
  <script src="<c:url value="/resources/js/filters.js"/>"></script>
  <script src="<c:url value="/resources/js/directives.js"/>"></script>
</body>
</html>
