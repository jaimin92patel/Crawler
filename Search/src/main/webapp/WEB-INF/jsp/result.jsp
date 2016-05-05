<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
<head>
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
<script
src="http://maps.googleapis.com/maps/api/js">
</script>
<script>
    
    var geocoder = new google.maps.Geocoder();
    var map;
function initialize() {
  var mapProp = {
    center:new google.maps.LatLng(51.508742,-0.120850),
    zoom:5,
    mapTypeId:google.maps.MapTypeId.ROADMAP
  };
   map=new google.maps.Map(document.getElementById("googleMap"), mapProp);
}
function codeAddress() {
    var address = document.getElementById("address").value;
    geocoder.geocode( { 'address': address}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        map.setCenter(results[0].geometry.location);
        
        document.getElementById("location").innerText = results[0].geometry.location;
        var marker = new google.maps.Marker({
            map: map,
            position: results[0].geometry.location
        });
      } else {
        alert("Geocode was not successful for the following reason: " + status);
      }
    });
  }
google.maps.event.addDomListener(window, 'load', initialize);
</script>

<style>
  .speech {border: 1px solid #DDD; width: 700px; padding: 0; align:center }
  .speech input {border: 0; width: 658px; display: inline-block; height: 30px;align:center}
  .speech img {float: right; width: 40px }
</style>
<style>
.center {
	margin: auto;
	width: 60%;
	padding: 10px;
}
</style>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">

<!-- HTML5 Speech Recognition API -->
<script>
  function startDictation() {
 
    if (window.hasOwnProperty('webkitSpeechRecognition')) {
 
      var recognition = new webkitSpeechRecognition();
 
      recognition.continuous = false;
      recognition.interimResults = false;
 
      recognition.lang = "en-US";
      recognition.start();
 
      recognition.onresult = function(e) {
        document.getElementById('transcript').value
                                 = e.results[0][0].transcript;
        recognition.stop();
        document.getElementById('labnol').submit();
      };
 
      recognition.onerror = function(e) {
        recognition.stop();
      }
 
    }
  }
</script>

</head>
<body>

	<div class="page-header" align="center">
		<h1>
			Search Engine Application
		</h1>
	</div>

	<div>
		<form id="labnol" method="get" action="search.html">
<div class="center" style="align: auto">
<div class="col-lg-6">
  <div class="speech">
    <input type="text" name="term" class="form-control" id="transcript" placeholder="Search"/>
    <img onclick="startDictation()" src="//i.imgur.com/cHidSVu.gif" />
  </div>
  </div>
  </div>
</form>	</div>
	<div></div>
	<div></div>
	<div></div>

	<c:if test="${result == null}">
		<table align="center" border="0" style="width: 800px">
			<tr>
				<td>
					<h3 align="center">
						<font style="" color="#0066FF">*No Result Found...</font>
					</h3>
				</td>
			</tr>
		</table>
	</c:if>
	<br />
	<br />
	<br />
	<br />
	<br />
	<br />
	<c:if test="${result != null}">
		<center>
			<div class="input-group" style="width: 800px;">
				<table align="center" border="0" style="width: 800px">
						<tr>
							<td align="left"><b>Document</b></td>
							<td align="center"><b>TFIDF</b></td>
							<td align="center"><b>PageRank</b></td>
							<td align="center"><b>Score</b></td>
						</tr>
						<tr></tr>
						<tr></tr>
						<c:forEach items="${result}" var="res">
							<tr>
							<c:set var="summary" value="file:///${res.path}" />
								<td align="left"><a href="<c:url value="${summary}"/>">${res.title} ${res.path}</a></td>
								<td align="center">${res.tfidfNorm}</td>
								<td align="center">${res.pageRankNorm}</td>
								<td align="center">${res.totalScore}</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</center>
	</c:if>
</body>
</html>
