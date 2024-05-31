(ns gaeclj.ds
  "Implementation of the core Clojure Datastore DSL"
  (:require [clj-time.coerce :as c]
            [clojure.string :refer [join]]
            [clojure.tools.logging :as log]
            [gaeclj.util :as u])
  (:import [com.google.appengine.api.datastore
            DatastoreServiceFactory
            DatastoreService
            Entity
            EntityNotFoundException
            FetchOptions$Builder
            KeyFactory
            Key
            Query
            Query$SortDirection
            Query$CompositeFilter
            Query$CompositeFilterOperator
            Query$FilterPredicate
            Query$FilterOperator
            TransactionOptions$Builder]))

(defprotocol NdbEntity
  (save! [this] [this parent-key] "Saves the entity with its parent key.")
  (delete! [this] "Deletes the entity")
  (gae-key [this] "Produces Java SDK Appengine key from Clojure NdbEntity information"))

(defprotocol ToNdbValue
  (->prop [v] "Converts the value to a ndb appropriate value"))

(defprotocol FromNdbValue
  (<-prop [v] "Converts the value from a ndb appropriate value"))

(extend-protocol ToNdbValue
  Key
  (->prop [k] k)

  String
  (->prop [s] s)

  Number
  (->prop [n] n)

  java.util.Date
  (->prop [d] d)

  org.joda.time.DateTime
  (->prop [d] (c/to-date d))

  ; support for repeated properties
  java.util.List
  (->prop [d] d)

  java.lang.Boolean
  (->prop [d] d))

(extend-protocol FromNdbValue
  Key
  (<-prop [k] k)

  String
  (<-prop [s] s)

  Number
  (<-prop [n] n)

  java.util.Date
  (<-prop [d] (c/from-date d))

  ; support for repeated properties
  java.util.List
  (<-prop [d] d)

  java.lang.Boolean
  (<-prop [d] d))

(defn make-key
  "Creates an instance of com.google.appengine.api.datastore.Key"
  ([kind value]
   (KeyFactory/createKey (name kind) value))
  ([kind parent value]
   (KeyFactory/createKey parent (name kind) value)))

(defn check-key
  "Checks whether we indeed are working with a key"
  [parent-key]
  (when parent-key
    (if (isa? (type parent-key) Key)
      parent-key
      (throw (RuntimeException. (str "parent key " parent-key " not an instance of " Key))))))

(defn save-entity
  "Saves the entity with all the trimmings"
  ([entity-type entity]
   (save-entity entity-type entity nil))
  ([entity-type entity parent-key]
   (log/debugf "Saving %s: %s" entity-type (pr-str entity))
   (let [datastore (DatastoreServiceFactory/getDatastoreService)
         gae-parent-key (check-key parent-key)
         gae-ent (if (:key entity)
                   (if gae-parent-key
                     (Entity. (name entity-type) (:key entity) gae-parent-key)
                     (Entity. (name entity-type) (:key entity)))
                   (if gae-parent-key
                     (Entity. (name entity-type) gae-parent-key)
                     (Entity. (name entity-type))))]
     (doseq [field (keys entity)]
       (.setProperty gae-ent (name field) (->prop (field entity))))
     (try
       (.put datastore gae-ent)
       (catch Exception e
         (log/error e (str "Unable to save " (pr-str entity)))
         (throw e)))
     (if (:key entity)
       entity
       (assoc entity :key (.. gae-ent getKey getId))))))

(defn- gae-entity->map [gae-entity]
  (let [gae-key (.getKey gae-entity)
        hm (.getProperties gae-entity)
        gae-map (apply merge (map #(hash-map (keyword %) (<-prop (get hm %))) (keys hm)))]
    (assoc gae-map :key (if (.getName gae-key)
                          (.getName gae-key)
                          (.getId gae-key)))))

(defn get-entity
  "Takes an entity kind and a datastore key and attempts to retrieve it, if possible"
  [entity-kind entity-key]
  (let [datastore (DatastoreServiceFactory/getDatastoreService)
        result (try
                 (gae-entity->map (.get datastore (make-key entity-kind entity-key)))
                 (catch EntityNotFoundException _
                   nil))]
    (log/debugf "Getting %s:%s: found %s" entity-kind entity-key (pr-str result))
    result))

(defn as-gae-key
  "Creates corresponding datastore key for an entity "
  [entity-kind entity-key]
  (make-key entity-kind entity-key))

(defn delete-entity
  "Deletes any number of keys of a certain kind"
  [entity-kind entity-key & more-keys]
  (let [datastore (DatastoreServiceFactory/getDatastoreService)]
    (.delete datastore (map #(make-key entity-kind %) (conj more-keys entity-key)))))

; Start DS Query support ;;;

(defn !=
  "Available to complete the operator-map logic. Reverse logic of the = function"
  ([x] (not x))
  ([x y] (not (clojure.lang.Util/equiv x y)))
  ([x y & more]
   (not (apply = x y more))))

(def operator-map
  "selects a logical operator for a query"
  {<  Query$FilterOperator/LESS_THAN
   >  Query$FilterOperator/GREATER_THAN
   =  Query$FilterOperator/EQUAL
   >= Query$FilterOperator/GREATER_THAN_OR_EQUAL
   <= Query$FilterOperator/LESS_THAN_OR_EQUAL
   != Query$FilterOperator/NOT_EQUAL})

(def sort-order-map
  "returns the API sort order for the query"
  {:desc Query$SortDirection/DESCENDING
   :asc  Query$SortDirection/ASCENDING
   nil   Query$SortDirection/ASCENDING})

(defn filter-map
  "Selects the api predicate type 'and' or 'or"
  [keyw jfilter-predicates]
  (if (= :or keyw)
    (Query$CompositeFilterOperator/or jfilter-predicates)
    (Query$CompositeFilterOperator/and jfilter-predicates)))

(defn add-sorts
  "Add sorting calls if they're present. Returns the query"
  [options query]
  (if-let [ordering (when (and (seq options) (not (= -1 (.indexOf options :order-by)))) (rest (subvec options (.indexOf options :order-by))))]
    (loop [[[order-prop direction] & more] (partition 2 ordering)]
      (if (and order-prop direction (not (= order-prop :keys-only)))
        (do (.addSort query (name order-prop) (get sort-order-map direction)) (recur more))
        query))
    query))

(defn get-option
  "Get an option from options, if exists"
  [options option?]
  (let [indexed-pairs (map-indexed vector options)]
    (when-let [[[index _]] (seq (filter #(= option? (second %)) indexed-pairs))]
      (get (vec options) (inc index)))))

(defn set-keys-only
  "Sets the query to a keys-only query and returns it"
  [options query]
  (if (get-option options :keys-only)
    (.setKeysOnly query)
    query))

(defn set-ancestor-key
  "Assigns the ancestor for this query, if it exists, and returns it. Otherwise throws."
  [options query]
  (if-let [parent-key (check-key (get-option options :ancestor-key))]
    (.setAncestor query parent-key)
    query))

(defn make-property-filter
  "Takes `pred-coll` consisting of vector of a property, an operator and
   a value for that property to build a filter"
  [pred-coll]
  (let [[property operator-fn query-value] pred-coll
        filter-operator (operator-map operator-fn)]
    (if filter-operator
      (Query$FilterPredicate. (name property) filter-operator query-value)
      (throw (RuntimeException. (str "operator " operator-fn " not found in operator-map " (keys operator-map)))))))

(declare compose-query-filter)

(defn compose-predicates
  "Figures out how to convert the query vector into valid native calls to Datastore"
  [preds-coll]
  (loop [jfilter-preds []
         preds preds-coll]
    (if (seq preds)
      (let [filter-fn (if (u/in (ffirst preds) [:or :and])
                        compose-query-filter
                        make-property-filter)]
        (recur (conj jfilter-preds (filter-fn (first preds)))
               (rest preds)))
      jfilter-preds)))

(defn compose-query-filter
  "Convert an expression like `[] [:ancestor-key (gae-key root-entity)]` into a native filter query"
  [preds-vec]
  (when (u/in (first preds-vec) [:and :or])
    (let [condition (first preds-vec)
          jfilter-predicates (compose-predicates (rest preds-vec))]
      (filter-map condition jfilter-predicates))))

(defn build-query
  "Build a query"
  [predicates options ent-kind filters]
  (->> (if (nil? filters) (make-property-filter predicates) filters)
       (.setFilter (Query. (name ent-kind)))
       (set-ancestor-key options)
       (set-keys-only options)
       (add-sorts options)))

(defn lazify-qiterable
  "Make results lazy"
  ([pq-iterable]
   (lazify-qiterable pq-iterable (.iterator pq-iterable)))
  ([pq-iterable i]
   (lazy-seq
    (when (.hasNext i)
      (cons (gae-entity->map (.next i)) (lazify-qiterable pq-iterable i))))))

(defn make-query
  "Takes a predicates vector, (nillable) options and an entity name"
  [predicates options ent-kind]
  (if (seq predicates)
    (build-query predicates options ent-kind (compose-query-filter predicates))
    (->> (Query. (name ent-kind))
         (set-ancestor-key options)
         (set-keys-only options)
         (add-sorts options))))

(defn query-entity
  "accepts a predicate such as: `[:int-value > 21000]`, 
   your entity symbol such as `MyEntityName` and any options 
   such as `[:keys-only true :order-by :int-value :desc]`"
  [predicates options ent-sym]
  (->> ent-sym
       (make-query predicates options)
       (.prepare (DatastoreServiceFactory/getDatastoreService))
       .asIterable
       lazify-qiterable
       seq))

(defmacro ds-operation-in-transaction
  "properly handle the transaction, returning the result of 
   executing `body`, handling rollbacks from any Throwable"
  [tx & body]
  `(try
     (let [body-result# (do ~@body)]
       (.commit ~tx)
       body-result#)
     (catch Throwable err#
       (do (.rollback ~tx)
           (throw err#)))))

(defmacro with-xg-transaction
  "Wrapper for Datastore cross-group transaction declaration. 
   You would choose this if you're operating on multiple entities
   that have different root entities. Check the docs for limits"
  [& body]
  `(let [tx# (.beginTransaction (DatastoreServiceFactory/getDatastoreService) (TransactionOptions$Builder/withXG true))]
     (ds-operation-in-transaction tx# ~@body)))

(defmacro with-transaction
  "Wrapper for Datastore transaction declaration. `body` is a entity operation such as `(save! ...)` "
  [& body]
  `(let [tx# (.beginTransaction (DatastoreServiceFactory/getDatastoreService))]
     (ds-operation-in-transaction tx# ~@body)))

(defn all-keywords?
  "whether we have just properties without any validation"
  [entity-fields]
  (every? #(isa? clojure.lang.Keyword (type %)) entity-fields))

(defn ident-fields-to-validators
  "Creates two vectors: one containing symbols made from field names, 
   the other containing validator functions. If you do not supply the
   validator functions, we put a `(constantly true)` for each field"
  [entity-fields] 
  (if (all-keywords? entity-fields)
    (let [fields (into [] (mapcat (fn [ent] [(symbol (name ent)) (constantly true)]) entity-fields))] 
      fields)
    (let [fields (->> (map first (partition 2 entity-fields))
                      (mapv #(symbol (name %))))]
      [fields (mapv second (partition 2 entity-fields))])))

; End DS Query support ;;;

(defmacro defentity
  "A valid `entity-name` is a noun in the system, like Automobile
   The `entity-fields` are the properties of that Automobile, like 
   the the number of tires or the maximum speed, The `validation` are 
   the functions that check whether you set the low level types 
   correctly on those properties such as the number of tires are 
   enforced to be an integer and the maximim speed is enforced to
   be a double. Obviously you can use anything to determine whether
   a property is correct, such as spec or even your own functions.

   Note validation is optional. When you don't supply validation for
   your properties they're set to whatever you want. Great for 
   migrating your schema at will. Datastore is schemaless, after all."

  [entity-name entity-fields]
  (when-not (or (all-keywords? entity-fields)
                (all-keywords? (mapv first (partition 2 entity-fields))))
    (throw (RuntimeException. "fields must be keywords or keywords followed by functions")))

  (let [x (ident-fields-to-validators entity-fields)
        [entity-fields validators] [(into [] (filter #(not (fn? %)) x)) (filter fn? x)]
        _ (prn entity-fields)
        _ (prn validators)
        ent-name entity-name
        sym (symbol ent-name)
        empty-ent (symbol (str 'empty- ent-name))
        creator (symbol (str '-> ent-name))]
    (prn "here 1")
    `(do
       (defrecord ~ent-name ~entity-fields
         NdbEntity
         (save! [this#] (save-entity '~sym this#))
         (save! [this# parent-key#] (save-entity '~sym this# parent-key#))
         (delete! [this#] (delete-entity '~sym (:key this#)))
         (gae-key [this#] (as-gae-key '~sym (:key this#))))

        (prn "here 2")
       
       (def ~empty-ent
         ~(conj (map (constantly nil) entity-fields) creator))

       (prn "here 3") 

       (defn ~(symbol (str 'create- ent-name)) ~entity-fields 
         (prn "hey 33")
         (if-let [val-rules# (seq '~validators)]
           (let [validation-fns# (map second (partition 2 (first val-rules#)))
                 values# ~entity-fields
                 validators-to-values# (partition 2 (interleave validation-fns# values#))
                 keys# (map first (partition 2 (first val-rules#)))
                 results# (map (fn [[f# v#]] ((eval f#) (eval v#))) validators-to-values#)
                 props-to-results# (partition 2 (interleave keys# results#))
                 invalid-props# (map first (filter (fn [[_# result#]] (false? result#)) props-to-results#))]
             (if (seq invalid-props#)
               (throw (RuntimeException. (str "(create-" (.getSimpleName ~ent-name) " ...) failed validation for props " (join ", " invalid-props#)))))))
         ~(conj (seq entity-fields) creator))
        
        (prn "here 4")

       (defn ~(symbol (str 'get- ent-name)) [key#]
         (if-let [result# (get-entity '~sym key#)]
           (merge ~empty-ent result#)))

       (defn ~(symbol (str 'query- ent-name)) [predicates# & options#]
         (if-let [results# (query-entity predicates# (first options#) '~sym)]
           (if (get-option (first options#) :keys-only)
             (map :key results#)
             (map #(merge ~empty-ent %) results#))))

       (defn ~(symbol (str 'delete- ent-name)) [key#]
         (delete-entity '~sym key#)))))

(comment 
  (do
    (defentity SomeEntity [:uuid :other])
    (create-SomeEntity "8e5625f8-60ec-11ea-a1ec-a45e60d5bfab"))




  )

