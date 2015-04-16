(ns simple-time.core-test
  (:use clojure.test)
  (:require [simple-time.core :as st]))

;; ****************************************************************************

(deftest test-datetime?
  (is (st/datetime? (st/datetime)))
  (is (not (st/datetime? (st/timespan 42))))
  (is (not (st/datetime? 42))))

(deftest test-datetime
  (let [datetime (st/datetime)]
    (is (> (st/datetime->epoch datetime)
           1390631873847)))
  (let [datetime (st/datetime 123)]
    (is (= (st/datetime->epoch datetime)
           123)))
  (let [datetime (st/datetime 2014 1 2)]
    (is (= (st/datetime->epoch datetime)
           1388620800000)))
  (let [datetime (st/datetime 2014 1 2 3 4 5)]
    (is (= (st/datetime->epoch datetime)
           1388631845000)))
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->epoch datetime)
           1388631845006)))
  (let [datetime (st/datetime 1950 6 1)]
    (is (= (st/datetime->epoch datetime)
           -618105600000)))
  (let [datetime (st/datetime -618080400000)]
    (is (= (st/datetime->epoch datetime)
           -618080400000)))
  (is (= 0 (st/datetime->epoch (st/datetime 0))))
  (is (thrown? AssertionError (st/datetime "42"))))

;; ****************************************************************************

(deftest test-datetime->year
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->year datetime)
           2014)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->year datetime)
           2014)))
  (is (thrown? AssertionError (st/datetime->year "42"))))

(deftest test-datetime->month
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->month datetime)
           1)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->month datetime)
           1)))
  (is (thrown? AssertionError (st/datetime->month "42"))))

(deftest test-datetime->day
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->day datetime)
           2)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->day datetime)
           2)))
  (is (thrown? AssertionError (st/datetime->day "42"))))

(deftest test-datetime->hour
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->hour datetime)
           3)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->hour datetime)
           3)))
  (is (thrown? AssertionError (st/datetime->hour "42"))))

(deftest test-datetime->minute
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->minute datetime)
           4)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->minute datetime)
           4)))
  (is (thrown? AssertionError (st/datetime->minute "42"))))

(deftest test-datetime->second
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->second datetime)
           5)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->second datetime)
           5)))
  (is (thrown? AssertionError (st/datetime->second "42"))))

(deftest test-datetime->millisecond
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->millisecond datetime)
           6)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->millisecond datetime)
           6)))
  (is (thrown? AssertionError (st/datetime->millisecond "42"))))

(deftest test-datetime->epoch
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->epoch datetime)
           1388631845006)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->epoch datetime)
           1388631845006)))
  (is (thrown? AssertionError (st/datetime->epoch "42")))

  (testing "epochs should be UTC"
    (= 0 (-> 0
           (st/datetime)
           (st/format)
           (st/parse)
           (st/datetime->epoch)))))

;; ****************************************************************************

(deftest test-timespan?
  (is (st/timespan? (st/timespan 42)))
  (is (not (st/timespan? (st/datetime 42))))
  (is (not (st/timespan? 42))))

(deftest test-timespan
  (let [timespan (st/timespan)]
    (is (= (:milliseconds timespan)
           0)))
  (let [timespan (st/timespan 1234)]
    (is (= (:milliseconds timespan)
           1234)))
  (let [timespan (st/timespan 1 2 3)]
    (is (= (:milliseconds timespan)
           3723000)))
  (let [timespan (st/timespan 1 2 3 4)]
    (is (= (:milliseconds timespan)
           93784000)))
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (:milliseconds timespan)
           93784005)))
  (is (thrown? AssertionError (st/timespan "42")))
  (is (thrown? AssertionError (st/timespan "42" 0 0))))

;; ****************************************************************************

(deftest test-timespan->total-days
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->total-days timespan)
           2084089/1920000))
    (is (= (double (st/timespan->total-days timespan))
           1.085463020833333)))
  (is (thrown? AssertionError (st/timespan->total-days "42"))))

(deftest test-timespan->total-hours
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->total-hours timespan)
           2084089/80000))
    (is (= (double (st/timespan->total-hours timespan))
           26.0511125)))
  (is (thrown? AssertionError (st/timespan->total-hours "42"))))

(deftest test-timespan->total-minutes
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->total-minutes timespan)
           6252267/4000))
    (is (= (double (st/timespan->total-minutes timespan))
           1563.06675)))
  (is (thrown? AssertionError (st/timespan->total-minutes "42"))))

(deftest test-timespan->total-seconds
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->total-seconds timespan)
           18756801/200))
    (is (= (double (st/timespan->total-seconds timespan))
           93784.005)))
  (is (thrown? AssertionError (st/timespan->total-seconds "42"))))

(deftest test-timespan->total-milliseconds
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->total-milliseconds timespan)
           93784005))
    (is (= (double (st/timespan->total-milliseconds timespan))
           9.3784005E7)))
  (is (thrown? AssertionError (st/timespan->total-milliseconds "42"))))

(deftest test-timespan->days
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->days timespan)
           1)))
  (is (thrown? AssertionError (st/timespan->days "42"))))

(deftest test-timespan->hours
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->hours timespan)
           2)))
  (is (thrown? AssertionError (st/timespan->hours "42"))))

(deftest test-timespan->minutes
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->minutes timespan)
           3)))
  (is (thrown? AssertionError (st/timespan->minutes "42"))))

(deftest test-timespan->seconds
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->seconds timespan)
           4)))
  (is (thrown? AssertionError (st/timespan->seconds "42"))))

(deftest test-timespan->milliseconds
  (let [timespan (st/timespan 1 2 3 4 5)]
    (is (= (st/timespan->milliseconds timespan)
           5)))
  (is (thrown? AssertionError (st/timespan->milliseconds "42"))))

(deftest test-days->timespan
  (is (= (st/days->timespan 42)
         (st/timespan 3628800000)))
  (is (thrown? AssertionError (st/days->timespan "42"))))

(deftest test-hours->timespan
  (is (= (st/hours->timespan 42)
         (st/timespan 151200000)))
  (is (thrown? AssertionError (st/hours->timespan "42"))))

(deftest test-minutes->timespan
  (is (= (st/minutes->timespan 42)
         (st/timespan 2520000)))
  (is (thrown? AssertionError (st/minutes->timespan "42"))))

(deftest test-seconds->timespan
  (is (= (st/seconds->timespan 42)
         (st/timespan 42000)))
  (is (thrown? AssertionError (st/seconds->timespan "42"))))

(deftest test-milliseconds->timespan
  (is (= (st/milliseconds->timespan 42)
         (st/timespan 42)))
  (is (thrown? AssertionError (st/milliseconds->timespan "42"))))

;; ****************************************************************************

(deftest test-sum-timespans
  (is (= (#'simple-time.core/sum-timespans [])
         0))
  (is (= (#'simple-time.core/sum-timespans [(st/timespan 1)])
         1))
  (is (= (#'simple-time.core/sum-timespans [(st/timespan 1) (st/timespan 2) (st/timespan 3)])
         6)))

(deftest test-+
  (is (= (st/+)
         (st/timespan 0)))
  (is (= (st/+ (st/timespan 123))
         (st/timespan 123)))
  (is (= (st/+ (st/datetime 123))
         (st/datetime 123)))
  (is (= (st/+ (st/datetime 123) (st/timespan 123))
         (st/datetime 246)))
  (is (= (st/+ (st/timespan 123) (st/datetime 123))
         (st/datetime 246)))
  (is (= (st/+ (st/datetime 123) (st/timespan 123) (st/timespan 123))
         (st/datetime 369)))
  (is (= (st/+ (st/timespan 123) (st/timespan 123) (st/datetime 123))
         (st/datetime 369)))
  (is (= (st/+ (st/timespan 123) (st/timespan 123))
         (st/timespan 246)))
  (is (= (st/+ (st/timespan 123) (st/timespan 123) (st/timespan 123))
         (st/timespan 369)))
  (is (thrown? AssertionError (st/+ (st/datetime 123) (st/datetime 123))))
  (is (thrown? AssertionError (st/+ (st/datetime 123) (st/timespan 123) (st/datetime 123)))))

(deftest test--*
  (is (= (#'simple-time.core/-* (st/datetime 123) [(st/datetime 100)])
         (st/timespan 23)))
  (is (= (#'simple-time.core/-* (st/datetime 2014 1 1) [(st/datetime 2013 1 1)])
         (st/days->timespan 365)))
  (is (= (#'simple-time.core/-* (st/datetime 123) [(st/timespan 100)])
         (st/datetime 23)))
  (is (= (#'simple-time.core/-* (st/datetime 123) [(st/timespan 50) (st/timespan 50)])
         (st/datetime 23))))

(deftest test--
  (is (= (st/- (st/timespan 123))
         (st/timespan -123)))
  (is (= (st/- (st/datetime 123) (st/datetime 100))
         (st/timespan 23)))
  (is (= (st/- (st/datetime 123) (st/timespan 100))
         (st/datetime 23)))
  (is (= (st/- (st/datetime 123) (st/timespan 50) (st/timespan 50))
         (st/datetime 23)))
  (is (= (st/- (st/timespan 123) (st/timespan 100))
         (st/timespan 23)))
  (is (= (st/- (st/timespan 123) (st/timespan 50) (st/timespan 50))
         (st/timespan 23)))
  (is (= (st/- (st/datetime 2014 1 1) (st/days->timespan 7))
         (st/datetime 2013 12 25)))
  (is (= (st/- (st/datetime 2014 1 1) (st/datetime 2013 12 25))
         (st/days->timespan 7)))
  (is (= (st/- (st/datetime 2013 12 25) (st/datetime 2014 1 1))
         (st/days->timespan -7)))
  (is (thrown? clojure.lang.ArityException (st/-)))
  (is (thrown? AssertionError (st/- (st/datetime 123))))
  (is (thrown? AssertionError (st/- (st/datetime 123) (st/datetime 123) (st/datetime 123))))
  (is (thrown? AssertionError (st/- (st/timespan 123) (st/datetime 123))))
  (is (thrown? AssertionError (st/- (st/timespan 123) (st/timespan 123) (st/datetime 123)))))

(deftest test-add-years
  (is (= (st/add-years (st/datetime 2014 1 1) 6)
         (st/datetime 2020 1 1)))
  (is (= (st/add-years (st/datetime 2012 2 29) 1)
         (st/datetime 2013 2 28)))
  (is (thrown? AssertionError (st/add-years (st/datetime) "42"))))

(deftest test-add-months
  (is (= (st/add-months (st/datetime 2014 1 1) 6)
         (st/datetime 2014 7 1)))
  (is (= (st/add-months (st/datetime 2014 1 31) 1)
         (st/datetime 2014 2 28)))
  (is (thrown? AssertionError (st/add-months (st/datetime) "42"))))

(deftest test-add-days
  (is (= (st/add-days (st/datetime 2014 1 1) 60)
         (st/datetime 2014 3 2)))
  (is (thrown? AssertionError (st/add-days (st/datetime) "42"))))

(deftest test-add-hours
  (is (= (st/add-hours (st/datetime 2014 1 1) 42)
         (st/datetime 2014 1 2 18 0 0)))
  (is (thrown? AssertionError (st/add-hours (st/datetime) "42"))))

(deftest test-add-minutes
  (is (= (st/add-minutes (st/datetime 2014 1 1) 42)
         (st/datetime 2014 1 1 0 42 0)))
  (is (thrown? AssertionError (st/add-minutes (st/datetime) "42"))))

(deftest test-add-seconds
  (is (= (st/add-seconds (st/datetime 2014 1 1) 42)
         (st/datetime 2014 1 1 0 0 42)))
  (is (thrown? AssertionError (st/add-seconds (st/datetime) "42"))))

(deftest test-add-milliseconds
  (is (= (st/add-milliseconds (st/datetime 2014 1 1) 42)
         (st/datetime 2014 1 1 0 0 0 42)))
  (is (thrown? AssertionError (st/add-milliseconds (st/datetime) "42"))))

(deftest test-=
  (is (st/= (st/datetime 2014 1 1)))
  (is (st/= (st/datetime 2014 1 1)
            (st/datetime 2014 1 1)))
  (is (st/= (st/datetime 2014 1 1)
            (st/datetime 2014 1 1)
            (st/datetime 2014 1 1)))
  (is (st/= (st/datetime 2014 1 1)
            (st/datetime 1388534400000)))
  (is (not (st/= (st/datetime 2014 1 1)
                 (st/datetime 2014 1 2)))))

(deftest test-not=
  (is (not (st/not= (st/datetime 2014 1 1))))
  (is (not (st/not= (st/datetime 2014 1 1)
                    (st/datetime 2014 1 1))))
  (is (not (st/not= (st/datetime 2014 1 1)
                    (st/datetime 2014 1 1)
                    (st/datetime 2014 1 1))))
  (is (not (st/not= (st/datetime 2014 1 1)
                    (st/datetime 1388534400000))))
  (is (st/not= (st/datetime 2014 1 1)
               (st/datetime 2014 1 2))))

(deftest test-<
  (is (st/< (st/datetime 2014 1 1)))
  (is (st/< (st/datetime 2014 1 1)
            (st/datetime 2014 1 2)))
  (is (not (st/< (st/datetime 2014 1 1)
                 (st/datetime 2014 1 1))))
  (is (st/< (st/datetime 2014 1 1)
            (st/datetime 2014 1 2)
            (st/datetime 2014 1 3)))
  (is (not (st/< (st/datetime 2014 1 3)
                 (st/datetime 2014 1 2)
                 (st/datetime 2014 1 1)))))

(deftest test->
  (is (st/> (st/datetime 2014 1 1)))
  (is (st/> (st/datetime 2014 1 2)
            (st/datetime 2014 1 1)))
  (is (not (st/> (st/datetime 2014 1 1)
                 (st/datetime 2014 1 1))))
  (is (st/> (st/datetime 2014 1 3)
            (st/datetime 2014 1 2)
            (st/datetime 2014 1 1)))
  (is (not (st/> (st/datetime 2014 1 1)
                 (st/datetime 2014 1 2)
                 (st/datetime 2014 1 3)))))

(deftest test-<=
  (is (st/<= (st/datetime 2014 1 1)))
  (is (st/<= (st/datetime 2014 1 1)
             (st/datetime 2014 1 2)))
  (is (st/<= (st/datetime 2014 1 1)
             (st/datetime 2014 1 1)))
  (is (st/<= (st/datetime 2014 1 1)
             (st/datetime 2014 1 2)
             (st/datetime 2014 1 2)
             (st/datetime 2014 1 3)))
  (is (not (st/<= (st/datetime 2014 1 3)
                  (st/datetime 2014 1 2)
                  (st/datetime 2014 1 1)))))

(deftest test->=
  (is (st/>= (st/datetime 2014 1 1)))
  (is (st/>= (st/datetime 2014 1 2)
             (st/datetime 2014 1 1)))
  (is (st/>= (st/datetime 2014 1 1)
             (st/datetime 2014 1 1)))
  (is (st/>= (st/datetime 2014 1 3)
             (st/datetime 2014 1 2)
             (st/datetime 2014 1 2)
             (st/datetime 2014 1 1)))
  (is (not (st/>= (st/datetime 2014 1 1)
                  (st/datetime 2014 1 2)
                  (st/datetime 2014 1 3)))))

(deftest test-duration
  (is (= (st/duration (st/timespan -100))
         (st/timespan 100)))
  (is (= (st/duration (st/timespan 100))
         (st/timespan 100))))

;; ****************************************************************************

(deftest test-with-precision
  (is (= (st/with-precision #{:year :month :day :hour :minute :second :millisecond}
           (st/datetime 2014 1 2 12 34 56 789))
         (st/datetime 2014 1 2 12 34 56 789)))
  (is (= (st/with-precision #{:year :day :minute :millisecond}
           (st/datetime 2014 1 2 12 34 56 789))
         (st/datetime 2014 1 2 0 34 0 789)))
  (is (= (st/with-precision #{:year :month :hour :second}
           (st/datetime 2014 1 2 12 34 56 789))
         (st/datetime 2014 1 1 12 0 56 0)))
  (is (= (st/with-precision #{:year}
           (st/datetime 2014 1 2 12 34 56 789))
         (st/datetime 2014 1 1)))
  (is (thrown? IllegalArgumentException
               (st/with-precision #{:month} (st/datetime 2014 1 2 12 34 56 789))))
  (is (thrown? IllegalArgumentException
               (st/with-precision #{:month :day :hour :minute :second :millisecond} (st/datetime 2014 1 2 12 34 56 789))))
  (is (= (st/with-precision #{:day :hour :minute :second :millisecond}
           (st/datetime 2014 1 2 12 34 56 789))
         (st/timespan 2 12 34 56 789)))
  (is (= (st/with-precision #{:millisecond}
           (st/datetime 2014 1 2 12 34 56 789))
         (st/timespan 789)))
  (is (= (st/with-precision #{:day :hour :minute :second :millisecond}
           (st/timespan 2 12 34 56 789))
         (st/timespan 2 12 34 56 789)))
  (is (= (st/with-precision #{:hour :second}
           (st/timespan 2 12 34 56 789))
         (st/timespan 0 12 0 56 0))))

(deftest test-datetime->date
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->epoch (st/datetime->date datetime))
           1388620800000)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->epoch (st/datetime->date datetime))
           1388620800000)))
  (is (thrown? AssertionError (st/datetime->date "42"))))

(deftest test-datetime->day-of-week
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->day-of-week datetime)
           4)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->day-of-week datetime)
           4)))
  (is (thrown? AssertionError (st/datetime->day-of-week "42"))))

(deftest test-datetime->day-of-year
  (let [datetime (st/datetime 2014 1 2 3 4 5 6)]
    (is (= (st/datetime->day-of-year datetime)
           2)))
  (let [datetime (st/datetime 1388631845006)]
    (is (= (st/datetime->day-of-year datetime)
           2)))
  (is (thrown? AssertionError (st/datetime->day-of-year "42"))))

(deftest test-datetime->time-of-day
  (let [datetime (st/datetime 2014 1 2 12 34 56)]
    (is (= (st/datetime->time-of-day datetime)
           (st/timespan 12 34 56))))
  (let [datetime (st/datetime 2014 1 2 12 34 56 789)]
    (is (= (st/datetime->time-of-day datetime)
           (st/timespan 0 12 34 56 789))))
  (is (thrown? AssertionError (st/datetime->time-of-day "42"))))

(deftest test-days-in-month
  (is (= (st/days-in-month 2014 1) 31))
  (is (= (st/days-in-month 2014 2) 28))
  (is (= (st/days-in-month 2012 2) 29))
  (is (thrown? AssertionError (st/days-in-month "2014" "1"))))

(deftest test-now
  (is (st/datetime? (st/now)))
  (is (< 0 (st/datetime->epoch (st/now)))))

(deftest test-today
  (is (st/datetime? (st/today)))
  (is (= 0 (st/datetime->hour (st/today))))
  (is (= 0 (st/datetime->minute (st/today))))
  (is (= 0 (st/datetime->second (st/today))))
  (is (= 0 (st/datetime->millisecond (st/today)))))

(deftest test-utc-now
  (is (st/datetime? (st/utc-now)))
  (is (< 0 (st/datetime->epoch (st/utc-now)))))

(deftest test-range
  (testing "no end or step"
    (is (= (take 4 (st/range (st/datetime 2014 1 1)))
           [(st/datetime 2014 1 1)
            (st/datetime 2014 1 2)
            (st/datetime 2014 1 3)
            (st/datetime 2014 1 4)])))
  (testing "with end, no step"
    (is (= (st/range (st/datetime 2014 1 1) (st/datetime 2014 1 4))
           [(st/datetime 2014 1 1)
            (st/datetime 2014 1 2)
            (st/datetime 2014 1 3)])))
  (testing "start, end, and step"
    (is (= (st/range (st/datetime 2014 1 1) (st/datetime 2014 1 4) 2)
           [(st/datetime 2014 1 1)
            (st/datetime 2014 1 3)])))
  (testing "empty"
    (is (= (st/range (st/datetime 2014 1 1) (st/datetime 2014 1 1))
           [])))
  (testing "empty, reverse"
    (is (= (st/range (st/datetime 2014 1 4) (st/datetime 2014 1 1))
           [])))
  (testing "empty, negative"
    (is (= (st/range (st/datetime 2014 1 1) (st/datetime 2014 1 1) -1)
           [])))
  (testing "negative step"
    (is (= (st/range (st/datetime 2014 1 1) (st/datetime 2013 12 25) -2)
           [(st/datetime 2014 1 1)
            (st/datetime 2013 12 30)
            (st/datetime 2013 12 28)
            (st/datetime 2013 12 26)])))
  (testing "advance hours"
    (is (= (st/range (st/datetime 2014 1 1 0 0 0) (st/datetime 2014 1 1 6 0 0) (st/hours->timespan 2))
           [(st/datetime 2014 1 1 0 0 0)
            (st/datetime 2014 1 1 2 0 0)
            (st/datetime 2014 1 1 4 0 0)])))
  (testing "user fn"
    (is (= (st/range (st/datetime 2013 11 1) (st/datetime 2014 6 1)
                     (fn [dt]
                       (let [dt (st/add-months dt 1)
                             y (st/datetime->year dt)
                             m (st/datetime->month dt)
                             d (st/days-in-month dt)]
                       (st/datetime y m d))))
           [(st/datetime 2013 11 1)
            (st/datetime 2013 12 31)
            (st/datetime 2014 1 31)
            (st/datetime 2014 2 28)
            (st/datetime 2014 3 31)
            (st/datetime 2014 4 30)
            (st/datetime 2014 5 31)])))
  (testing "daylight savings time with no timezone - add days"
    (is (= (st/range (st/datetime 2014 3 8) (st/datetime 2014 3 12))
           [(st/datetime 2014 3 8)
            (st/datetime 2014 3 9)
            (st/datetime 2014 3 10)
            (st/datetime 2014 3 11)])))
  (testing "daylight savings time with no timezone - add 24 hours"
    (is (= (st/range (st/datetime 2014 3 8) (st/datetime 2014 3 12) (st/days->timespan 1))
           [(st/datetime 2014 3 8)
            (st/datetime 2014 3 9)
            (st/datetime 2014 3 10)
            (st/datetime 2014 3 11)]))))

(deftest test-total-months
  (is (= 1 (st/total-months (st/datetime 2014 1 1) (st/datetime 2014 2 1))))
  (is (= 865/868 (st/total-months (st/datetime 2014 1 31) (st/datetime 2014 2 28))))
  (is (= 910/868 (st/total-months (st/datetime 2014 1 15) (st/datetime 2014 2 15))))
  (is (= 12 (st/total-months (st/datetime 2013 1 1) (st/datetime 2014 1 1))))
  (is (= 10388/868 (st/total-months (st/datetime 2014 1 1) (st/datetime 2014 12 31))))
  (is (= 12 (st/total-months (st/datetime 2012 1 1) (st/datetime 2013 1 1)))))

;; ****************************************************************************

(defn safe-parse [value format]
  (try
    (st/parse value format)
    (catch Exception z (.getMessage z))))

(defn round-trip-formatter [value format]
  (let [formatted (st/format value format)
        parsed (safe-parse formatted format)]
    (if-let [round-trip (-> format st/formatters :round-trip)]
      (let [expected (round-trip value)]
        [expected parsed])
      [parsed parsed])))

(deftest test-formatters
  (let [value (st/datetime 2014 1 2 12 34 56 789)]
    (doseq [format (keys simple-time.core/formatters)]
      (let [[expected parsed] (round-trip-formatter value format)]
        (is (= expected parsed) (str "Formatter " format))))))
