var chart
function simulate() {
	var date = $('#startDate').data("DateTimePicker").date();
	console.log(date);
	var days = parseInt($('#numDays').val());
	var randomDay = 0;
	var randomService = 0;
	for (var i = 0; i < parseInt($('#numRequests').val()); ++i) {
		randomDay = Math.floor(Math.random() * (days + 1));
		var myDate = date.clone().add(randomDay, "Days");
		randomService = Math.floor(Math.random() * (15)) + 1;
		$.ajax({
			async : false,
			method : "POST",
			url : '/api/appointment',
			data : JSON.stringify({
				"date" : myDate.toISOString(),
				"service" : randomService
			}),
			success : function(data) {
				console.log("Data:" + data);
				$('#results').append(
						'<tr><td>' + data.service + '</td><td>'
								+ data.requestedDate + '</td><td>'
								+ data.proposedDate + '</td><td>'
								+ data.proposedTime + '</td></tr>')
			},
			contentType : 'application/json'
		});
	}
};

function setup() {
	$('#startDate').datetimepicker();
	$('#simulate').click(function(event) {
		event.preventDefault();
		simulate();
	});
	$('#tabs-2').hide();
	$('#tab1').click(function(event){
		$('#tab1').addClass("active");
		$('#tabs-1').show();
		$('#tabs-2').hide();
		$('#tab2').removeClass("active");
	});
	$('#tab2').click(function(event){
		$('#tabs-2').show();
		$('#tabs-1').hide();
		$('#tab2').addClass("active");
		$('#tab1').removeClass("active");
	});
	setInterval(updateProviderSchedule, 5000)
};

function setupChart(data) {
	var options = {
		title : {
			text : "Provider Schedule Occupancy"
		},
		animationEnabled : true,
		data : [ {
			type : "line", // change it to line, area, bar, pie, etc
			dataPoints : data
		} ],
		axisX : {
			title : "Date",
			titleFontSize: "14",
			labelAngle: -60
		},

		axisY : {
			title : "Hours Booked (Optimal 12 Hours / day)",
			titleFontSize: "12"
		},
	};
	chart = $("#chartContainer").CanvasJSChart(options);
}

function updateProviderSchedule() {
	var date = $('#startDate').data("DateTimePicker").date();
	if (date == null) {
		date = moment();
	}
	$.ajax({
		async : false,
		method : "GET",
		url : '/booking/count',
		data : {
			"startDate" : date.toISOString(),
			"numDays" : 20
		},
		success : function(data) {
			console.log("Data:" + data[0].label + ", " + data[0].y);
			setupChart(data);
		},
		contentType : 'application/json'
	});
}