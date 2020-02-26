(ns gaeclj.util)

(defn in [scalar sequence]
  "Returns true if scalar value is found in sequence, otherwise returns nil"
  (some #(= scalar %) sequence))
