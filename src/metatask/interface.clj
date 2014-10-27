(ns metatask.interface
  (:require [lanterna.screen :as s]
            [metatask.toml :refer :all]))

(def scr (s/get-screen))

(def cursor
  (atom {:x 1 :y 1}))

(defn highlight-item [scr c key]
  (let [down? (= key :down)
        up? (= key :up)
        mod (if (= down? true)
              inc
              (if (= up? true) dec nil))]
    (let [new-cursor (update-in c [:y] mod)]
      (reset! cursor new-cursor)))
  (println @cursor)
  (let [x (:x @cursor) y (:y @cursor)]
    (s/move-cursor scr x y)
    (s/redraw scr)))

(defn hash-map? [x]
  (= (type x) clojure.lang.PersistentHashMap))

(defn save-tasks [tasks]
  (let [toml-tasks (->> @tasks
                        (map val)
                        (map hash->toml))
        str-to-write (apply str toml-tasks)]
    (spit toml-loc str-to-write)
    str-to-write))

(defn toggle-truth [x]
  (println "toggle-truth")
  ; (println x)
  ; (println (type x))
  (if (or (= x "true") (true? x))
    false true))

(defn toggle-completed [xs key]
  (toggle-truth (:completed (get @xs key))))

(defn toggle-data [xs key title]
  (println (toggle-completed xs key))
  (let [new-truth (toggle-completed xs key)
        new-xs (assoc-in @xs [key title] new-truth)]
    (reset! xs new-xs)
    (let [task (get @xs key)
          new-task (update-in task [:completed]
                              not)
          t (hash->toml new-task)]
      ; (println t)
      (println new-task)
      (save-tasks xs))))

(defn input-loop [scr current-scr display-fn id data]
  (let [key (s/get-key-blocking scr)
        vec-data (if (hash-map? @data)
                   (map last @data)
                   @data)
        data-keys (map first @data)]
    (println key)
    (if (or (= key :down) (= key :up))
      (do
        (println (str "pressed" key))
        (highlight-item scr @cursor key)))
    (if (= key :enter)
      (let [n (- (:y @cursor) 3)
            x (nth vec-data n)
            key (nth data-keys n)]
        (println "pressed [enter]")
        (println x)
        (toggle-data data key :completed)))
    (if (or (= key \t) (= key \T))
      (do
        (println "pressed [T]")
        (let [next (display-fn)]
          (if (nil? next)
            (input-loop scr id display-fn id data)
            (input-loop scr next display-fn id data)))))
    (if (or (= key \Q) (= key \q))
      (s/stop scr)))
  (println current-scr)
  (println "looping...")
  (input-loop scr current-scr display-fn id data))

(defn display-menu [scr current-scr text id]
  (s/put-string scr 1 1 (:q text))
  (if (= current-scr id)
    (s/put-string scr 1 2 (:goto-tasks text))
    (s/put-string scr 1 2 (:back-tasks text)))
  :menu)

(defn display-interface [menu display-fn id data]
  (s/start scr)
  (menu scr :menu)
  (s/redraw scr)
  (input-loop scr nil display-fn id data))

(defn display-list [items pos formatter id]
  (loop [xs items
         n (count xs)
         x (:x pos)
         y (:y pos)]
    (if (empty? xs)
      :menu
      (do
        (s/put-string scr x y "* "
          {:fg :red :bg :black})
        (let [title (formatter (first xs))]
          (s/put-string scr (inc x) y title
            {:fg :black :bg :white})
          (recur (rest xs) n 1 (inc y))))))
  (s/redraw scr)
  id)
  ; (s/put-string scr 0 12 "*" {:fg :red})
  ; (s/put-string scr 1 12 "Task 1")
  ; (s/put-string scr 0 13 "*" {:fg :green})
  ; (s/put-string scr 1 14 "Task 2" {:fg :black :bg :yellow})
