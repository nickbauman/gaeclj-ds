(def appengine-version "2.0.20")

(defproject gaeclj-ds "0.1.3.2"
  :description "A DSL to support querying Google App Engine's Datastore"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :min-lein-version "2.6.0"
  :url "https://github.com/nickbauman/gaeclj-ds"
  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.0.0"]
                 [org.clojure/data.json "2.4.0"]
                 [clj-time "0.15.2"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [com.google.guava/guava "31.1-jre"]
                 [com.google.appengine/appengine-api-1.0-sdk ~appengine-version]
                 [org.apache.httpcomponents/httpclient "4.5.14"]
                 [com.google.api-client/google-api-client-appengine "2.2.0"
                  :exclusions [com.google.guava/guava-jdk5]]
                 [com.google.oauth-client/google-oauth-client-appengine "1.34.1"
                  :exclusions [com.google.guava/guava-jdk5]]       
                 [com.google.http-client/google-http-client-appengine "1.43.3"
                  :exclusions [com.google.guava/guava-jdk5]]
                 [danlentz/clj-uuid "0.1.9"]]
  :java-source-paths ["src-java"]
  :aot :all
  :profiles
  {:dev
   {:dependencies [[com.google.appengine/appengine-testing ~appengine-version]
                   ;[com.google.appengine/appengine-api-labs ~appengine-version]
                   [com.google.appengine/appengine-api-stubs ~appengine-version]
                   [com.google.appengine/appengine-tools-sdk ~appengine-version]]}})
