(ns simple-time.core
  (:refer-clojure :exclude [format + - = not= < > <= >= with-precision range])
  (:require [simple-time.interop :as jt])
  (:import [org.joda.time LocalDateTime DateTimeZone Duration Period]
           [org.joda.time.format DateTimeFormat DateTimeFormatter ISODateTimeFormat]
           [org.joda.time.chrono ISOChronology]))

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
    (->SimpleDateTime (LocalDateTime. epoch (ISOChronology/getInstanceUTC))))
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
  (-> datetime jt/datetime->LocalDateTime (.toDateTime DateTimeZone/UTC) .getMillis))

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

    (+ (datetime 2013 12 25) (days->timespan 7)) -> (datetime 2014 1 1)
    (+ (hours->timespan 1) (minutes->timespan 2)) -> (timespan 1 2 0)
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
        (empty? datetimes)
        (->SimpleTimeSpan (sum-timespans timespans))

        (clojure.core/= 1 (count datetimes))
        (->SimpleDateTime
          (loop [ldt (jt/datetime->LocalDateTime datetime)
                 remaining (sum-timespans timespans)]
            (if (clojure.core/<= remaining Integer/MAX_VALUE)
              (.plusMillis ldt remaining)
              (recur
                (.plusMillis ldt Integer/MAX_VALUE)
                (clojure.core/- remaining Integer/MAX_VALUE)))))

          :else
          (throw-illegal-math "addition" datetime timespans)))))

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
    (add-months (datetime 2014 1 31) 1) -> (datetime 2014 1 28)
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

(defn with-precision
  "Returns a datetime or timespan with the desired precision. precision is a set
of fields to retain. If :year is not specified, returns a timespan. Available
fields are :year, :month, :day, :hour, :minute, :second, and :millisecond. When
a datetime or timespan is not specified, returns a function that takes a
datetime or timespan and returns it with the specified precision.

  Example:
    (with-precision #{:year :month} (datetime 2014 1 15)) -> (datetime 2014 1 1)
    (with-precision #{:year :month} (datetime 2014 1 15 12 34 56 789)) -> (datetime 2014 1 1)
    (with-precision #{:hour :minute :second} (datetime 2014 1 15 12 34 56 789)) -> (timespan 12 34 56)

    (def ym (with-precision #{:year :month}))
    (ym (datetime 2014 1 15)) -> (datetime 2014 1 1)

  Note: You cannot specify :month without :year
"
  ([precision] (partial with-precision precision))
  ([precision datetime]
    {:pre [(set? precision)]}
    (cond
      (datetime? datetime)
      (cond
        (:year precision)
        (simple-time.core/datetime
          (datetime->year datetime)
          (if (:month precision) (datetime->month datetime) 1)
          (if (:day precision) (datetime->day datetime) 1)
          (if (:hour precision) (datetime->hour datetime) 0)
          (if (:minute precision) (datetime->minute datetime) 0)
          (if (:second precision) (datetime->second datetime) 0)
          (if (:millisecond precision) (datetime->millisecond datetime) 0))

        (:month precision)
        (throw (IllegalArgumentException. "When using :month precision, you must also use :year."))

        :else
        (timespan
          (if (:day precision) (datetime->day datetime) 0)
          (if (:hour precision) (datetime->hour datetime) 0)
          (if (:minute precision) (datetime->minute datetime) 0)
          (if (:second precision) (datetime->second datetime) 0)
          (if (:millisecond precision) (datetime->millisecond datetime) 0)))

      (timespan? datetime)
      (timespan
          (if (:day precision) (timespan->days datetime) 0)
          (if (:hour precision) (timespan->hours datetime) 0)
          (if (:minute precision) (timespan->minutes datetime) 0)
          (if (:second precision) (timespan->seconds datetime) 0)
          (if (:millisecond precision) (timespan->milliseconds datetime) 0)))))

(defn datetime->date
  "Returns the date with no time component.

  Example:
    (datetime->date (datetime 2014 1 2 12 34 56)) -> (datetime 2014 1 2)
"
  [^SimpleDateTime datetime]
  {:pre [(datetime? datetime)]}
  (with-precision #{:year :month :day} datetime))

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

(defn datetime->time-of-day
  "Returns a timespan based on the time of day.

  Example:
    (datetime->time-of-day (datetime 2014 1 2 12 34 56)) -> (timespan 12 34 56))
    (datetime->time-of-day (datetime 2014 1 2 12 34 56 789)) -> (timespan 0 12 34 56 789))
"
  [datetime]
  {:pre [(datetime? datetime)]}
  (with-precision #{:hour :minute :second :millisecond} datetime))

(defn days-in-month
  "How many days are in a given month and year?

  Example:
    (days-in-month 2014 1) -> 31
    (days-in-month 2014 2) -> 28
    (days-in-month 2012 2) -> 29

    (days-in-month (datetime 2014 1 15)) -> 31
"
  ([year month]
    {:pre [(every? number? [year month])]}
    (let [value (jt/datetime->LocalDateTime (datetime year month 1))]
      (.. value dayOfMonth getMaximumValue)))
  ([datetime] (days-in-month (datetime->year datetime) (datetime->month datetime))))

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

(defn range
  "Returns a lazy sequence of datetimes, beginning with start (inclusive) through
end (exclusive). If end is not specified, the sequence will be infinite. The
parameter step specifies the interval. It can be a number (increase by that
number of days), a timespan (increase by that amount of time), or any function
that takes a single datetime and returns another datetime. If a function is used
to step, it must be free of side effects.

  Example:
    (->> (range (datetime 2014 1 1)) (take 4))
    (range (datetime 2014 1 1) (datetime 2014 1 4))
    (range (datetime 2014 1 1) (datetime 2014 1 4) 2)
    (range (datetime 2014 1 1) (datetime 2013 12 25) -2)
    (range (datetime 2014 1 1 0 0 0) (datetime 2014 1 1 6 0 0) (hours->timespan 2))
    (range (datetime 2013 11 1) (datetime 2014 6 1)
           (fn [dt]
             (let [dt (add-months dt 1)
                   y (datetime->year dt)
                   m (datetime->month dt)
                   d (days-in-month dt)]
             (datetime y m d))))
"
  ([start]
    (range start nil 1))
  ([start end]
    (range start end 1))
  ([start end step]
    {:pre [(datetime? start) ((some-fn datetime? nil?) end)]}
    (let [step-fn (cond
                    (timespan? step) #(+ % step)
                    (number? step) #(add-days % step)
                    (fn? step) step
                    :else (throw (IllegalArgumentException. (str "Unknown step fn:" step))))
          ;; if the first step increases, the sequence is increasing; else decreasing
          comp (delay (if (< start (step-fn start)) < >))]
      (->>
        (iterate step-fn start)
        (take-while #(or (nil? end) (@comp % end)))))))

(defn total-months
  "EXPERIMENTAL - May change or be removed.

Calculates the total number of months between start (inclusive) and end
(exclusive). The calculation used is the sum of 1/days-in-month for each date
in the range."
  [start end]
  (->> (range start end)
    (map #(/ 1 (days-in-month %)))
    (reduce clojure.core/+)))

;; ****************************************************************************

(defrecord SimpleFormatter
  [formatter pre-format post-format pre-parse post-parse round-trip])

(alter-meta! #'->SimpleFormatter assoc :no-doc true :private true)
(alter-meta! #'map->SimpleFormatter assoc :no-doc true :private true)

(defn ^:private ->simple-formatter [& {:as values}]
  {:pre [(:formatter values)]}
  (let [defaults {:pre-format identity
                  :post-format identity
                  :pre-parse identity
                  :post-parse identity
                  :round-trip identity}]
    (map->SimpleFormatter (merge defaults values))))

(def ^:private string->formatter
  "Creates a formatter from a string. Don't use directly - use formatter instead."
  (memoize
    (fn [format]
      (.withZoneUTC (DateTimeFormat/forPattern format)))))

(defn ^:private add-timezone
  "Adds a Z to the end of a local datetime so that it will parse."
  [value]
  (if (clojure.core/<= (byte \0) (byte (last value)) (byte \9))
    (str value "Z")
    value))

(defn ^:private add-timezone-long
  "Adds a Z to the end of a local datetime so that it will parse."
  [^String value]
  (if (or (.endsWith value " PM") (.endsWith value " AM"))
    (str value " GMT")
    value))

;; TODO how to make this private?
(def formatters
  {
   ;; datetime

   :basic-date
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicDate)
     :round-trip (with-precision #{:year :month :day}))

   :basic-date-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicDateTime)
     :pre-parse add-timezone)

   :basic-date-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicDateTimeNoMillis)
     :pre-parse add-timezone
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :basic-ordinal-date
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicOrdinalDate)
     :round-trip (with-precision #{:year :month :day}))

   :basic-ordinal-date-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicOrdinalDateTime)
     :pre-parse add-timezone)

   :basic-ordinal-date-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicOrdinalDateTimeNoMillis)
     :pre-parse add-timezone
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :basic-week-date
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicWeekDate)
     :round-trip (with-precision #{:year :month :day}))

   :basic-week-date-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicWeekDateTime)
     :pre-parse add-timezone)

   :basic-week-date-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicWeekDateTimeNoMillis)
     :pre-parse add-timezone
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :date
   (->simple-formatter
     :formatter (ISODateTimeFormat/date)
     :round-trip (with-precision #{:year :month :day}))

   :date-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/dateTime)
     :pre-parse add-timezone)

   :date-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/dateTimeNoMillis)
     :pre-parse add-timezone
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :date-hour
   (->simple-formatter
     :formatter (ISODateTimeFormat/dateHour)
     :round-trip (with-precision #{:year :month :day :hour}))

   :date-hour-minute
   (->simple-formatter
     :formatter (ISODateTimeFormat/dateHourMinute)
     :round-trip (with-precision #{:year :month :day :hour :minute}))

   :date-hour-minute-second
   (->simple-formatter
     :formatter (ISODateTimeFormat/dateHourMinuteSecond)
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :date-hour-minute-second-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/dateHourMinuteSecondMillis))

   :ordinal-date
   (->simple-formatter
     :formatter (ISODateTimeFormat/ordinalDate)
     :round-trip (with-precision #{:year :month :day}))

   :ordinal-date-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/ordinalDateTime)
     :pre-parse add-timezone)

   :ordinal-date-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/ordinalDateTimeNoMillis)
     :pre-parse add-timezone
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :week-date
   (->simple-formatter
     :formatter (ISODateTimeFormat/weekDate)
     :round-trip (with-precision #{:year :month :day}))

   :week-date-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/weekDateTime)
     :pre-parse add-timezone)

   :week-date-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/weekDateTimeNoMillis)
     :pre-parse add-timezone
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :weekyear
   (->simple-formatter
     :formatter (ISODateTimeFormat/weekyear)
     :round-trip #(with-precision #{:year :month :day}
                    (- % (days->timespan (datetime->day-of-week %)) (days->timespan -1))))

   :weekyear-week
   (->simple-formatter
     :formatter (ISODateTimeFormat/weekyearWeek)
     :round-trip #(with-precision #{:year :month :day}
                    (- % (days->timespan (datetime->day-of-week %)) (days->timespan -1))))

   :weekyear-week-day
   (->simple-formatter
     :formatter (ISODateTimeFormat/weekyearWeekDay)
     :round-trip (with-precision #{:year :month :day}))

   :year
   (->simple-formatter
     :formatter (ISODateTimeFormat/year)
     :round-trip (with-precision #{:year}))

   :year-month
   (->simple-formatter
     :formatter (ISODateTimeFormat/yearMonth)
     :round-trip (with-precision #{:year :month}))

   :year-month-day
   (->simple-formatter
     :formatter (ISODateTimeFormat/yearMonthDay)
     :round-trip (with-precision #{:year :month :day}))

   :short-date
   (->simple-formatter
     :formatter (DateTimeFormat/shortDate)
     :round-trip (with-precision #{:year :month :day}))

   :short-date-time
   (->simple-formatter
     :formatter (DateTimeFormat/shortDateTime)
     :round-trip (with-precision #{:year :month :day :hour :minute}))

   :medium-date
   (->simple-formatter
     :formatter (DateTimeFormat/mediumDate)
     :round-trip (with-precision #{:year :month :day}))

   :medium-date-time
   (->simple-formatter
     :formatter (DateTimeFormat/mediumDateTime)
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :long-date
   (->simple-formatter
     :formatter (DateTimeFormat/longDate)
     :round-trip (with-precision #{:year :month :day}))

   :long-date-time
   (->simple-formatter
     :formatter (DateTimeFormat/longDateTime)
     :post-format clojure.string/trim
     :pre-parse add-timezone-long
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   :long-month-day
   (->simple-formatter
     :formatter (string->formatter "MMMM d")
     :round-trip nil)

   :long-year-month
   (->simple-formatter
     :formatter (string->formatter "MMMM, YYYY")
     :round-trip (with-precision #{:year :month}))

   :full-date
   (->simple-formatter
     :formatter (DateTimeFormat/fullDate)
     :round-trip (with-precision #{:year :month :day}))

   :full-date-time
   (->simple-formatter
     :formatter (DateTimeFormat/fullDateTime)
     :post-format clojure.string/trim
     :pre-parse add-timezone-long
     :round-trip (with-precision #{:year :month :day :hour :minute :second}))

   ;; timespan

   :basic-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicTime)
     :pre-parse add-timezone
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second :millisecond}))

   :basic-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicTimeNoMillis)
     :pre-parse add-timezone
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second}))

   :basic-t-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicTTime)
     :pre-parse add-timezone
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second :millisecond}))

   :basic-t-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/basicTTimeNoMillis)
     :pre-parse add-timezone
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second}))

   :hour
   (->simple-formatter
     :formatter (ISODateTimeFormat/hour)
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour}))

   :hour-minute
   (->simple-formatter
     :formatter (ISODateTimeFormat/hourMinute)
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute}))

   :hour-minute-second
   (->simple-formatter
     :formatter (ISODateTimeFormat/hourMinuteSecond)
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second}))

   :hour-minute-second-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/hourMinuteSecondMillis)
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second :millisecond}))

   :time
   (->simple-formatter
     :formatter (ISODateTimeFormat/time)
     :pre-parse add-timezone
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second :millisecond}))

   :time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/timeNoMillis)
     :pre-parse add-timezone
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second}))

   :t-time
   (->simple-formatter
     :formatter (ISODateTimeFormat/tTime)
     :pre-parse add-timezone
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second :millisecond}))

   :t-time-no-ms
   (->simple-formatter
     :formatter (ISODateTimeFormat/tTimeNoMillis)
     :pre-parse add-timezone
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second}))

   :short-time
   (->simple-formatter
     :formatter (DateTimeFormat/shortTime)
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute}))

   :medium-time
   (->simple-formatter
     :formatter (DateTimeFormat/mediumTime)
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second}))

   :long-time
   (->simple-formatter
     :formatter (DateTimeFormat/longTime)
     :post-format clojure.string/trim
     :pre-parse add-timezone-long
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second}))

   :full-time
   (->simple-formatter
     :formatter (DateTimeFormat/fullTime)
     :post-format clojure.string/trim
     :pre-parse add-timezone-long
     :post-parse datetime->time-of-day
     :round-trip (with-precision #{:hour :minute :second}))
   })

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
  ([format]
    {:post [%]}
    (cond
      (instance? SimpleFormatter format) format
      (instance? DateTimeFormatter format) (->simple-formatter :formatter format)
      (keyword? format) (formatter (formatters format))
      (string? format) (formatter (string->formatter format))
      (map? format) (->simple-formatter format)
      :else (throw (IllegalArgumentException. (str "Unknown formatter:" format)))))
  ([format timezone]
    (-> (formatter format)
      (update-in [:formatter] #(.withZone ^DateTimeFormatter % (DateTimeZone/forID timezone))))))

;; TODO edn inst
;; TODO timespan
(defn format
  "Formats a datetime and returns a string representation of the value.

  Example:
    => (format (datetime 2014 1 2 12 34 56 789))
    \"2014-01-02T12:34:56.789\"

    => (format (datetime 2014 1 2 12 34 56 789) \"YYYYmmDD\")
    \"20143402\"

    => (format (datetime 2014 1 2 12 34 56 789) :medium-date-time)
    \"Jan 2, 2014 12:34:56 PM\"
"
  ([datetime] (format datetime :date-time))
  ([datetime format]
    (let [{:keys [pre-format formatter post-format]} (formatter format)]
      (->> datetime
        pre-format
        jt/datetime->LocalDateTime
        (.print ^DateTimeFormatter formatter)
        post-format))))

(defn format-all
  "Format a datetime using all known formatters. Useful for exploring the
built-in formatters.

  Example:
    (format-all (datetime 2014 1 2 12 34 56 789))
"
  [datetime]
  (->> formatters
    (sort-by first)
    (map (fn [[t f]] [t (format datetime f)]))
    (into (sorted-map))))

(defn parse
  "Parses a string into a datetime based on the specified format. If no format
is provided, ISO8601 is used by default.

  Example:
    (parse \"2014-01-02T12:34:56.789\") -> (datetime 2014 1 2 12 34 56 789)
    (parse \"20140102\" \"YYYYmmDD\") -> (datetime 2014 1 2)
    (parse \"Jan 2, 2014 12:34:56 PM\" :medium-date-time) -> (datetime 2014 1 2 12 34 56)
"
  ([^String value] (parse value :date-time))
  ([^String value format]
    (let [{:keys [pre-parse formatter post-parse]} (formatter format)]
      (-> value
        pre-parse
        (LocalDateTime/parse formatter)
        ->SimpleDateTime
        post-parse))))
