(ns user
  "for local development"
 (:require [portal.api :as portal]))

(def p 
  "launch a portal instance when the this namespace is evaluated" 
  (portal/open {:launcher :vs-code}))

(add-tap #'portal/submit)

(comment 
  p)

