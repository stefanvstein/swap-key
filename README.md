# swap-key
Swapping atoms without the independent parts

It is usefull to have systems with few, but extensive, atoms as state regardless whether all values there in are always updated dependently. Expensive state transformation can be reduced when parts of a atom are updated independently. These parts may be updated dependently at other occations, and is hence usefully kept in a single atom. 

Swap some part of an associative atom with transformation of only that part, 
without bothering about, but accpeting, other parts. The transformation will not be retried unless the transformed part is beeing updated concurrently. The atomic retry is reduced to loop on `compare-and-set!`, reusing the result of transformation, while only other parts of the atom are updated concurrently.

```clojure
(defn sum [& xs]
  (apply + xs))
  
(def a (atom {:first 0 :second 1}))
(swap-key! a :second sum 5)
```

would would swap in `{:first 0 :second 6}` since `(+ 1 5)` is `6`, 
where `1` is the value of `:second` and `5` is and extra optional parameter.
The sum function will not be retried, unless the value of `:second` has changed concurrently, The value of `:second` is a dependency, and is applied to `sum` transformation. State transformation is only dependent of `:second`, and changes to other parts of the atom will only cause `compare-and-set!` without redundant transformation applications of `sum`.

```clojure
(def a (atom {:first 0 :tower {:left 1 :right {:value 2}}}))
(swap-key-in! a [:tower :right :value] sum 5)
```

would likewise swap in 7 in `{:first 0 :tower {:left 1 :right {:value 7}}}` similarly to update-in. The `sum` function will only be retried up on a concurrent change of the path `:tower :right :value`. When something else is concurrently changed a simple `compare-and-set!` will occur, with the already transformed value associated.

It's possible to update a part with more than one dependency, and still reduce retries:

```clojure
(def a (atom {:first 4 :tower {:left 1 :right {:value 2}}}))
(swap-key-on! a [:foo :bar] sum [[:first] [:tower :right :value]] 5)
```

..will swap in `[:foo :bar]` as in `{:foo {:bar 11} :first 4 :tower {:left 1 :right {:value 2}}}`
with paths `[:first]` and `[:tower :right :value]` as dependencies with values `4` and `2`, and idenpendent value `5`. The `sum` will only be retried if any of the values of dependencies `[:first]` and `[:tower :right :value]` has been cocurrently updated.



