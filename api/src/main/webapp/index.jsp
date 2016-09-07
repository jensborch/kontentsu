<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<c:set var="angularVersion" value="1.5.7" scope="application" />
<c:set var="angularMaterialVersion" value="1.0.9" scope="application" />
<c:set var="swaggerUiVersion" value="2.1.4" scope="application" />
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/webjars/angular-material/${angularMaterialVersion}/angular-material.min.css">
        <link rel="stylesheet" href="cdn.css">
        <title>CDN publishing application</title>
    </head>
    <body ng-app="CdnApp" ng-cloak>

        <script src="${pageContext.request.contextPath}/webjars/angularjs/${angularVersion}/angular.min.js"></script>
        <script src="${pageContext.request.contextPath}/webjars/angularjs/${angularVersion}/angular-animate.min.js"></script>
        <script src="${pageContext.request.contextPath}/webjars/angularjs/${angularVersion}/angular-aria.min.js"></script>
        <script src="${pageContext.request.contextPath}/webjars/angularjs/${angularVersion}/angular-messages.min.js"></script>
        <script src="${pageContext.request.contextPath}/webjars/angular-material/${angularMaterialVersion}/angular-material.min.js"></script>

        <script type="text/javascript">
            angular.module('CdnApp', ['ngMaterial']);
        </script>

        <md-toolbar class="md-hue-2">
            <div class="md-toolbar-tools">
                <h1>CDN Publishing REST Service</h1>
            </div>
        </md-toolbar>
        <md-content felx>
            <md-list>
                <md-list-item class="md-2-line">
                    <md-button class="md-fab" aria-label="Swagger" href="${pageContext.request.contextPath}/webjars/swagger-ui/${swaggerUiVersion}/?url=${pageContext.request.contextPath}/api/swagger.json">
                        <md-icon md-font-set="material-icons">info</md-icon>
                    </md-button>
                    <div class="md-list-item-text">
                        <h3>Swagger</h3>
                        <p>
                            Documentation for the CDN REST API
                        </p>
                    </div>
                </md-list-item>
            </md-list>
        </md-content>
    </body>
</html>
