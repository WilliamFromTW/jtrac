<%@ tag body-content="empty" %> 
<script type="text/javascript">
	function fireOnReady() {
		var r = document.querySelector(':root');
		r.style.setProperty('--header', '${colorHeader}');
		r.style.setProperty('--lightblue', '${colorLightBlue}');
		r.style.setProperty('--mediumblue', '${colorMediumBlue}');
		r.style.setProperty('--darkblue', '${colorDarkBlue}');
		r.style.setProperty('--error', '${colorError}');
		r.style.setProperty('--errorbg', '${colorErrorBg}');
		r.style.setProperty('--gray', '${colorGray}');
	}

	if (document.readyState === 'complete') {
		fireOnReady();
	} else {
		document.addEventListener("DOMContentLoaded", fireOnReady);
	}
</script>
