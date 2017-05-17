# The introduction to Functional Reactive Programming you've been messing

---

So you're curious in learning this new thing called Functional Reactive Programming, particularly its variant comprising of Sodium, Reflex, Reactive Banana, Flapjax and others.

The hardest part of a learning journey is **thinking in Functional Reactive**.  It's a lot about letting go of old imperative and stateful habits of typical programming, forcing your brain to work in a different paradigm.  I hope this helps you.

## "What is Functional Reactive Programming?"

#### Functional Reactive programming is programming with events and behaviors.

But that only begs more questions.

##### What are events?

In a way, this isn't anything new.  Event buses or your typical click events are really events, on which you can observe and do some side effects.  Functional Reactive is that idea on steroids.  You are able to create events of anything, not just from click and hover events.  Events are cheap and ubiquitous, anything can be an event: user inputs, properties, caches, data structures, etc.  For example imagine your Twitter feed would be an event in the same fashion that click events are.  You can listen to that event and react accordingly.

**On top of that, you are given an amazing toolbox of functions to combine, create and filter any of those events.**  That's where the "functional" magic kicks in.  An event can be used as an input to another one.  Even multiple events can be used as an input to another one.  You can __mappend__ two events to merge them.  You can __filter__ an event to get another one that has only those events you are interested in.  You can __fmap__ data values from one event to another new one.

If events are so central to Functional Reactive, let's take a careful look at them, starting with our familiar "clicks on a button" event.

<!--TODO add a diagram-->
An event is a list of **ongoing occurrences ordered in time**.  I can emit only one thing: a value (of some type).

We capture these emitted occurrences only **asynchronously**, by defining a side-effecting function that will execute when a value is emitted.  The "listening" to the event is called subscribing.  The functions we are defining are observers.  The event is the subject being observed.  This is precisely the [Observer Design Pattern](https://en.wikipedia.org/wiki/Observer_pattern).

```
--a---b-c---d---->

a, b, c, d are emitted values

---> is the timeline
```

Since this feels so familiar already, and I don't want you to get bored, let's do something new: we are going to create new click events transformed out of the original click event.

First, let's make a counter event that indicates how many times a button was clicked.  In common Functional Reactive libraries, each event has many functions attached to it, such as `<$>`, `filter`, `reduce`, etc.  When you call one of these functions, such as `(<$> f click-event)`, it returns a **new event** based on the click event.  It does not modify the original click event in any way.  This is a property called **immutability**, and it goes together with Functional Reactive events just like pancakes are good with syrup.  This allows us to chain functions like `(reduce g (<$> f click-event))`, or with a threading macro,

```clojure
(->> click-event
  (<$> f)
  (reduce g))
```

```
  click-event: ---c----c--c----c------c-->
               vvvv <$> (c becomes 1) vvvv
               ---1----1--1----1------1-->
               vvvvvvvvv reduce + vvvvvvvv
counter-event: ---1----2--3----4------5-->
```

TODO: write [great documentation](http://jacobian.org/writing/what-to-write/)

---

### Legal
Based on a work at https://gist.github.com/staltz/868e7e9bc2a7b8c1f754 by Andre Medeiros at http://andre.staltz.com
