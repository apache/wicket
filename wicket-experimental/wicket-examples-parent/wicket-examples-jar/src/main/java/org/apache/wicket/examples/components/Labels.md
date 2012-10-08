<div class="page-header">
	<h2>Labels <small>Displaying text using labels, MultiLineLabels and escaping markup</small></h2>
</div>

With `Label` components you can display dynamic text in
your pages and components.

{% highlight java %}
    add(new Label<String>("text", "Hello, World!"));
{% endhighlight %}

Like all Wicket components, the label needs a counterpart in the
markup with the same wicket:id:

{% highlight html %}
    <span wicket:id="text">gets replaced</span>
{% endhighlight %}

Using models you can display fields from your entities in your pages:

{% highlight java %}
    Person person = ...;
    add(new Label<String>("text", new PropertyModel<String>(person, "name")));
{% endhighlight %}

### Multi-line text

HTML notoriously strips newlines from your text and renders
everything like it is just one line of text. With MultiLineLabel you
can render text like it is supposed to look with paragraphs and
line-breaks.

{% highlight java %}
    add(new Label("txt", "Hello,\nWorld!"));
    add(new MultiLineLabel("multi", "Hello,\nWorld!"));
{% endhighlight %}

Will render the following markup:

{% highlight html %}
    <p>Hello,
    World!</p>
    <p>Hello,<br />World!</p>
{% endhighlight %}

And that will result in text like:

{% highlight html %}
    Hello, World!
    Hello,
    World!
{% endhighlight %}

Notice that the first label is displayed on a single line, while the
`MultiLineLabel` correctly renders the text across multiple lines.

### Label tags

The associated markup tag can be anything: a `<span>`, `<a>`nchor,
`<p>`aragraph, or even if you don't want a surrounding tag in the
final markup a `<wicket:container>`. So for example:

{% highlight html %}
    <span wicket:id="t1"></span>
    <p wicket:id="t2"></p>
    <wicket:container wicket:id="t3"></wicket:container>
{% endhighlight %}

Will render as:

{% highlight html %}
    <span>Hello, World!</span>
    <p>Hello, World!</p>
    Hello, World!
{% endhighlight %}

You can also use `label.setRenderBodyOnly(true)` to instruct Wicket
to just render the body.

### Escaping markup

By default, Wicket escapes all rendered text, preventing JavaScript
injection attacks:

{% highlight java %}
    add(new Label("bad",  "<a onclick=\"alert('Booh')\">Click me</a>"));
{% endhighlight %}
					
Will render safely as the following markup:

{% highlight html %}
    &lt;a onclick=\&quot;alert(&#x27;Booh&#x27;)\&quot;&gt;Click me&lt;/a&gt;
{% endhighlight %}

Which displays in the browser as:

{% highlight html %}
    <a onclick=\"alert('Booh')\">Click me</a>
{% endhighlight %}
