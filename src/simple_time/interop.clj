(ns simple-time.interop
  (:import [org.joda.time LocalDateTime DateTime Duration]))

(defn ^LocalDateTime datetime->LocalDateTime
  "Converts a datetime into a joda-time LocalDateTime"
  [datetime]
  (:datetime datetime))

(defn ^DateTime datetime->DateTime
  "Converts a datetime into a joda-time DateTime"
  [datetime]
  (.toDateTime ^LocalDateTime (:datetime datetime)))

(defn ^Duration timespan->Duration
  "Converts a timespan into a joda-time Duration"
  [timespan]
  (Duration. (:milliseconds timespan)))
