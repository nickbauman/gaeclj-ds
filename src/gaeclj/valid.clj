(ns gaeclj.valid
  (:require [gaeclj.util :refer [try-with-default]]
            [clojure.tools.logging :as log]))

(def uuid-regex #"[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}")

(defn valid-uuid?
  [x]
  (try-with-default false
                    (not (nil? (re-matches uuid-regex x)))))

(defn long?
  [x]
  (try-with-default false (instance? Long x)))

(defn string-or-nil?
  [x]
  (or (string? x) (nil? x)))

(defn repeated-uuid?
  [x]
  (and (seq x) (every? #(not (nil? %)) (map valid-uuid? x))))

(defn repeated-longs?
  [x]
  (and (seq x) (every? true? (map long? x))))

(defn repeated-floats?
  [x]
  (and (seq x) (every? true? (map #(or (float? %1) (ratio? %1)) x))))