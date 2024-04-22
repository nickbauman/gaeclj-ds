(ns gaeclj.test.valid
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clj-uuid :as uuid]
            [clj-time.core :as t]
            [gaeclj.ds :refer [defentity with-transaction with-xg-transaction gae-key save! delete! !=]]
            [gaeclj.test.fixtures :as fixtures]))

(use-fixtures :once fixtures/setup-local-service-test-helper)

(defentity SomeSpecValidatedEntity
  [uuid]
  [:uuid                 #(s/valid? uuid? %)])

(deftest test-some-spec-validated-entity
  (testing "check for valid uuid"
    (let [ent (create-SomeSpecValidatedEntity
               "8e5625f8-60ec-11ea-a1ec-a45e60d5bfab")
          ; save it
          saved-ent (save! ent)
          ; read it back from the db
          read-ent (query-CostStrategy [:uuid = (:uuid saved-ent)])]
      (is (not (nil? read-ent))))))

(deftest test-some-spec-validated-entity-invalid
  (testing "Save CostStratety invalid"
    (is (thrown? RuntimeException (create-CostStrategy "invalid")))))
