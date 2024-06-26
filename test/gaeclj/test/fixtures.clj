(ns gaeclj.test.fixtures
  "Google Cloud API test doubles for GAE"
  (:import [com.google.appengine.tools.development.testing
            LocalServiceTestConfig
            LocalServiceTestHelper
            LocalDatastoreServiceTestConfig])
  (:require [clojure.java.io :as io]))

(defn- datastore-config []
  (doto
   (LocalDatastoreServiceTestConfig.)
    (.setApplyAllHighRepJobPolicy)
    (.setNoStorage true)))

(defn- create-local-test-helper []
  (LocalServiceTestHelper. (into-array LocalServiceTestConfig [(datastore-config)])))

(defn setup-local-service-test-helper [f]
  (let [helper (create-local-test-helper)]
    (try
      (.setUp helper)
      (f)
      (finally
        (.tearDown helper)))))

