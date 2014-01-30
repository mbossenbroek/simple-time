(ns simple-time.interop-test
  (:use clojure.test)
  (:require [simple-time.interop :as interop]
            [simple-time.core :as st])
  (:import [org.joda.time LocalDateTime Duration]))

(deftest test-datetime->LocalDateTime
  (is (instance? LocalDateTime (interop/datetime->LocalDateTime (st/datetime)))))

(deftest test-timespan->Duration
  (is (instance? Duration (interop/timespan->Duration (st/timespan 100)))))
