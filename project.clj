(def appengine-version "1.9.78")

(defproject gaeclj-ds "0.1.3"
  :description "A DSL to support querying Google App Engine's Datastore"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :min-lein-version "2.6.0"
  :url "https://github.com/nickbauman/gaeclj-ds"
  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.logging "0.6.0"]
                 [org.clojure/data.json "1.0.0"]
                 [clj-time "0.15.2"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [com.google.guava/guava "23.0"]
                 [com.google.appengine/appengine-api-1.0-sdk ~appengine-version]
                 [org.apache.httpcomponents/httpclient "4.5.11"]
                 [com.google.api-client/google-api-client-appengine "1.30.8"
                  :exclusions [com.google.guava/guava-jdk5]]
                 [com.google.oauth-client/google-oauth-client-appengine "1.30.5"
                  :exclusions [com.google.guava/guava-jdk5]]       
                 [com.google.http-client/google-http-client-appengine "1.34.2"
                  :exclusions [com.google.guava/guava-jdk5]]
                 [danlentz/clj-uuid "0.1.9"]]
  :java-source-paths ["src-java"]
  :aot :all
  :profiles
  {:dev
   {:dependencies [[com.google.appengine/appengine-testing ~appengine-version]
                   [com.google.appengine/appengine-api-labs ~appengine-version]
                   [com.google.appengine/appengine-api-stubs ~appengine-version]
                   [com.google.appengine/appengine-tools-sdk ~appengine-version]]}})
