(ns swap-key.concurrent-test
  (:require [clojure.test :refer [deftest is]]
            [swap-key.core :as sk])
  (:import [java.util.concurrent Executors TimeUnit CountDownLatch]))



(defn concurrently [num f]
  (let [
        startLatch (CountDownLatch. num)
        pool (Executors/newFixedThreadPool num)
        callables (vec (map #(fn []
                               (.countDown startLatch)
                               (.await startLatch)
                               (f %))
                            (take num (range))))
        futures (.invokeAll pool callables)]
    (.shutdown pool)
    (.awaitTermination pool 5 TimeUnit/SECONDS)
    (into [] (for [t futures]
               (.get t)))))

(deftest test-swap-key
  (newline)
  (println "Running swap-key")
  (dotimes [y 100]
    (when (= 0 (mod y 10))
      (print ".")
      (flush))
    (let [work (fn work [a i]
                 (dotimes [n 1000]
                   #_(Thread/yield)
                   (when (and (< 0 i) (= 0 (mod n 5)))
                     (sk/swap-key! a (dec i) (fnil dec 0)))
                   (sk/swap-key! a i (fnil inc 0))))
          world (atom {})
          num 10
          _ (concurrently num (partial work world))
          expected (-> (reduce (fn [a x] (assoc a x 800))
                               {}
                               (take (dec num) (range)))
                       (assoc (dec num) 1000))]
      (is (= expected @world)))))

(deftest test-swap-key-in
  (newline)
  (flush)
  (println "Running swap-key-in")
  (dotimes [y 100]
    (when (= 0 (mod y 10))
      (print ".")
      (flush))
    (let [work (fn work [a i]
                 (dotimes [n 1000]
                   #_(Thread/yield)
                   (when (and (< 0 i) (= 0 (mod n 5)))
                     (sk/swap-key-in! a [:a (dec i)] (fnil dec 0)))
                   (sk/swap-key-in! a [:a i] (fnil inc 0))))
          world (atom {})
          num 10
          _ (concurrently num (partial work world))
          expected (-> (reduce (fn [a x] (assoc-in a [:a x] 800))
                               {}
                               (take (dec num) (range)))
                       (assoc-in [:a (dec num)] 1000))]

            (is (= expected @world)))))

(deftest test-swap-key-on
  (newline)
  (flush)
  (println "Running swap-key-on")
  (dotimes [y 100]
    (when (= 0 (mod y 10))
      (print ".")
      (flush))
    (let [work (fn work [a i]
                 (dotimes [n 1000]
                   #_(Thread/yield)
                   (when (and (< 0 i) 
                              (= 0 (mod n 5)))
                     (sk/swap-key-on! a 
                                      [:a (dec i)] 
                                      (fnil dec 0) 
                                      [[:a (dec i)]]))
                   (if (< 0 i)
                     (sk/swap-key-on! a 
                                      [:a i] 
                                      (fn [x _] (inc (or x 0))) 
                                      [[:a i] 
                                       [:a (dec i)]])
                     (sk/swap-key-on! a [:a i] 
                                      (fnil inc 0) 
                                      [[:a i]]))))
          world (atom {})
          num 10
          _ (concurrently num (partial work world))
          expected (-> (reduce (fn [a x] (assoc-in a [:a x] 800))
                               {}
                               (take (dec num) (range)))
                       (assoc-in [:a (dec num)] 1000))]

      (is (= expected @world)))))


