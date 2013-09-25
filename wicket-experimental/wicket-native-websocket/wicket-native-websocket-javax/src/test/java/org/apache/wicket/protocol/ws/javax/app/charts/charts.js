/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
google.load("visualization", "1", {
	packages:["corechart"]
});

google.setOnLoadCallback(drawChart);

function drawChart() {

	var options = {
		title: 'Stock prices'
	};

	var rawData = [
		['Year', 'Company 1', 'Company 2'],
		["1999", 0, 0]
	];

	var chart;

	/**
	 * (Re)Initializes the chart
	 */
	var updateChart = function() {
		var data = google.visualization.arrayToDataTable(rawData);
		if (chart) {
			chart.clearChart();
		} else {
			chart = new google.visualization.LineChart(Wicket.$('chart_div'));
		}
		chart.draw(data, options);
	};

	/**
	 * Updates the chart data by updating or appending the new data pushed by the server
	 *
	 * @param data the new data pushed by the server
	 */
	var updateChartData = function(data) {

		// the index of the column which data will be updated. Either 1 (Company 1) or 2 (Company 2)
		var columnIndex = rawData[0].indexOf(data.field);

		var replaced = false;

		// look whether there is already a record for that year and update the data for the company
		for (var r = 1; r < rawData.length; r++) {
			var oldRecord = rawData[r];
			if (oldRecord[0] === data.year) {
				oldRecord[columnIndex] = data.value;
				replaced = true;
				break;
			}
		}

		if (!replaced) {
			// there is no update, so append the new data

			// the data of the last appended record
			var lastRecord = rawData[rawData.length - 1];

			// the new record that keeps the same data as the last one but updates the value for the updated company
			var newRecord = [data.year, lastRecord[1], lastRecord[2]];
			newRecord[columnIndex] = data.value;
			rawData.push(newRecord);
		}

		if (rawData.length > 20) {
			// show at most 20 years back. The first record is special (the headers), so cut the second one
			rawData.splice(1, 1);
		}

		updateChart();
	};

	Wicket.Event.subscribe("/websocket/open", function(jqEvent) {
		// show the initial state of the chart
		updateChart();
	});

	Wicket.Event.subscribe("/websocket/message", function(jqEvent, message) {
		// new record is pushed by the server

		var record = jQuery.parseJSON(message);
		if (record && record.year) {
			updateChartData(record);
		}
	});

}

