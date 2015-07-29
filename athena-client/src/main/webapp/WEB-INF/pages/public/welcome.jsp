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
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/app/css/dataTables.bootstrap.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/app/css/jquery-ui.min.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/app/css/ui.jqgrid-bootstarp.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/app/css/ui.jqgrid.css">



    <%--Libraries--%>
    <script src="${pageContext.request.contextPath}/resources/app/lib/jquery.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/underscore.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/backbone.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/backbone.marionette.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/jquery.dataTables.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/dataTables.bootstrap.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/dataTables.scroller.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/backbone.picky.min.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/grid.locale-en.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/lib/jquery.jqGrid.min.js"></script>

    <%--Main App--%>
    <script src="${pageContext.request.contextPath}/resources/app/js/athena.js"></script>

    <%--Entities--%>
    <script src="${pageContext.request.contextPath}/resources/app/js/entities/vocabStatus.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/entities/logMessages.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/entities/vocabInfo.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/entities/menuItem.js"></script>

    <%--Common--%>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/common/views/loading.js"></script>

    <%--Main Menu App--%>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/menu/menuApp.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/menu/menuController.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/menu/menuItemView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/menu/menuMainView.js"></script>

    <%--Vocabulary Builder App--%>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/builderApp.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/builderLayoutView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/status/builderStatusListView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/status/builderStatusController.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/log/builderLogController.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/log/builderLogListView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/info/builderInfoController.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/vocabularyBuilder/info/builderInfoView.js"></script>

    <%--Vocabulary Browser App--%>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/browser/browserApp.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/browser/views/browserLayoutView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/browser/browserMainController.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/browser/views/browserVocabulariesView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/browser/views/browserConceptsView.js"></script>
    <script src="${pageContext.request.contextPath}/resources/app/js/apps/browser/views/browserRelationsView.js"></script>


    <title>Athena welcome page</title>
</head>
<body>
<div id="menu-region">

</div>
<div id="testRegion"></div>
<div id="mainRegion"></div>

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
