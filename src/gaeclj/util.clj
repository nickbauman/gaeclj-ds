(ns gaeclj.util
  (:require [clojure.tools.logging :as log]))

(defmacro try-with-default 
  "Tries to do 'forms'. If forms throws an exception, does default."
  [default & forms] 
  `(try
     (do  ~@forms)
     (catch Exception ~'ex
       (do
         (log/warn (str "Failed with " (type ~'ex) ": " (.getMessage ~'ex) ". Defaulting to " ~default))
         ~default))))

(defn in 
  "Returns true if scalar value is found in sequence, otherwise returns nil"
  [scalar sequence] 
  (some #(= scalar %) sequence))
