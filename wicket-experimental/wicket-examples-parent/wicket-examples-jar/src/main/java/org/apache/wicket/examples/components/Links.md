<div class="page-header">
	<h2>Links <small>Action links, page links, bookmarks and external links</small></h2>
</div>

With links you can trigger actions on the server side, navigate to
other pages or to external sites.

### Action links

When you want to perform some action on the server, triggered by a
click in the browser, you can use [Link](http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/markup/html/link/Link.html)
as the serverside component.

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

The following Java snippet just refreshes the current page, you don't
perform have to perform any action in the `onClick` handler.

{% highlight java %}
    add(new Link<Void>("refresh") {
		@Override
		public void onClick() {
		}
	});
{% endhighlight %}

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

When you use a page instance as the argument to `setResponsePage`,
the resulting URL is relative to the session the user works in and is
not resolvable after the session has ended, resulting in a
`PageExpiredException`.

You can also use the `setResponsePage` method using a page class and
`PageParameters`, resuling in a URL that is absolute, and can be used
even when the user's session was ended.

{% highlight java %}
    add(new Link<Void>("action") {
		@Override
		public void onClick() {
			setResponsePage(OtherPage.class, new PageParameters().add("foo", "bar")));
		}
	});
{% endhighlight %}

{% alert alert-info %}
<strong>Note</strong> The constructor of the page you are linking to needs to be either a
default constructor or a constructor taking `PageParameters` as the
sole parameter—otherwise Wicket will not be able to create an
instance of your page.
{% endalert %}

## Page links

When linking to pages, you might want your users to be able to
bookmark them. Typically an action link is not bookmarkable, since
the generated URL doesn't convey the necessary information to
re-create a particular page instance. To create bookmarkable, session
independent links you should use `BookmarkablePageLink` components.

{% highlight java %}
    add(new BookmarkablePageLink<Void>("menuitem", LinkedPage.class));
{% endhighlight %}

With `PageParameters` you can add additional parameters that are
passed on to your page's constructor:

{% highlight java %}
    add(new BookmarkablePageLink<Void>("grolsch", BeerPage.class, 
                     new PageParameters().add("brand", "grolsch")));
{% endhighlight %}

Note that you can only create bookmarkable links to pages that have a
default constructor or a constructor taking just a PageParameters
object.

The server does not recieve an event when the user clicks on the
link, but directly constructs the linked page and renders it. In
other words: the page that has the bookmarkable link on it never
knows if the link was clicked.

### Bookmarkable page

A page is considered bookmarkable when it has a default constructor,
or a constructor taking PageParameters, or both constructors. If your
page doesn't have at least one of these types of constructors, Wicket
will not know how to instantiate your page.

The following examples show pages that are bookmarkable:

{% highlight java %}
    public class BeerPage extends WebPage {
		public BeerPage() {
			// default constructor
		}
	}
{% endhighlight %}

An example page taking page parameters:

{% highlight java %}
	public class BeerPage extends WebPage {
		public BeerPage(PageParameters parameters) {
			String brand = parameters.get("brand").toString();
		}
	}
{% endhighlight %}

An example of a page that is **not** bookmarkable, since it doens't
have one of the necessary constructors:

{% highlight java %}
	public class BeerPage extends WebPage {
		public BeerPage(Beer beer) {
			String brand = beer.getBrand();
		}
	}
{% endhighlight %}

## Mounting pages

You can map your bookmarkable pages to specific URLs relative to your
application context. You register the URL with the corresponding page
with the application instance.

{% highlight java %}
    public class MyApplication extends WebApplication {
		@Override
		protected void init() {
			super.init();
			mountPage("/beer", BeerPage.class);
		}
	}
{% endhighlight %}

This instructs Wicket to render URLs to the BeerPage page to look like:

    /beer/brand/grolsch
	^^^^^
	|
	mountpath

The `mountPage` method uses the [MountedMapper](http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/core/request/mapper/MountedMapper.html) URL encoder/decoder.

The mount path can contain parameter placeholders, i.e.
`/mount/${foo}/path`. In that case the appropriate segment from the URL
will be accessible as named parameter "foo" in the `PageParameters`.

Similarly when the URL is constructed, the second segment will
contain the value of the "foo" named page parameter. 
Optional parameters are denoted by using a `#` instead of `$`:
`/mount/#{foo}/path/${bar}` has an optional `foo` parameter, a fixed
`/path/` part and a required `bar` parameter.

When in doubt, parameters are matched from left to right, where
required parameters are matched before optional parameters, and
optional parameters eager (from left to right).

## External links

Links to URLs outside your application can be constructed using the
[ExternalLink](http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/markup/html/link/ExternalLink.html).
Similar to bookmarkable links, external links don't recieve a
serverside event: the user is taken directly to the external site.

{% highlight java %}
	add(new ExternalLink("google", "http://google.com"));
{% endhighlight %}
