Wicket.Event.subscribe('/ajax/call/success', function(attrs, jqXHR, data, textStatus) {
   console.log(data);
});

Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
   console.log(errorThrown);
});