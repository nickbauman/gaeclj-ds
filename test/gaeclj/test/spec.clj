(ns gaeclj.test.spec
  "Demonstrate the use of spec for validating properties on entities"
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clj-uuid :as uuid]
            [clj-time.core :as t]
            [gaeclj.ds :refer [defentity with-transaction with-xg-transaction gae-key save! delete! !=]]
            [gaeclj.test.fixtures :as fixtures]))

(use-fixtures :once fixtures/setup-local-service-test-helper)

(s/def ::uuid-string? #(parse-uuid %))
(defn validate-uuid [might-be-uuid]
  (s/valid? ::uuid-string? might-be-uuid))

(defentity SomeSpecValidatedEntity
  [my-uuid] 
  [:my-uuid                 (requiring-resolve `gaeclj.test.spec/validate-uuid)])

(deftest test-some-spec-validated-entity
  (testing "check for valid uuid"
    (let [ent (create-SomeSpecValidatedEntity
               "8e5625f8-60ec-11ea-a1ec-a45e60d5bfab")
          ; save it
          saved-ent (save! ent)
          ; read it back from the db
          read-ent (query-SomeSpecValidatedEntity [:my-uuid = (:my-uuid saved-ent)])]
      (prn :read-ent read-ent)
      (is (not (nil? read-ent))))))

(deftest test-some-spec-validated-entity-invalid
  (testing "Save CostStratety invalid"
    (is (thrown? RuntimeException (create-SomeSpecValidatedEntity "invalid")))))
