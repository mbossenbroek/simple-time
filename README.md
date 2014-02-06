![](can-the-boy-tell-time.png)

# simple-time

A Clojure datetime library for people who can't tell time

## Artifacts

`simple-time` is available from clojars:

With Leiningen:

``` clj
[simple-time "0.1.0"]
```

With Maven:

``` xml
<dependency>
  <groupId>simple-time</groupId>
  <artifactId>simple-time</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage

[Full API docs here](http://mbossenbroek.github.io/simple-time/simple-time.core.html)

`simple-time` has two things: `datetime` and `timespan`

### Making time

``` clj
    (datetime) -> the current time
    (datetime 1390631873847) -> a java epoch (ms since Jan 1, 1970)
    (datetime 2014 1 2) -> just the date
    (datetime 2014 1 2 12 34 56) -> date & time
    (datetime 2014 1 2 12 34 56 789) -> date & time w/ milliseconds
```

### What time is it?

``` clj
    (now) -> the current datetime
    (today) -> the current date
    (utc-now) -> the current datetime in UTC
```

### What day is it?

``` clj
    => (datetime->year (datetime 2014 1 2))
    2014
    => (datetime->month (datetime 2014 1 2))
    1
    => (datetime->day (datetime 2014 1 2))
    2
    => (datetime->hour (datetime 2014 1 2 12 34 56))
    12
    => (datetime->minute (datetime 2014 1 2 12 34 56))
    34
    => (datetime->second (datetime 2014 1 2 12 34 56))
    56
    => (datetime->millisecond (datetime 2014 1 2 12 34 56 789))
    789
```

### How long does it take?

``` clj
    (timespan) -> 0 ms
    (timespan 100) -> 100 ms
    (timespan 1 2 3) -> 1 hr, 2 min, 3 sec
    (timespan 1 2 3 4) -> 1 day, 2 hr, 3 min, 4 sec
    (timespan 1 2 3 4 5) -> 1 day, 2 hr, 3 min, 4 sec, 5 ms

    (days->timespan 42) -> 42 days
    (hours->timespan 42) -> 42 hours
    (minutes->timespan 42) -> 42 minutes
    (seconds->timespan 42) -> 42 seconds
    (milliseconds->timespan 42) -> 42 milliseconds
```

### But what is that in days/hours/etc?

``` clj
    => (timespan->total-days (timespan 1 2 3 4 5))
    2084089/1920000
    => (double *1)
    1.085463020833333

    => (timespan->total-minutes (timespan 1 2 3 4 5))
    6252267/4000
    => (double *1)
    1563.06675
```

### Just the hours/minutes/seconds

``` clj
    => (timespan->days (timespan 1 2 3 4 5))
    1
    => (timespan->hours (timespan 1 2 3 4 5))
    2
    => (timespan->minutes (timespan 1 2 3 4 5))
    3
    => (timespan->seconds (timespan 1 2 3 4 5))
    4
    => (timespan->milliseconds (timespan 1 2 3 4 5))
    5
```

### Strings

``` clj
    => (format (datetime 2014 1 2 12 34 56 789))
    "2014-01-02T12:34:56.789"

    => (format (datetime 2014 1 2 12 34 56 789) "YYYYmmDD")
    "20143402"

    => (format (datetime 2014 1 2 12 34 56 789) :medium-date-time)
    "Jan 2, 2014 12:34:56 PM"

    (parse "2014-01-02T12:34:56.789") -> (datetime 2014 1 2 12 34 56 789)
    (parse "20140102" "YYYYmmDD") -> (datetime 2014 1 2)
    (parse "Jan 2, 2014 12:34:56 PM" :medium-date-time) -> (datetime 2014 1 2 12 34 56)
```

You can create a formatter by passing in a string or using a keyword for a predefined formatter. Use [`format-all`](http://mbossenbroek.github.io/simple-time/simple-time.core.html#var-format-all) to explore the predefined formats. Joda-time formatters also work.

### Let's do some math

``` clj
    (+ datetime) -> datetime
    (+ datetime & timespan*) -> datetime

    (+) -> (timespan 0)
    (+ timespan) -> timespan
    (+ timespan & timespan*) -> timespan

    (+ (datetime 2013 12 25) (days->timespan 7)) -> (datetime 2014 1 1)
    (+ (hours->timespan 1) (minutes->timespan 2)) -> (timespan 1 2 0)

    (- timespan) -> -timespan
    (- datetime datetime) -> timespan
    (- datetime & timespan*) -> datetime
    (- timespan & timespan*) -> timespan

    (- (datetime 2014 1 1) (days->timespan 7)) -> (datetime 2013 12 25)
    (- (datetime 2014 1 1) (datetime 2013 12 25)) -> (days->timespan 7)
    (- (datetime 2013 12 25) (datetime 2014 1 1)) -> (days->timespan -7)

    (duration (timespan -100)) -> (timespan 100)
```

Months and years can't be in timespans, so we have this instead:

``` clj
    (add-years (datetime 2014 1 1) 6) -> (datetime 2020 1 1)
    (add-years (datetime 2012 2 29) 1) -> (datetime 2013 2 28)

    (add-months (datetime 2014 1 1) 6) -> (datetime 2014 7 1)
    (add-months (datetime 2014 1 31) 1) -> (datetime 2014 1 28)
```

### Comparisons

``` clj
    (= (datetime 2014 1 1) (datetime 2014 1 1))
    (not= (datetime 2014 1 1) (datetime 2014 1 2))
    (< (datetime 2014 1 1) (datetime 2014 1 2))
    (> (datetime 2014 1 2) (datetime 2014 1 1))
    (<= (datetime 2014 1 1) (datetime 2014 1 1) (datetime 2014 1 2))
    (>= (datetime 2014 1 2) (datetime 2014 1 2) (datetime 2014 1 1))
```

### Misc

``` clj
    (datetime->date (datetime 2014 1 2 12 34 56)) -> (datetime 2014 1 2)

    (datetime->time-of-day (datetime 2014 1 2 12 34 56)) -> (timespan 12 34 56))
    (datetime->time-of-day (datetime 2014 1 2 12 34 56 789)) -> (timespan 0 12 34 56 789))

    (days-in-month 2014 1) -> 31
    (days-in-month 2014 2) -> 28
    (days-in-month 2012 2) -> 29

    => (datetime->day-of-week (datetime 2014 1 6)) ; Monday
    1
    => (datetime->day-of-week (datetime 2014 1 5)) ; Sunday
    7

    => (datetime->day-of-year (datetime 2014 1 1))   ; New year's day
    1
    => (datetime->day-of-year (datetime 2014 12 31)) ; New year's eve
    365
    => (datetime->day-of-year (datetime 2012 12 31)) ; Leap year
    366
```

## License

Copyright © 2014 Matt Bossenbroek

Distributed under the Eclipse Public License, the same as Clojure.
