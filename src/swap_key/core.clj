(ns swap-key.core
  "Atoms can be finer grained than swap! Non dependent parts can be omitted when retrying")


(defn swap-key!
  "Swap k value of associative atom with f, without bothering 
  about other parts of atom value. The updating f, will not be 
  called again on retry, unless the original value of k in atom 
  has changed. Behaves similar to update. Useful when f is costly."
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
  has changed. Behaves similar to update-in. Useful when f i costly."
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


(defn values
  "The values of each nested path of m"
  [m paths]
  (map #(get-in m %) paths))

(defn update-on
  "update destination-path of m, with values from paths of m, 
  followed by all in xtra-args, applied to f"
  [m destination-path f paths xtra-args]
  (let [argz (concat (values m paths)
                     xtra-args)]
    (assoc-in m destination-path (apply f argz))))

(defn swap-key-on! 
  "swap destination-path of m, where values of paths of m, 
  followed by all in xtra-args, is applied to f. Unless the 
  original values of paths in atom has changed."
  [atom dest-path f paths & args]
  (loop [old @atom
         new (update-on old dest-path f paths args)]
    (if-not (compare-and-set! atom old new)
      (let [current @atom]
        (if (= (values old paths)
               (values current paths))
          (recur current 
                 (assoc-in current 
                           dest-path 
                           (get-in new dest-path)))
          (recur current 
                 (update-on current 
                            dest-path 
                            f 
                            paths 
                            args))))
      new)))
