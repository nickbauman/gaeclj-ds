(ns gaeclj.test.valid
  "Demonstrates validation using ad-hoc functions from an arbitrary namespace"
  (:require [clojure.test :refer [deftest is run-tests testing use-fixtures]]
            [clj-uuid :as uuid]
            [clj-time.core :as t]
            [gaeclj.ds :refer [defentity save!]]
            [gaeclj.test.fixtures :as fixtures]))

(use-fixtures :once fixtures/setup-local-service-test-helper)

(defentity CostStrategy
  [:uuid                 (requiring-resolve `gaeclj.valid/valid-uuid-str?)
   :recurring?           (requiring-resolve `gaeclj.valid/bool?)
   :create-date          (requiring-resolve `gaeclj.valid/long?)
   :cost-uuid            (requiring-resolve `gaeclj.valid/valid-uuid-str?)
   :strategy-description (requiring-resolve `gaeclj.valid/string-or-nil?)
   :ordered-member-uuids (requiring-resolve `gaeclj.valid/repeated-uuid?)
   :ordered-amounts      (requiring-resolve `gaeclj.valid/repeated-longs?)])

(deftest test-model-CostStrategy
  (testing "Save CostStratety success"
    (let [ent (create-CostStrategy "8e5625f8-60ec-11ea-a1ec-a45e60d5bfab"
                                   false
                                   (.getMillis (t/date-time 1999 12 31))
                                   (str (uuid/v1))
                                   "even distribution"
                                   [(str (uuid/v1)) (str (uuid/v1))]
                                   [1234 1234])
          ; save it
          saved-ent (save! ent)
          ; read it back from the db
          read-ent (query-CostStrategy [:uuid = (:uuid saved-ent)])]
      (is (not (nil? read-ent))))))

(deftest test-model-CostStrategy-invalid
  (testing "Save CostStratety invalid"
    (is (thrown? RuntimeException (create-CostStrategy "invalid"
                                                       true
                                                       (t/date-time 1999 12 31)
                                                       (str (uuid/v1))
                                                       "even distribution"
                                                       [(str (uuid/v1)) (str (uuid/v1))]
                                                       [1/2 1/2])))))

(comment
  (run-tests))
