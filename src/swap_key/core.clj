(ns swap-key.core)


(defn swap-key!
  "Swap k value of associative atom with f, without bothering 
  about other parts of atom value. The updating f, will not be 
  called again on retry, unless the original value of k in atom 
  has changed. Behaves similar to update. Usefull when f is costly."
  ([atom k f]
   (loop [old @atom
          new (update old k f)]
     (if-not (compare-and-set! atom old new)
       (let [current @atom]
         (if (= (get old k) 
                (get current k))
           (recur current (assoc current k (get new k)))
           (recur current (update current k f))))
       new)))
  ([atom k f & more]
   (loop [old @atom
          new (apply update old k f more)]
     (if-not (compare-and-set! atom old new)
       (let [current @atom]
         (if (= (get old k) 
                (get current k))
           (recur current (assoc current k (get new k)))
           (recur current (apply update current k f more))))
       new))))

(defn swap-key-in!
  "Swap! in a nested associative atom, where ks is a
  sequence of keys describing the path, and f is a function
  that will take the old value, and any supplied args, and return 
  the new value swapped in. The updating f, will not be 
  called again on retry, unless the original value at path in atom 
  has changed. Behaves similar to update-in. Usefull when f i costly."
  ([atom ks f]
   (loop [old @atom
          new (update-in old ks f)]
     (if-not (compare-and-set! atom old new)
       (let [current @atom]
         (if (= (get-in old ks) 
                (get-in current ks))
           (recur current (assoc-in current ks (get-in new ks)))
           (recur current (update-in current ks f))))
       new)))
  ([atom ks f & args]
   (loop [old @atom
          new (apply update-in old ks f args)]
     (if-not (compare-and-set! atom old new)
       (let [current @atom]
         (if (= (get-in old ks) 
                (get-in current ks))
           (recur current (assoc-in current ks (get-in new ks)))
           (recur current (apply update-in current ks f args))))
       new))))


