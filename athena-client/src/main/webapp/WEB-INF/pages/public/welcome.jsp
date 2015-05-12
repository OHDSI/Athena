<%--
  Created by IntelliJ IDEA.
  User: GMalikov
  Date: 23.03.2015
  Time: 15:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/app/css/cerulean.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/app/css/main.min.css">

    <script src="${pageContext.request.contextPath}/resources/app/lib/jquery.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/underscore.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/backbone.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/backbone.marionette.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/bootstrap.js"></script>

    <script src="${pageContext.request.contextPath}/resources/app/js/athena.js"></script>

    <script src="${pageContext.request.contextPath}/resources/app/js/entities/vocabStatus.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/entities/logMessages.js"></script>

    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/vocabularyBuilderApp.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/vocabularyBuilderLayoutView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/list/statusListView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/list/statusListController.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/show/vocabularyShowLogController.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/show/vocabularyShowLogView.js"></script>

    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/info/vocabularyInfoController.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/info/vocabularyInfoView.js"></script>

    <title>Athena welcome page</title>
</head>
<body>
<div id="menu-region">
    <nav class="navbar navbar-inverse">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-2">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
            </div>

            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-2">
                <ul class="nav navbar-nav">
                    <li class="active"><a href="#">Build vocabularies <span class="sr-only">(current)</span></a></li>
                    <li><a href="#">Browse vocabularies</a></li>
                </ul>

                <ul class="nav navbar-nav navbar-right">
                    <p class="navbar-text">ATHENA Standardized Vocabularies for OMOP CDM</p>
                </ul>
            </div>
        </div>
    </nav>
</div>
<div id="mainRegion">

</div>
<div id="logRegion">

</div>
<div id="appContainer"></div>

</body>
<script type="text/javascript">
    $(document).ready( function(){
        $.get('../athena-client/resources/app/templates/templates.html', function(templateHtml){
            var template = $(templateHtml);
            $('body').append(template);
            AthenaApp.start();
        });
    });
</script>
</html>
