(ns simple-time.core
  (:refer-clojure :exclude [format + - = not= < > <= >=])
  (:require [simple-time.interop :as jt])
  (:import [org.joda.time LocalDateTime DateTimeZone Duration Period]
           [org.joda.time.format DateTimeFormat DateTimeFormatter ISODateTimeFormat]))

(set! *warn-on-reflection* true)

;; ****************************************************************************

(defrecord ^:no-doc ^:private SimpleDateTime [^LocalDateTime datetime]
  Comparable
   (compareTo [this other] (.compareTo datetime (:datetime other))))

(alter-meta! #'->SimpleDateTime assoc :no-doc true :private true)
(alter-meta! #'map->SimpleDateTime assoc :no-doc true :private true)

(defn datetime?
  "Returns whether the specified value is a datetime."
  [value]
  (instance? SimpleDateTime value))

;; TODO override tostring with format
#_(defmethod clojure.core/print-method user.MyRecord [x writer]
(.write writer (:name x)))

(defn datetime
  "Creates a new datetime.

  Examples:

    (datetime) -> the current time
    (datetime 1390631873847) -> a java epoch (ms since Jan 1, 1970)
    (datetime 2014 1 2) -> just the date
    (datetime 2014 1 2 12 34 56) -> date & time
    (datetime 2014 1 2 12 34 56 789) -> date & time w/ milliseconds
"
  ([]
    (->SimpleDateTime (LocalDateTime.)))
  ([epoch]
    {:pre [(number? epoch)]}
    (->SimpleDateTime (LocalDateTime. epoch)))
  ([year month day]
    {:pre [(every? number? [year month day])]}
    (->SimpleDateTime (LocalDateTime. year month day 0 0 0 0)))
  ([year month day hour minute second]
    {:pre [(every? number? [year month day hour minute second])]}
    (->SimpleDateTime (LocalDateTime. year month day hour minute second)))
  ([year month day hour minute second millisecond]
    {:pre [(every? number? [year month day hour minute second millisecond])]}
    (->SimpleDateTime (LocalDateTime. year month day hour minute second millisecond))))

;; ****************************************************************************

(defn datetime->year
  "Returns the year of the specified date.

  Example:
    => (datetime->year (datetime 2014 1 2))
    2014
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getYear))

(defn datetime->month
  "Returns the month (1-12) of the specified date.

  Example:
    => (datetime->month (datetime 2014 1 2))
    1
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getMonthOfYear))

(defn datetime->day
  "Returns the day (1-31) of the specified date.

  Example:
    => (datetime->day (datetime 2014 1 2))
    2
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getDayOfMonth))

(defn datetime->hour
  "Returns the hour (0-23) of the specified date.

  Example:
    => (datetime->hour (datetime 2014 1 2 12 34 56))
    12
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getHourOfDay))

(defn datetime->minute
  "Returns the minute (0-59) of the specified date.

  Example:
    => (datetime->minute (datetime 2014 1 2 12 34 56))
    34
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getMinuteOfHour))

(defn datetime->second
  "Returns the second (0-59) of the specified date.

  Example:
    => (datetime->second (datetime 2014 1 2 12 34 56))
    56
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getSecondOfMinute))

(defn datetime->millisecond
  "Returns the milliseconds (0-999) of the specified date.

  Example:
    => (datetime->millisecond (datetime 2014 1 2 12 34 56 789))
    789
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getMillisOfSecond))

(defn datetime->epoch
  "Returns the java epoch (milliseconds since Jan 1, 1970) of the specified date"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .toDateTime .getMillis))

(defn datetime->date
  "Returns the date with no time component.

  Example:
    => (= (datetime->date (datetime 2014 1 2 12 34 56))
          (datetime 2014 1 2))
    true
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (apply simple-time.core/datetime
         ((juxt datetime->year datetime->month datetime->day) datetime)))

(defn datetime->day-of-week
  "Returns the day of the week (1-7) of the specified date.

  Example:
    => (datetime->day-of-week (datetime 2014 1 6)) ; Monday
    1
    => (datetime->day-of-week (datetime 2014 1 5)) ; Sunday
    7
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getDayOfWeek))

(defn datetime->day-of-year
  "Returns the day of the year (1-366) of the specified date.

  Example:
    => (datetime->day-of-year (datetime 2014 1 1))   ; New year's day
    1
    => (datetime->day-of-year (datetime 2014 12 31)) ; New year's eve
    365
    => (datetime->day-of-year (datetime 2012 12 31)) ; Leap year
    366
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (-> datetime jt/datetime->LocalDateTime .getDayOfYear))

;; ****************************************************************************

(defrecord ^:no-doc ^:private SimpleTimeSpan [milliseconds]
  Comparable
   (compareTo [this other] (compare milliseconds (:milliseconds other))))

(alter-meta! #'->SimpleTimeSpan assoc :no-doc true :private true)
(alter-meta! #'map->SimpleTimeSpan assoc :no-doc true :private true)

(defn timespan?
  "Returns whether the specified value is a timespan."
  [value]
  (instance? SimpleTimeSpan value))

(def ^:private milliseconds-per-second 1000)
(def ^:private milliseconds-per-minute (* 60 milliseconds-per-second))
(def ^:private milliseconds-per-hour   (* 60 milliseconds-per-minute))
(def ^:private milliseconds-per-day    (* 24 milliseconds-per-hour))

(defn timespan
  "Creates a new timespan, which represents a duration of time.

  Examples:

    (timespan) -> 0 ms
    (timespan 100) -> 100 ms
    (timespan 1 2 3) -> 1 hr, 2 min, 3 sec
    (timespan 1 2 3 4) -> 1 day, 2 hr, 3 min, 4 sec
    (timespan 1 2 3 4 5) -> 1 day, 2 hr, 3 min, 4 sec, 5 ms
"
  ([]
    (->SimpleTimeSpan 0))
  ([milliseconds]
    {:pre [(number? milliseconds)]}
    (->SimpleTimeSpan milliseconds))
  ([hours minutes seconds]
    (timespan 0 hours minutes seconds 0))
  ([days hours minutes seconds]
    (timespan days hours minutes seconds 0))
  ([days hours minutes seconds milliseconds]
    {:pre [(every? number? [days hours minutes seconds milliseconds])]}
    (->SimpleTimeSpan
      (clojure.core/+ (* days milliseconds-per-day)
         (* hours milliseconds-per-hour)
         (* minutes milliseconds-per-minute)
         (* seconds milliseconds-per-second)
         milliseconds))))

(defn days->timespan
  "Returns a timespan with the specified number of days.

  Example:
    (days->timespan 42) -> 42 days
"
  [days]
  {:pre [(number? days)]}
  (->SimpleTimeSpan (* days milliseconds-per-day)))

(defn hours->timespan
  "Returns a timespan with the specified number of hours.

  Example:
    (hours->timespan 42) -> 42 hours
"
  [hours]
  {:pre [(number? hours)]}
  (->SimpleTimeSpan (* hours milliseconds-per-hour)))

(defn minutes->timespan
  "Returns a timespan with the specified number of minutes.

  Example:
    (minutes->timespan 42) -> 42 minutes
"
  [minutes]
  {:pre [(number? minutes)]}
  (->SimpleTimeSpan (* minutes milliseconds-per-minute)))

(defn seconds->timespan
  "Returns a timespan with the specified number of seconds.

  Example:
    (seconds->timespan 42) -> 42 seconds
"
  [seconds]
  {:pre [(number? seconds)]}
  (->SimpleTimeSpan (* seconds milliseconds-per-second)))

(defn milliseconds->timespan
  "Returns a timespan with the specified number of milliseconds.

  Example:
    (milliseconds->timespan 42) -> 42 milliseconds
"
  [milliseconds]
  {:pre [(number? milliseconds)]}
  (->SimpleTimeSpan milliseconds))

;; ****************************************************************************

(defn timespan->total-days
  "Returns the total number of days, whole and fractional, that the specified
instance represents.

  Example:
    => (timespan->total-days (timespan 1 2 3 4 5))
    2084089/1920000
    => (double *1)
    1.085463020833333

  See also: timespan->days
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan :milliseconds (/ milliseconds-per-day)))

(defn timespan->total-hours
  "Returns the total number of hours, whole and fractional, that the specified
instance represents.

  Example:
    => (timespan->total-hours (timespan 1 2 3 4 5))
    2084089/80000
    => (double *1)
    26.0511125

  See also: timespan->hours
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan :milliseconds (/ milliseconds-per-hour)))

(defn timespan->total-minutes
  "Returns the total number of minutes, whole and fractional, that the specified
instance represents.

  Example:
    => (timespan->total-minutes (timespan 1 2 3 4 5))
    6252267/4000
    => (double *1)
    1563.06675

  See also: timespan->minutes
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan :milliseconds (/ milliseconds-per-minute)))

(defn timespan->total-seconds
  "Returns the total number of seconds, whole and fractional, that the specified
instance represents.

  Example:
    => (timespan->total-seconds (timespan 1 2 3 4 5))
    18756801/200
    => (double *1)
    93784.005

  See also: timespan->seconds
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan :milliseconds (/ milliseconds-per-second)))

(defn timespan->total-milliseconds
  "Returns the total number of milliseconds that the specified instance represents.

  Example:
    => (timespan->total-milliseconds (timespan 1 2 3 4 5))
    93784005

  See also: timespan->milliseconds
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan :milliseconds))

(defn timespan->days
  "Returns the days element of the specified timespan.

  Example:
    => (timespan->days (timespan 1 2 3 4 5))
    1

  See also: timespan->total-days
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan timespan->total-days long))

(defn timespan->hours
  "Returns the hours element (0-23) of the specified timespan.

  Example:
    => (timespan->hours (timespan 1 2 3 4 5))
    2
    => (timespan->hours (hours->timespan 36))
    12

  See also: timespan->total-hours
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan timespan->total-hours long (mod 24)))

(defn timespan->minutes
  "Returns the minutes element (0-59) of the specified timespan.

  Example:
    => (timespan->minutes (timespan 1 2 3 4 5))
    3
    => (timespan->minutes (minutes->timespan 90))
    30

  See also: timespan->total-minutes
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan timespan->total-minutes long (mod 60)))

(defn timespan->seconds
  "Returns the seconds element (0-59) of the specified timespan.

  Example:
    => (timespan->seconds (timespan 1 2 3 4 5))
    4
    => (timespan->seconds (seconds->timespan 90))
    30

  See also: timespan->total-seconds
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan timespan->total-seconds long (mod 60)))

(defn timespan->milliseconds
  "Returns the milliseconds element (0-999) of the specified timespan.

  Example:
    => (timespan->milliseconds (timespan 1 2 3 4 5))
    5
    => (timespan->milliseconds (milliseconds->timespan 1234))
    234

  See also: timespan->total-milliseconds
"
  [timespan]
  {:pre [(timespan? timespan)]}
  (-> timespan timespan->total-milliseconds long (mod 1000)))

;; ****************************************************************************

(defrecord SimpleFormatter
  [formatter type round-trip])

(def ^:private string->formatter
  "Creates a formatter from a string. Don't use directly - use formatter instead."
  (memoize
    (fn [format]
      (DateTimeFormat/forPattern format))))

(def datetime-formatters
  {
   :basic-date (->SimpleFormatter (ISODateTimeFormat/basicDate) :datetime (fn [d] (apply datetime ((juxt datetime->year datetime->month datetime->day) d))))
;   :basic-date-time (ISODateTimeFormat/basicDateTime)
;   :basic-date-time-no-ms (ISODateTimeFormat/basicDateTimeNoMillis)
;
;   :basic-ordinal-date (ISODateTimeFormat/basicOrdinalDate)
;   :basic-ordinal-date-time (ISODateTimeFormat/basicOrdinalDateTime)
;   :basic-ordinal-date-time-no-ms (ISODateTimeFormat/basicOrdinalDateTimeNoMillis)
;   
;   :basic-week-date (ISODateTimeFormat/basicWeekDate)
;   :basic-week-date-time (ISODateTimeFormat/basicWeekDateTime)
;   :basic-week-date-time-no-ms (ISODateTimeFormat/basicWeekDateTimeNoMillis)
;   
;   :date (ISODateTimeFormat/date)
;   :date-time (ISODateTimeFormat/dateTime)
;   :date-time-no-ms (ISODateTimeFormat/dateTimeNoMillis)
;   :date-hour (ISODateTimeFormat/dateHour)
;   :date-hour-minute (ISODateTimeFormat/dateHourMinute)
;   :date-hour-minute-second (ISODateTimeFormat/dateHourMinuteSecond)
;   :date-hour-minute-second-ms (ISODateTimeFormat/dateHourMinuteSecondMillis)
;
;   :ordinal-date (ISODateTimeFormat/ordinalDate)
;   :ordinal-date-time (ISODateTimeFormat/ordinalDateTime)
;   :ordinal-date-time-no-ms (ISODateTimeFormat/ordinalDateTimeNoMillis)
;   
;   :week-date (ISODateTimeFormat/weekDate)
;   :week-date-time (ISODateTimeFormat/weekDateTime)
;   :week-date-time-no-ms (ISODateTimeFormat/weekDateTimeNoMillis)
;   :weekyear (ISODateTimeFormat/weekyear)
;   :weekyear-week (ISODateTimeFormat/weekyearWeek)
;   :weekyear-week-day (ISODateTimeFormat/weekyearWeekDay)
;   
;   :year (ISODateTimeFormat/year)
;   :year-month (ISODateTimeFormat/yearMonth)
;   :year-month-day (ISODateTimeFormat/yearMonthDay)
;
;   :short-date (DateTimeFormat/shortDate)
;   :short-date-time (DateTimeFormat/shortDateTime)
;   
;   :medium-date (DateTimeFormat/mediumDate)
;   :medium-date-time (DateTimeFormat/mediumDateTime)
;   
;   :long-date (DateTimeFormat/longDate)
;   :long-date-time (DateTimeFormat/longDateTime)
;   :long-month-day (string->formatter "MMMM d")
;   :long-year-month (string->formatter "MMMM, YYYY")
;   
;   :full-date (DateTimeFormat/fullDate)
;   :full-date-time (DateTimeFormat/fullDateTime)
   })

(def timespan-formatters
  {
;   :basic-time (ISODateTimeFormat/basicTime)
;   :basic-time-no-ms (ISODateTimeFormat/basicTimeNoMillis)
;   :basic-t-time (ISODateTimeFormat/basicTTime)
;   :basic-t-time-no-ms (ISODateTimeFormat/basicTTimeNoMillis)
;   
;   :hour (ISODateTimeFormat/hour)
;   :hour-minute (ISODateTimeFormat/hourMinute)
;   :hour-minute-second (ISODateTimeFormat/hourMinuteSecond)
;   :hour-minute-second-ms (ISODateTimeFormat/hourMinuteSecondMillis)
;   
;   :time (ISODateTimeFormat/time)
;   :time-no-ms (ISODateTimeFormat/timeNoMillis)
;   :t-time (ISODateTimeFormat/tTime)
;   :t-time-no-ms (ISODateTimeFormat/tTimeNoMillis)
;   
;   :short-time (DateTimeFormat/shortTime)
;   :medium-time (DateTimeFormat/mediumTime)
;   :long-time (DateTimeFormat/longTime)
;   :full-time (DateTimeFormat/fullTime)
   })

(def formatters
  (merge datetime-formatters timespan-formatters))

(defn formatter
  "Returns a formatter based on the specified format. If a string is supplied,
creates a new, memoized formatter. If a keyword is supplied, returns a
pre-defined formatter. See format-all for the set of pre-defined formatters.
Generally, this isn't used directly - the same types can be passed to parse and
format.

  Example:
    (formatter \"YYYYmmDD\")
    (formatter :date-time)
"
  [format]
  {:post [%]}
  (cond
    (instance? SimpleFormatter format) (:formatter format)
    (instance? DateTimeFormatter format) format
    (keyword? format) (formatter (formatters format))
    (string? format) (string->formatter format)
    :else (throw (IllegalArgumentException. (str "Unknown formatter:" format)))))

;; TODO format timespan
;; TODO edn inst
(defn format
  "Formats a datetime and returns a string representation of the value.

  Example:
    (format (datetime 2014 1 2 12 34 56 789))
    (format (datetime 2014 1 2 12 34 56 789) \"YYYYmmDD\")
    (format (datetime 2014 1 2 12 34 56 789) :date-time)
"
  ([datetime] (format datetime :date-time))
  ([datetime format]
    (let [^DateTimeFormatter format (formatter format)]
      (.print format (jt/datetime->LocalDateTime datetime)))))

(defn format-all
  "Format a datetime using all known formatters. Useful for exploring the
built-in formatters.

  Example:
    (format-all (datetime 2014 1 2 12 34 56 789))
"
  [datetime]
  (->> datetime-formatters
    (sort-by first)
    (map (fn [[t f]] (prn t) [t (format datetime f)]))
    (into (sorted-map))))

;; TODO fix roundtripping on :date-time
;; TODO test roundtripping on all
(defn parse
  "Parses a string into a datetime based on the specified format. If no format
is provided, ISO8601 is used by default.

  Example:
    (parse \"2014-01-26T19:03:49.825Z\")
    (parse \"20140126\" \"YYYYmmDD\")
"
  ([^String value] (parse value :date-time))
  ([^String value format]
    (let [format (formatter format)]
      (->SimpleDateTime
        (LocalDateTime/parse value format)))))

;; ****************************************************************************

(defn ^:private throw-illegal-math [description datetime timespans]
  (let [form (->> timespans (cons datetime) (map (comp #(.getName ^Class %) type)) (clojure.string/join " "))]
    (throw (AssertionError. (str "Illegal time " description ": (" form ")")))))

(defn ^:private sum-timespans [timespans]
  {:pre [(every? timespan? timespans)]}
  (->> timespans (map :milliseconds) (reduce clojure.core/+)))

(defn +
  "Adds the specified timespans to datetime. When a datetime is involved, there
must be only one datetime.

  Example:
    (+ datetime) -> datetime
    (+ datetime & timespan*) -> datetime

    (+) -> (timespan 0)
    (+ timespan) -> timespan
    (+ timespan & timespan*) -> timespan
"
  {:arglists '([] [datetime & timespans] [timespan & timespans])}
  ([] (->SimpleTimeSpan 0))
  ([datetime]
    {:pre [((some-fn datetime? timespan?) datetime)]}
    datetime)
  ([datetime & timespans]
    (let [all (cons datetime timespans)
          [datetime :as datetimes] (filter datetime? all)
          timespans (filter timespan? all)]
      (cond
        (empty? datetimes) (->SimpleTimeSpan (sum-timespans timespans))
        (clojure.core/= 1 (count datetimes)) (->SimpleDateTime (.plusMillis (jt/datetime->LocalDateTime datetime) (sum-timespans timespans)))
        :else (throw-illegal-math "addition" datetime timespans)))))

(defn ^:private -*
  "Handles (datetime - datetime) and (datetime - timespans*)"
  [^SimpleDateTime datetime [first & rest :as timespans]]
  (cond 
    (and (datetime? first) (empty? rest))
    (->SimpleTimeSpan (clojure.core/- (datetime->epoch datetime) (datetime->epoch first)))
    
    (every? timespan? timespans)
    (->> timespans sum-timespans Duration. (.minus (jt/datetime->LocalDateTime datetime)) ->SimpleDateTime)
    
    :else (throw-illegal-math "subtraction" datetime timespans)))

(defn -
  "Subtracts datetimes and timespans.

  Example:
    (- timespan) -> -timespan
    (- datetime datetime) -> timespan
    (- datetime & timespan*) -> datetime
    (- timespan & timespan*) -> timespan

    (- (datetime 2014 1 1) (days->timespan 7)) -> (datetime 2013 12 25)
    (- (datetime 2014 1 1) (datetime 2013 12 25)) -> (days->timespan 7)
    (- (datetime 2013 12 25) (datetime 2014 1 1)) -> (days->timespan -7)
"
  {:arglists '([timespan] [datetime datetime] [datetime & timespans] [timespan & timespans])}
  ([^SimpleTimeSpan timespan]
    {:pre [(timespan? timespan)]}
    (->SimpleTimeSpan (clojure.core/- (:milliseconds timespan))))
  ([datetime & timespans]
    (cond
      (timespan? datetime) (->SimpleTimeSpan (clojure.core/- (timespan->total-milliseconds datetime) (sum-timespans timespans)))
      (datetime? datetime) (-* datetime timespans)
      :else (throw-illegal-math "subtraction" datetime timespans))))

(defn add-years
  "Adds the specified number of years to a datetime.

  Example:
    (add-years (datetime 2014 1 1) 6) -> (datetime 2020 1 1)
    (add-years (datetime 2012 2 29) 1) -> (datetime 2013 2 28)
"
  [datetime years]
  {:pre [(datetime? datetime) (number? years)]}
  (->SimpleDateTime (-> datetime jt/datetime->LocalDateTime (.plusYears years))))

(defn add-months
  "Adds the specified number of months to a datetime.

  Example:
    (add-months (datetime 2014 1 1) 6) -> (datetime 2014 7 1)
"
  [datetime months]
  {:pre [(datetime? datetime) (number? months)]}
  (->SimpleDateTime (-> datetime jt/datetime->LocalDateTime (.plusMonths months))))

(defn add-days
  "Adds the specified number of days to a datetime.

  Example:
    (add-days (datetime 2014 1 1) 60) -> (datetime 2014 3 2)
"
  [datetime days]
  {:pre [(datetime? datetime) (number? days)]}
  (->SimpleDateTime (-> datetime jt/datetime->LocalDateTime (.plusDays days))))

(defn add-hours
  "Adds the specified number of hours to a datetime.

  Example:
    (add-hours (datetime 2014 1 1) 42) -> (datetime 2014 1 2 18 0 0)
"
  [datetime hours]
  {:pre [(datetime? datetime) (number? hours)]}
  (->SimpleDateTime (-> datetime jt/datetime->LocalDateTime (.plusHours hours))))

(defn add-minutes
  "Adds the specified number of minutes to a datetime.

  Example:
    (add-minutes (datetime 2014 1 1) 42) -> (datetime 2014 1 1 0 42 0)
"
  [datetime minutes]
  {:pre [(datetime? datetime) (number? minutes)]}
  (->SimpleDateTime (-> datetime jt/datetime->LocalDateTime (.plusMinutes minutes))))

(defn add-seconds
  "Adds the specified number of seconds to a datetime.

  Example:
    (add-seconds (datetime 2014 1 1) 42) -> (datetime 2014 1 1 0 0 42)
"
  [datetime seconds]
  {:pre [(datetime? datetime) (number? seconds)]}
  (->SimpleDateTime (-> datetime jt/datetime->LocalDateTime (.plusSeconds seconds))))

(defn add-milliseconds
  "Adds the specified number of milliseconds to a datetime.

  Example:
    (add-milliseconds (datetime 2014 1 1) 42) -> (datetime 2014 1 1 0 0 0 42)
"
  [datetime milliseconds]
  {:pre [(datetime? datetime) (number? milliseconds)]}
  (->SimpleDateTime (-> datetime jt/datetime->LocalDateTime (.plusMillis milliseconds))))

(defn ^:private value-fn
  "Selects a value for comparisons"
  [value]
  (cond
    (datetime? value) (datetime->epoch value)
    (timespan? value) (:milliseconds value)
    :else (throw (IllegalArgumentException. (str "Unknown value:" value)))))
  
(defn ^:private compare* [comparator]
  (fn [t & more]
    (->> (cons t more)
      (map value-fn)
      (apply comparator))))

(def
  ^{:doc "Tests one or more datetimes or timespans for equality.

  Example:
    (= (datetime 2014 1 1) (datetime 2014 1 1))
"}
  = (compare* clojure.core/=))

(def
  ^{:doc "Tests one or more datetimes or timespans for inequality.

  Example:
    (not= (datetime 2014 1 1) (datetime 2014 1 2))
"}
  not= (compare* clojure.core/not=))

(def
  ^{:doc "Tests one or more datetimes or timespans for increasing order.

  Example:
    (< (datetime 2014 1 1) (datetime 2014 1 2))
"}
  < (compare* clojure.core/<))

(def
  ^{:doc "Tests one or more datetimes or timespans for decreasing order.

  Example:
    (> (datetime 2014 1 2) (datetime 2014 1 1))
"}
  > (compare* clojure.core/>))

(def
  ^{:doc "Tests one or more datetimes or timespans for increasing order.

  Example:
    (<= (datetime 2014 1 1) (datetime 2014 1 1) (datetime 2014 1 2))
"}
  <= (compare* clojure.core/<=))

(def
  ^{:doc "Tests one or more datetimes or timespans for decreasing order.

  Example:
    (>= (datetime 2014 1 2) (datetime 2014 1 2) (datetime 2014 1 1))
"}
  >= (compare* clojure.core/>=))

(defn duration
  "The absolute value of a timespan.

  Example:
    (duration (timespan -100)) -> (timespan 100)
"
  [^SimpleTimeSpan timespan]
  {:pre [(timespan? timespan)]}
  (->SimpleTimeSpan (java.lang.Math/abs ^long (:milliseconds timespan))))

;; ****************************************************************************

(defn now
  "Returns the current datetime."
  []
  (->SimpleDateTime (LocalDateTime.)))

(defn today
  "Returns the current day with no time."
  []
  (datetime->date (now)))

(defn utc-now
  "Returns the current datetime in the UTC time zone."
  []
  (->SimpleDateTime (LocalDateTime. DateTimeZone/UTC)))

;; TODO ->utc
#_(defn ->utc
   "Converts a local datetime to UTC."
   []=)

(defn datetime->time-of-day
  "Returns a timespan based on the time of day.

  Example:
    (= (datetime->time-of-day (datetime 2014 1 2 12 34 56))
       (timespan 12 34 56))
    (= (datetime->time-of-day (datetime 2014 1 2 12 34 56 789))
       (timespan 0 12 34 56 789))
"
  [datetime]
  {:pre [(datetime? datetime)]}
  (apply timespan 0 ((juxt datetime->hour datetime->minute datetime->second datetime->millisecond) datetime)))

(defn days-in-month
  "How many days are in a given month and year?"
  [year month]
  {:pre [(every? number? [year month])]}
  (let [value (jt/datetime->LocalDateTime (datetime year month 1))]
    (.. value dayOfMonth getMaximumValue)))
