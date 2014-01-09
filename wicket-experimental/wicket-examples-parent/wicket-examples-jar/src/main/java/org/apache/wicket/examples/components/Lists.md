<div class="page-header">
	<h2>Lists <small>Repeating content with Repeaters, ListViews and DataViews</small></h2>
</div>

When you need to repeat markup, based on a list or just in a for
loop, the repeater family of components is your friend. Wicket offers
several repeaters for different purposes:

- [RepeatingView](#RepeatingView) and friends: for iterating some components a number
  of times. 
- [ListView](#ListView) and friends: for repeating markup based on a list of things
- `DataView` and friends: for repeating markup for items coming from a database

<a id="RepeatingView" name="RepeatingView"></a>
### RepeatingView

With a
[RepeatingView](http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/markup/repeater/RepeatingView.html) you can repeat
markup in a loop. The RepeatingView will repeat the markup it is
assigned to as often you tell it to.

So for example if we want to render a list of primary colors, we can
do the following:

{% highlight html %}
    <ul>
	    <li wicket:id="colors"></li>
    </ul>
{% endhighlight %}

The idea is to fill each `li` tag with a primary color:

{% highlight java %}
    String[] colors = { "red", "blue", "yellow" };
	RepeatingView colorsView = new RepeatingView("colors");
	add(colorsView);
	for(String color : colors) {
		colorsView.add(new Label(colorsView.newChildId(), color));
	}
{% endhighlight %}

In this example you add a RepeatingView to the page. Next you iterate
through the primary colors, adding a new label to the repeating view
for each color.

Each child of the RepeatingView needs an unique component identifier,
so we ask the repeating view for a `newChildId`.

<a id="ListView" name="ListView"></a>
### ListView

A
[ListView](http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apac
he/wicket/markup/html/list/ListView.html) is useful for displaying
lists of items, for example a list of pizza toppings, or menu items.

Taken from the RepeatingView example above, here's how to show a list
of colors using a ListView:

{% highlight html %}
    <ul>
	    <li wicket:id="colors">
			<wicket:container wicket:id="color"></wicket:container>
		</li>
    </ul>
{% endhighlight %}

Note that this markup slightly differs from a RepeatingView's markup.
The ListView will create for each item in the list a `ListItem<T>`
(where T is the element type of the list), and attach that to the
markup identified by its component id. This is what gets repeated.
Any additional components you want to render with each item need to
be added as children to the **item**. In this example you use a
`<wicket:container>` as a child of the `<li>` for rendering the color.

Given a list of colors, the list view will render a list item for
each color:

{% highlight java %}
    List<String> colors = Arrays.asList("red", "blue", "yellow");
	ListView<String> colorsView = new ListView<String>("colors", colors) {
		@Override
		protected void populateItem(ListItem<String> item) {
			item.add(new Label("color", item.getModelObject()))
		}
	};
	add(colorsView);
{% endhighlight %}

The listview receives the list of colors in the constructor. This can
also be a more dynamic list by using an IModel implementation, such
as a LoadableDetachableModel that reconstructs the list for each
request, or a PropertyModel that binds to a list of things on an
entity.

Take care that you add the label to the **item**, not the listview
(which is the context in which `populateItem` is executed).
