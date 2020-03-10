(ns gaeclj.test.valid
  (:require [clojure.test :refer :all]
            [clj-uuid :as uuid]
            [clj-time.core :as t]
            [gaeclj.ds :refer [defentity with-transaction with-xg-transaction gae-key save! delete! !=]]
            [gaeclj.test.fixtures :as fixtures]))

(use-fixtures :once fixtures/setup-local-service-test-helper)

(defentity CostStrategy
           [uuid
            recurring?
            create-date
            cost-uuid
            strategy-description
            ordered-member-uuids
            ordered-percentages]
           [:uuid                 gaeclj.valid/valid-uuid?
            :recurring?           gaeclj.valid/bool?
            :create-date          gaeclj.valid/long?
            :cost-uuid            gaeclj.valid/valid-uuid?
            :strategy-description gaeclj.valid/string-or-nil?
            :ordered-member-uuids gaeclj.valid/repeated-uuid?
            :ordered-amounts      gaeclj.valid/repeated-longs?])

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
