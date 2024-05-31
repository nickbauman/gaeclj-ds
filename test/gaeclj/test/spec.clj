
(ns gaeclj.test.spec
  "Demonstrate the use of spec for validating properties on entities"
  (:require [clojure.test :refer [deftest is run-tests testing use-fixtures]]
            [clojure.spec.alpha :as s]
            [gaeclj.ds :refer [defentity    save!]]
            [gaeclj.test.fixtures :as fixtures]))

(use-fixtures :once fixtures/setup-local-service-test-helper)

(declare SomeSpecValidatedEntity)
(declare create-SomeSpecValidatedEntity)
(declare query-SomeSpecValidatedEntity)

(s/def ::uuid-string? #(parse-uuid %))
(defn validate-uuid
  "Validate stringified uuid"
  [might-be-uuid]
  (s/valid? ::uuid-string? might-be-uuid))

(defentity SomeSpecValidatedEntity
  [:my-uuid (requiring-resolve `gaeclj.test.spec/validate-uuid)])

(deftest test-some-spec-validated-entity
  (testing "check for valid uuid"
    (let [ent (create-SomeSpecValidatedEntity
               "8e5625f8-60ec-11ea-a1ec-a45e60d5bfab")
          ; save it
          saved-ent (save! ent)
          ; read it back from the db

          read-ent (query-SomeSpecValidatedEntity [:my-uuid = (:my-uuid saved-ent)])]
      (is (not (nil? read-ent))))))

(deftest test-some-spec-validated-entity-invalid
  (testing "Save CostStratety invalid"
    (is (thrown? RuntimeException (create-SomeSpecValidatedEntity "invalid")))))

(comment
  (run-tests))

(comment
  (tap> {:foo :foo}))
