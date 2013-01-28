Wicket Native WebSockets
========================

Since version 6.0 Wicket provides an experimental module that provides integration with WebSocket support in the web containers like Jetty and Apache Tomcat.

Each of the integrations provide a custom implementation of `WicketFilter <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/protocol/http/WicketFilter.html>`_ that first checks whether the current web request needs to upgrade its protocol from HTTP to WebSocket. This is done by checking the request headers for the special header with name "Upgrade". If the request is an upgrade then the filter registers an endpoint for the web socket connection. Later this endpoint is used to read/write messages from/to the client. 

When a client is connected it is being registered in a global (application scoped) registry using as a key the application, the client session id and the id of the page that registered it. Later when the server needs to push a message it uses this registry to filter which clients need to receive the message.
When a message is received from the client Wicket uses the associated page id for the web connection and loads the Wicket Page as Ajax requests do, then it broadcasts an `IWebSocketMessage <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/protocol/ws/api/message/IWebSocketMessage.html>`_ (:ref:Wicket 1.5 Event system) to all its components and behaviors so they can react on it.
The server-side can push plain text and binary messages to the client - pure web socket messages, but can also add components for re-render, prepend/append JavaScript as you do with AjaxRequestTarget.

h3. How to use it ?

h5. Maven dependency

Add dependency to either org.apache.wicket:wicket-native-websocket-jetty or org.apache.wicket:wicket-native-websocket-tomcat.

h5. Custom WicketFilter 

Setup the custom WicketFilter implementation for the chosen web container in your web.xml.

For Jetty 7.5+ this is 
  <filter-class>org.apache.wicket.protocol.http.Jetty7WebSocketFilter</filter-class>

For Tomcat 7.0.27+:
  <filter-class>org.apache.wicket.protocol.http.Tomcat7WebSocketFilter</filter-class>

h5. WebSocketBehavior

_org.apache.wicket.protocol.ws.api.WebSocketBehavior_ is much like Ajax behaviors that you may know from earlier versions of Wicket.
Add WebSocketBehavior to the page that will use web socket communication:

MyPage.java

::

    public class MyPage extends WebPage {

       public MyPage() {
           add(new WebSocketBehavior() {
               @Override
               protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
               }
           });
       }
    }

Use _message.getText()_ to read the message sent by the client and use the passed _handler.push(String)_ to push a text message to the connected client. Additionally you can use _handler.add(Component...)_ to add Wicket components for re-render, _handler#prependJavaScript(CharSequence)_ and _handler#appendJavaScript(CharSequence)_ as you do with AjaxRequestTarget.

See the demo application at [martin-g's GitHub|https://github.com/martin-g/wicket-native-websocket-example]. It is written with Scala and uses [Akka|http://akka.io] which are not available at Maven central repository so it cannot be hosted at Apache Git servers.

h5. Client side APIs

By adding a _WebSocketBehavior_ to your component(s) Wicket will contribute _wicket-websocket-jquery.js_ library which provides some helper functions to write your client side code. There is a default websocket connection per Wicket Page opened for you which you can use like _Wicket.WebSocket.send('\{msg: "my message"\}')_.

If you need more WebSocket connections then you can do: _var ws = new Wicket.WebSocket(); ws.send('message');_

To close a connection: _Wicket.WebSocket.close()_ or _ws.close()_.

_Wicket.WebSocket_ is a simple wrapper around the native _window.WebSocket_ API which is used to intercept the calls and to fire special events (Wicket.Event PubSub).
|| Event name || Arguments || Description ||
| /websocket/open | jqEvent | A WebSocket connection has been just opened |
| /websocket/message | jqEvent, message | A message has been received from the server |
| /websocket/closed | jqEvent | A WebSocket connection has been closed |
| /websocket/error | jqEvent | An error occurred in the communication. The connection will be closed |

If you don't need to listen for these events then you can just use the native JavaScript API (window.WebSocket).

A demo code can be seen in the [Demo Application|https://github.com/martin-g/wicket-native-websocket-example/blob/master/src/main/resources/org/apache/wicket/websocket/jetty/example/client.js]

h3. Testing

The module provides _org.apache.wicket.protocol.ws.util.tester.WebSocketTester_ which gives you the possibility to emulate sending and receiving messages without the need to run in a real web container, as WicketTester does this for HTTP requests.

Check [WebSocketTesterTest|https://git-wip-us.apache.org/repos/asf/wicket/repo?p=wicket.git;a=blob;f=wicket-experimental/wicket-native-websocket/wicket-native-websocket-core/src/test/java/org/apache/wicket/protocol/ws/util/tester/WebSocketTesterTest.java;hb=master] for an example.

h3. Differences with Wicket-Atmosphere module.

Wicket-Atmosphere provides integration with [Atmosphere|https://github.com/Atmosphere/atmosphere/] and let it handle the inconsistencies in WebSocket protocol support in different browsers and web containers. If either the browser or the web container do not support WebSockets then Atmosphere will downgrade (depending on the configuration) to either long-polling, streaming, server-side events, jsonp, ... to simulate the long running connection.

Wicket Native WebSocket uses only WebSocket connections, so it wont work for browsers and web containers which do not support WebSockets. There are no plans to add such support in future. Use it only if you really know that you will run your application in an environment that supports WebSockets.

Currently supported web containers are Jetty 7.5+ and Tomcat 7.0.27+.
Currently supported browsers are Google Chrome/Chromium, Firefox 11+, Safari 5.x (with Jetty), IE10.

FAQ
---

1. Request and session scoped beans do not work.

The Web Socket communication is not intercepted by Servlet Filters and Listeners and thus the Dependency Injection libraries have no chance to export the request and session beans.
