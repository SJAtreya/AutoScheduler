var chart
function simulate() {
	var date = $('#startDate').data("DateTimePicker").date();
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
				$('#results').append(
						'<tr><td>' + data.serviceName + '</td><td>'
								+ data.requestedDate + '</td><td>'
								+ data.proposedDate + '</td><td>'
								+ data.proposedTime
								+ '</td><td><a class="btn btn-primary btn-sm" href="javascript:openModal('+data.serviceId+')">Chat with our Assistant</a></td></tr>')
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
	$('#tab1').click(function(event) {
		$('#tab1').addClass("active");
		$('#tabs-1').show();
		$('#tabs-2').hide();
		$('#tab2').removeClass("active");
	});
	$('#tab2').click(function(event) {
		$('#tabs-2').show();
		$('#tabs-1').hide();
		$('#tab2').addClass("active");
		$('#tab1').removeClass("active");
	});
	$('#findAppointment').click(function(event) {
		event.preventDefault();
		findAppointment();
	});
//	$('#chatModal').modal();
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
			titleFontSize : "14",
			labelAngle : -60
		},

		axisY : {
			title : "Hours Booked (Optimal 12 Hours / day)",
			titleFontSize : "12"
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
			setupChart(data);
		},
		contentType : 'application/json'
	});
}

function openModal(serviceId) {
	$('#chatModal').modal('show');
	$('#serviceId').val(serviceId);
	$('#chatContent').val('');
	setTimeout(function(){$('#chatContent').append('<div class="row" style="padding-top:10px"><div class="col-md-6 col-md-offset-5" style="background-color:#a4f2c1;border-radius:5px"><strong>Vader: </strong>Hello, I\'m Vader. How may I assist you?</div></div>')},1000);
}

function findAppointment(){
	var message = $('#message').val();
	var serviceId = $('#serviceId').val();
	var options = ''
	$.getJSON('/api/appointment/finder/v1',{"message":message,"serviceId":serviceId},function(data){
		options = (data.options!=null || data.options!= undefined)?data.options:''
		$('#chatContent').append('<div class="row" style="padding-top:10px"><div class="col-md-4 col-md-offset-1" style="background-color:#ADD8E6;border-radius:5px"><strong>Me:</strong> '+message+'</div></div>').
		append('<div class="row" style="padding-top:10px"><div class="col-md-6 col-md-offset-5" style="background-color:#a4f2c1;border-radius:5px"><strong>Vader: </strong>'+data.message+'<br/><br/>'+options+'</div></div>');
		
	});
	$('#message').val('');
}