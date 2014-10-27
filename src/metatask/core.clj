(ns metatask.core
  (:require [metatask.toml :refer [hash->toml get-config toml-loc]]
            [metatask.interface :refer :all]
            [clj-toml.core :refer :all]
            [clojure.java.io :refer :all]
            [clojure.walk :refer [keywordize-keys]]
            [lanterna.screen :as s]))

;;; This is an incorrect implementation, such as might be written by
;;; someone who was used to a Lisp in which an empty list is equal to
;;; nil.
(defn first-element [sequence default]
  (if (nil? sequence)
    default
    (first sequence)))

; (def scr (s/get-screen))

(def tasks
  (atom []))

(defn set-tasks []
  (let [config-tasks (->> (get-config)
                          (keywordize-keys)
                          (:tasks))]
    (reset! tasks config-tasks))
  tasks)

(defn display-and-get [x]
  (println x)
  (read-line))

(defn save-task [task]
  (let [toml-task (hash->toml task)]
    (with-open [w (writer toml-loc :append true)]
      (.write w toml-task))))

(defn get-user-task [get-input?]
  (if (let [gi get-input?]
    (or (= gi "Y") (= gi "y")))
    (let [title (display-and-get "title")
          desc (display-and-get "desc")
          task (hash-map :title title :desc desc)]
      (save-task task)
      task)
    (println "Okay, have a nice day!\n")))

(defn make-stars [{t :title d :desc}]
  (let [lens (map #(.length %) [t d])
        max (apply max lens)
        stars (repeat max "*")]
    (apply str stars)))

(defn make-task-str
  [{title :title desc :desc :as task}]
  (if (nil? task)
  ""
  (str
    "Title: " title "\n"
    "Description: " desc "\n\n"
    (apply str (repeat 12 "-")) "\n")))

(defn print-all-tasks
  [tasks]
  (loop [q false
         n (count tasks)
         tasks tasks]
    (println (first tasks))
    (println "Press [ENTER] to continue...\n")
    (println "Press [Q] to quit...\n")
    (let [input (read-line)
          q? (or (= input "Q") (= input "q"))]
      (if (or (= 1 n) (= q? true))
        q?
        (recur q? (dec n) (rest tasks))))))

(defn display-tasks []
  (display-list @tasks {:x 1 :y 3}
    #(-> % last :title) :tasks))

(defn display-tasks-menu [scr current-scr]
  (display-menu scr current-scr
    {:q "Press [Q] to exit!"
     :goto-tasks "Press [T] again to go back."
     :back-tasks "Press [T] to view TODOs!"}
    :tasks)
  :menu)

(defn -main
  "Application entry point"
  [& args]
  (if (= (count @tasks) 0)
    (set-tasks))
  (let [get-input? (display-and-get "New Task? (Y/N)")
        t (get-user-task get-input?)]
    (display-interface display-tasks-menu
      display-tasks :tasks tasks)
    (if (not (nil? t))
      (println t))))
  ;   (println "Here are today's tasks:\n")
  ;   (print-all-tasks str-tasks)))
  ;   (prn c)))
