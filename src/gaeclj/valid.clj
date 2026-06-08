(ns gaeclj.valid
  "functions that can be used as ad-hoc validators for entities"
  (:require [gaeclj.util :refer [try-with-default]]
            [clojure.tools.logging :as log]))

(def ^{:doc "this is a regex of a UUID"} 
  uuid-regex #"[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}")

(defn valid-uuid-str?
  "a valid UUID string"
  [x]
  (try-with-default false
                    (not (nil? (re-matches uuid-regex x)))))
(defn long?
  "a long value"
  [x]
  (try-with-default false (instance? Long x)))

(defn string-or-nil?
  "a string or nil"
  [x]
  (or (string? x) (nil? x)))

(defn repeated-uuid?
  "a sequence of UUID strings"
  [x]
  (and (seq x) (every? #(not (nil? %)) (map valid-uuid-str? x))))

(defn repeated-longs?
  "a sequence of longs"
  [x]
  (and (seq x) (every? true? (map long? x))))

(defn bool?
  "a boolean value"
  [x]
  (boolean? x))

(defn repeated-floats?
  "a sequence of floats"
  [x]
  (and (seq x) (every? true? (map #(or (float? %1) (ratio? %1)) x))))