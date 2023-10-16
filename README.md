# swap-key
Swap value of associative atom with f, without bothering about other parts of the atom value. Usefull when f, state tranformation, is costly.

```clojure
(defn sum [& xs]
  (apply + xs))
  
(def a (atom {:first 0 :second 1}))
(swap-key! a :second sum 5)
```
would would swap in `{:first 0 :second 6}` since `(+ 1 5)` is 6, 
where 1 is the value of :second and 5 is and extra optional parameter.
The sum function will not be retried, unless the value of :second has changed concurrently. Here :second is a dependency, whose value is applied to sum. State transformation is only dependent of :second, and changes to other parts will only cause compare-and-set! without redundant tranformation, with sum.

```clojure
(def a (atom {:first 0 :tower {:left 1 :right {:value 2}}}))
(swap-key-in! a [:tower :right :value] sum 5)
```

would likewise swap in 7 in `{:first 0 :tower {:left 1 :right {:value 7}}}` similarly to update-in. The sum function will only be retried of a concurrent change occured on the path :tower :right :value. When sonemting else is concurrently changed a sinple compare-and-set on the already tranformed value will be made.

```clojure
(def a (atom {:first 4 :tower {:left 1 :right {:value 2}}}))
(swap-key-on! a [:foo :bar] sum [[:first] [:tower :right :value]] 5)
```

..will swap in `[:foo :bar]` as in `{:foo {:bar 11} :first 4 :tower {:left 1 :right {:value 2}}}`
with paths `[:first]` and `[:tower :right :value]` as dependencies with values 4 and 2, and value 5. The sum vill only be retried if any of the values of dependencies `[:first]` and `[:tower :right :value]` has been updated.



