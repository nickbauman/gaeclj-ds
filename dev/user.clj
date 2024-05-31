(ns user
  "for local development"
 (:require [portal.api :as portal]))

(def p 
  "launch a portal instance when the this namespace is evaluated" 
  (portal/open {:launcher :vs-code}))

(add-tap #'portal/submit)


(comment
  
  (mapv first (partition 2 [:my-uuid (requiring-resolve `gaeclj.test.spec/validate-uuid)
                            :cost (requiring-resolve `gaeclj.test.spec/validate-uuid)])) 
  )

(comment 
  (every? #(isa? clojure.lang.Keyword (type %)) [:my-uuid :cost]))

  
(comment 
  
  (let [x `(repeat 5 (constantly true))]
    (eval x)
    )


