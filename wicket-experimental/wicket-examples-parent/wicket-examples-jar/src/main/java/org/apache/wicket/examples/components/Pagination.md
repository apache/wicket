<div class="page-header">
	<h2>Links <small>Action links, page links, bookmarks and external links</small></h2>
</div>

With links you can trigger actions on the server side, navigate to
other pages or to external sites.

### Action links

When you want to perform some action on the server, triggered by a
click in the browser, you can use `Link` as the serverside component.

{% highlight java %}
    add(new Link<Void>("action") {
		@Override
		public void onClick() {
			// do something
		}
	});
{% endhighlight %}

For a `Link` it doesn't matter if you attach it to an `a` tag,
`input` or even a `div` or `span`. The link will generate for
non-anchor tags javascript onclick handlers automatically. So the
link of our previous example could be attached to:

{% highlight html %}
    <a href="#" wicket:id="action">Click me for some action</a>
{% endhighlight %}

Or a `div` like:

{% highlight html %}
    <div wicket:id="action">Click me for some action</div>
{% endhighlight %}

#### Generics of a Link

The type parameter for a link component is used for the model the
link is supposed to work on. For example you can have a link that
increments a counter on each click:

{% highlight java %}
    add(new Link<Integer>("inc", new Model<Integer>(0)) {
		@Override
		public void onClick() {
			int count = getModelObject();
			setModelObject(count + 1);
		}
	});
{% endhighlight %}

If your link doesn't require a model, but just performs some actions,
then you can use the `Void` type to denote such uses.

#### Going to another page after some action

With the `setResponsePage` method you can instruct Wicket to render a
different page. By default Wicket will re-render the current page
unless you instruct otherwise.

##### Example: refresh current page

The following Java snippet just refreshes the current page:

{% highlight java %}
    add(new Link<Void>("refresh") {
		@Override
		public void onClick() {
		}
	});
{% endhighlight %}

Note that you don't perform any action in the `onClick` handler.

##### Example: navigate to another page

The following example instructs Wicket to render the `OtherPage` as
the response to your user.

{% highlight java %}
    add(new Link<Void>("action") {
		@Override
		public void onClick() {
			setResponsePage(new OtherPage());
		}
	});
{% endhighlight %}

