(ns observe-it.core
    (:gen-class) (:import (observeIt IteratorExample ObserverExample)))

(defn -main [& args]
  (do
    (println "Executing Iterator Pipeline example:")
    (IteratorExample/iteratorPipeline )
    (println "Executing Iterator Union example:")
    (IteratorExample/unifyingIterators )
    (println "Executing Observer example:")
    (ObserverExample/observerPipeline )
    ))
