# gaeclj-ds

A Clojure query DSL for Google App Engine for Java 11 & 17. Inspired by the Python NDB library (with an emphasis on Clojure's more functional idiom.) Queries return a lazy sequence.

[![Clojars Project](https://img.shields.io/clojars/v/gaeclj-ds.svg)](https://clojars.org/gaeclj-ds)

## Coverage

|    Namespace | % Forms | % Lines |
|--------------|---------|---------|
|    gaeclj.ds |   96.40 |   94.87 |
|  gaeclj.util |  100.00 |  100.00 |
| gaeclj.valid |   43.51 |   84.21 |
|    ALL FILES |   93.33 |   94.12 |

## Prerequisites

You will need the [Java SDK for AppEngine and its dependencies](https://cloud.google.com/appengine/docs/standard/java-gen2/runtime).

## Using

Leiningen Clojars dependency:

```
[gaeclj-ds "0.1.3.3"]
```


## Example usages

```clojure
(defentity BasicEntity [content saved-time repeated-value])

(defentity AnotherEntity [content saved-time int-value])

(let [entity (save! (create-AnotherEntity "Some content woo" (t/date-time 1980 3 5) 6))
      entity2 (save! (create-AnotherEntity "Other content" (t/date-time 1984 10 12) 91))
      entity3 (save! (create-AnotherEntity "More interesting content" (t/date-time 1984 10 12) 17))
                                        ; repeated properties
      root-entity (save! (create-BasicEntity "basic entity content" (t/date-time 2015 6 8) [1 2 3])) 
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
                                        ; Deleting
  (delete! entity) ; or (delete! {:key (:key entity)})
   (delete! entity2)
   (delete! entity3)
   (delete! child-entity1)
   (delete! child-entity2)
   (delete! root-entity)
```

## Validation on create

Optionally, you can declare rules that are applied to each property before the entity is created.

```clojure
(defentity CostStrategy
           [uuid
            create-date
            cost-uuid
            strategy-description
            ordered-member-uuids
            ordered-percentages]
           [:uuid                 gaeclj.valid/valid-uuid-str?
            :create-date          gaeclj.valid/long?
            :cost-uuid            gaeclj.valid/valid-uuid-str?
            :strategy-description gaeclj.valid/string-or-nil?
            :ordered-member-uuids gaeclj.valid/repeated-uuid?
            :ordered-amounts      gaeclj.valid/repeated-longs?])
```

When creating a new `CostStrategy` the rules are applied.

```clojure
(create-CostStrategy "invalid uuid string"
                      (.getMillis (t/date-time 1999 12 31))
                      (str (uuid/v1))
                      "even distribution"
                      [(str (uuid/v1)) (str (uuid/v1))]
                      [1/2 1/2]) ; ratios are not longs, so also invalid
```

Results in an `RuntimeException` thrown.

```text
java.lang.RuntimeException: (create-CostStrategy ...) failed validation for props :uuid, :ordered-amounts
 at gaeclj.test.valid$create_CostStrategy.invokeStatic (valid.clj:10)
    gaeclj.test.valid$create_CostStrategy.invoke (valid.clj:10)
    gaeclj.test.valid$fn__1961.invokeStatic (valid.clj:26)
    gaeclj.test.valid/fn (valid.clj:24)
    clojure.test$test_var$fn__9737.invoke (test.clj:717)
    clojure.test$test_var.invokeStatic (test.clj:717)
    clojure.test$test_var.invoke (test.clj:708)
```
## Runing the automated tests

Through leiningen

```shell
lein test
```

## License

Copyright Nick Bauman and Peter Schwarz © 2016-2024

Employing the Eclipse Public License (expressed in LICENSE file)
