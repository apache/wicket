Some notes on trying to integrate Rico with Wicket (was posted on their forum).

Hi,

I've got a few questions on Rico's (1.1b) ajax support
(which I am currently trying to integrate with Wicket)

* I find the pre-registring of urls quit inconvenient. I
usually have one function (e.g. validate) for several

components on a page, and I'd like to re-use that function
by passing in the actual url for the ajax call. Is it
possible/ supported to make ajax call without
pre-registering the urls first?

* The urls that I start with, have some request parameters
in them allready. However, if I append parameters to that
request, Rico puts another ? before it. Looks like a bug to
me. What I would like to be able to do though, is just use
my whole url, including the parameters I want, and use that
to make an ajax call.

* Do you have any plans of creating a smaller distr (just
the ajax stuff) in the near future? Not that important, but
just curious.

* how does Rico's ajax implementation and browser support
compare to some other frameworks? I have been looking at
Qooxdoo (doesn't work on Safari yet) and Dojo (which seems
to have the best all browser support).

Regards,

Eelco