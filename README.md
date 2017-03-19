# gaeclj-ds

A Clojure query DSL for Google App Engine. Inspired by the Python NDB library (with an emphasis on Clojure's more functional idiom.) Queries return a lazy sequence.

## Prerequisites

You will need the [Java SDK for AppEngine and its dependencies](https://cloud.google.com/appengine/docs/standard/java/download). 

## Using

Leiningen Clojars dependency:

```
[gaeclj-ds "0.1.0"]
```


## Example usages

```clojure
(defentity BasicEntity [content saved-time])

(defentity AnotherEntity [content saved-time int-value])

(let [entity (save! (create-AnotherEntity "Some content woo" (t/date-time 1980 3 5) 6))
      entity2 (save! (create-AnotherEntity "Other content" (t/date-time 1984 10 12) 91))
      entity3 (save! (create-AnotherEntity "More interesting content" (t/date-time 1984 10 12) 17))
      root-entity (save! (create-BasicEntity "basic entity content" (t/date-time 2015 6 8)))
      child-entity1 (save! (create-AnotherEntity "child one content" (t/date-time 2016 12 10) 33) (gae-key root-entity))
      child-entity2 (save! (create-AnotherEntity "child two content" (t/date-time 2016 12 10) 44) (gae-key root-entity))]   
                                        ; query all
  (query-AnotherEntity [])
                                        ; equality
  (query-AnotherEntity [:content = "Some content woo"])
  (query-AnotherEntity [:content = "Blearg not found"])
                                        ; not equal
  (query-AnotherEntity [:content != "Not found"])
  (query-AnotherEntity [:content != "Other content"])
                                        ; greater-than and less-than
  (query-AnotherEntity [:int-value < 7])
  (query-AnotherEntity [:int-value < 5])
                                        ; time: before and after
  (query-AnotherEntity [:saved-time > (.toDate (t/date-time 1979 3 5))])
  (query-AnotherEntity [:saved-time < (.toDate (t/date-time 1979 3 5))])
                                        ; "and" compound queries
  (query-AnotherEntity [:and [:content = "Some content woo"] [:int-value > 5]])
  (query-AnotherEntity [:and [:int-value > 5] [:int-value <= 17]])
                                        ; "or" compound queries
  (query-AnotherEntity [:or [:content = "Some content woo"] [:int-value < 5]])
  (query-AnotherEntity [:or [:content = "Some content woo"] [:int-value > 5]])
                                        ; compound queries with nested compound predicates
  (query-AnotherEntity [:or [:content = "Other content"] 
                        [:and [:saved-time < (.toDate (t/date-time 1983 3 5))] [:int-value = 6]]])
                                        ; keys-only support
  (query-AnotherEntity [:int-value < 7] [:keys-only true])
                                        ; order-by support
  (query-AnotherEntity [:int-value > 0] [:order-by :int-value :desc])
                                        ; keys only and order-by support together 
  (query-AnotherEntity [:int-value > 0] [:keys-only true :order-by :int-value :desc])
                                        ; support multiple sort orders (with keys-only, too)
  (query-AnotherEntity [:saved-time > 0] [:order-by :saved-time :desc :int-value :asc :keys-only true])
                                        ; parents can find their children
  (query-AnotherEntity [] [:ancestor-key (gae-key root-entity)])
                                        ; ancestors that work with predicates
  (query-AnotherEntity [:int-value > 33] [:ancestor-key (gae-key root-entity)])
                                        ; ancestors that work with keys-only support
  (query-AnotherEntity [] [:keys-only true :ancestor-key (gae-key root-entity)])
  (query-AnotherEntity [] [:ancestor-key (gae-key root-entity) :keys-only true])
                                        ; ancestors that work with order-by
  (query-AnotherEntity [] [:ancestor-key (gae-key root-entity) :order-by :int-value :desc])
                                        ; transactions
  (with-transaction
     (save! (create-AnotherEntity "Content information" (t/date-time 1984 10 12) 201)))
                                        ; Cross-group transactions
  (with-xg-transaction
     (save! (create-AnotherEntity "Content information" (t/date-time 1984 10 12) 6001))
     (save! (create-AnotherEntity "More content information" (t/date-time 1984 10 12) 6002))))
```

## Runing the automated tests

Through leiningen

    > lein test

## License

Copyright Peter Schwarz and Nick Bauman © 2016, 2017

Employing the Eclipse Public License (expressed in LICENSE file)
