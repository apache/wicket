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

jQuery(document).ready(function() {

	module('Wicket.ChannelManager');

	test('queue', function () {
	
		var cm		= new Wicket.ChannelManager()
			ch		= 'name|s',
			i		= 0,
			result	= '';
	
		for (; i < 10; i++) {
			cm.schedule(ch, function () {
				result += i;
				cm.done(ch);
			});
		}
		
		equal(result, '0123456789');
	});

	test('drop', function () {
		
		expect(1);
		
		stop();
	
		var cm		= new Wicket.ChannelManager(),	// the manager
			name	= 'name',						// the channel's name
			chq		= name + '|s',					// the channel(s) to queue
			chd		= name + '|d',					// the channel to drop
			number	= 10,							// the number of channels to schedule
			i		= 0,							// the current iteration
			j 		= 0,							// the counter that decides when to release the test
			result	= '',							// the container for the actual result
			queueCallback = function(k) {
				result += k;
				cm.done(chq);
				
				if (++j === (number / 2)) {
					start();
			
					//equal(result, '0!drop!6789'); // desired check, but cannot find how to pass 
													// the current value to the channel's callback
					
					equal(result, '0!drop!10101010'); // one '10' for 6,7,8,9
			}
		}

		for (; i < number; i++) {

			cm.schedule(chq, function () {

				// how to pass the current value of 'i' ?! 
				setTimeout(queueCallback, 1, i);

			});

			if (i === number / 2) {
				cm.schedule(chd, function() {
					result += '!drop!';
					cm.done('name|d');
				});
			}
		}
		
	});

});
