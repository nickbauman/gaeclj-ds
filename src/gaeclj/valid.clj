(ns gaeclj.valid
  "functions that can be used as ad-hoc validators for entities"
  (:require [gaeclj.util :refer [try-with-default]]
            [clojure.tools.logging :as log]))

(def uuid-regex
  "A regex that matches a UUID"
  #"[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}")

(defn valid-uuid-str?
  "Returns true if x is a valid UUID string"
  [x]
  (try-with-default false
                    (not (nil? (re-matches uuid-regex x)))))

(defn long?
  "Returns true if x is a Long"
  [x]
  (try-with-default false (instance? Long x)))

(defn string-or-nil?
  "Returns true if x is a string or nil"
  [x]
  (or (string? x) (nil? x)))

(defn repeated-uuid?
  "Returns true if x is a sequence of valid UUID strings"
  [x]
  (and (seq x) (every? #(not (nil? %)) (map valid-uuid-str? x))))

(defn repeated-longs?
  "Returns true if x is a sequence of Longs"
  [x]
  (and (seq x) (every? true? (map long? x))))

(defn bool?
  "Returns true if x is a boolean"
  [x]
  (boolean? x))

(defn repeated-floats?
  "Returns true if x is a sequence of floats"
  [x]
  (and (seq x) (every? true? (map #(or (float? %1) (ratio? %1)) x))))