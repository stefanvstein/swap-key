# swap-key
Swap value of associative atom with f, without bothering about other parts of the atom value. Usefull when f is costly.

```clojure
(def a (atom {:first 0 :second 1}))
(swap-key! a :second (partial + 5) 2)
```
would would swap in {:first 0 :second 8} since (+ 5 1 2) is 8
