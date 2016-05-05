<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet"
href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
	
	<!-- CSS Styles -->
<style>
  .speech {border: 1px solid #DDD; width: 700px; padding: 0; align:center }
  .speech input {border: 0; width: 658px; display: inline-block; height: 30px;align:center}
  .speech img {float: right; width: 40px }
</style>
 
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

<!-- Search Form -->
<form id="labnol" method="get" action="search.html">
<div class="center" style="align: auto">
<div class="col-lg-6">
  <div class="speech">
    <input type="text" name="term" class="form-control" id="transcript" placeholder="Search"/>
    <img onclick="startDictation()" src="//i.imgur.com/cHidSVu.gif" />
  </div>
  </div>
  </div>
</form>
</body>
</html>