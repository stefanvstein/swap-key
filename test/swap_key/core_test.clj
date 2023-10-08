(ns swap-key.core-test
  (:require [clojure.test :refer :all]
            [swap-key.core :refer :all]))

(deftest basics
  (let [a (atom {})]
    (is (= {:a 1}
           (swap-key! a :a (fnil inc 0))))
    (is (= {:a 1 :b 2}
           (swap-key! a :b (fnil + 0) 2)))
    (is (= {:a 1 :b 2 :c {:d 1}}
           (swap-key-in! a [:c :d] (fnil inc 0))))
    (is (=  {:a 1 :b 2 :c {:d 1 :e 2}}
            (swap-key-in! a [:c :e] (fnil + 0) 2)))
    (is (= {:a 1 :b 2 :c {:d 1 :e 2}}
           @a))))

(deftest with-vector
  (let [a (atom [])]
    (is (= [1]
           (swap-key! a 0 (fnil inc 0))))
    (is (=  [1 2]
            (swap-key! a 1 (fnil + 0) 2)))
    (swap-key! a 2 (constantly []))
    (is (= [1 2 [1]]
           (swap-key-in! a [2 0] (fnil inc 0))))
    (is (= [1 2 [1 2]]
            (swap-key-in! a [2 1] (fnil + 0) 2)))
    (is (= [1 2 [1 2]]
           @a))))

(deftest provocative
  (let [a (atom {:a 0 :b 0})
        times (atom 2)
        calls (atom 0)]
    (is (= {:a 11 :b 0}
           (swap-key! a :a (fn [v]
                             (swap! calls inc)
                             (when (< 0 @times)
                               (swap! times dec)
                               (swap! a #(update % :a + 5)))
                             (inc v)))))
    (is (= 3 @calls))
    (is (= {:a 11 :b 0} @a))))

(deftest updated
  (let [a (atom {:a 0 :b 0})
        calls (atom 0)]
    (is (= {:a 1 :b 1}
           (swap-key! a :a (fn [v]
                             (swap! calls inc)
                             (swap! a #(update % :b inc))
                             (inc v)))))
    (is (= 1 @calls))
    (is (= {:a 1 :b 1} @a))))

(deftest both
  (let [a (atom {:a 0 :b 0})
        times (atom 2)
        calls (atom 0)]
    (is (= {:a 11 :b 2}
           (swap-key! a :a (fn [v]
                             (swap! calls inc)
                             (when (= 0 @times)
                               (swap! times dec)
                               (swap! a #(update % :b + 2)))
                             (when (< 0 @times)
                               (swap! times dec)
                               (swap! a #(update % :a + 5)))
                             
                             (inc v)))))
    (is (= 3 @calls))
    (is (= {:a 11 :b 2} @a))))

(deftest both-in
  (let [a (atom {})
        times (atom 2)
        calls (atom 0)]
    (is (= {:a {:b 11} :c 2}
           (swap-key-in! a [:a :b] (fn [v]
                                     (swap! calls inc)
                                     (when (= 0 @times)
                                       (swap! times dec)
                                       (swap! a #(update % :c (fnil (partial + 2) 0))))
                                     (when (< 0 @times)
                                       (swap! times dec)
                                       (swap! a #(update-in % [:a :b]
                                                            (fnil (partial + 5) 0))))
        
                                     (if v (inc v)
                                         0)))))
    (is (= 3 @calls))
    (is (= {:a {:b 11} :c 2} @a))))
