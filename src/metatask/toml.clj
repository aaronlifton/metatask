(ns metatask.toml
  (:require [clj-toml.core :refer :all]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def toml-loc "config.toml")

(def config
  (slurp toml-loc))

(defn get-config [] (parse-string (slurp toml-loc)))

(defn str->regex [s] (java.util.regex.Pattern/compile s))

(defn toml-title-for [cat title]
  (if (nil? title)
    ""
    (str "[" cat "."
         (str/lower-case title)
         "]")))

(defn toml-val-for [k v]
  (str k " = \"" v "\""))

(defn hash->toml [{title :title desc :desc c :completed}
                  & {:keys [indent]
                     :or {indent 2}}]
  (let [idt (apply str (repeat indent " "))]
    (str "\n" idt (toml-title-for "tasks" title)
         "\n" idt (toml-val-for "title" title)
         "\n" idt (toml-val-for "desc" desc)
         "\n"
         (if (not (nil? c))
           (str idt (toml-val-for "completed", (str c))
           "\n"))
         "\n")))

(def infinity java.lang.Integer/MAX_VALUE)

(defn zip-lines-with-index [lines]
  (zipmap lines (range infinity)))

(defn filter-lines [pred]
  (filter pred (str/split-lines config)))

(defn find-lines [key pred]
  (let [line (filter-lines pred)]
    line))

(defn get-lines [key]
  (let [line (filter-lines
    (fn [x] (re-find (str->regex key) x)))]
    line))

(defn trim-and-zip-lines [k]
  (->> (get-lines k)
       (map str/trim)
       zip-lines-with-index))

; (defn get-line [k]
;   (->> (get-lines k)
;        (map str/trim)
;        zip-lines-with-index
;        (filter #(= (key %)
;                 (str "[tasks." k "]")))))

(defn toml-header-for? [tpl title]
  (= (key tpl)
     (str (toml-title-for "tasks" title))))

(defn not-toml-line [tpl n]
  (not= (val tpl) n))

(defn get-line [k]
  (->> (trim-and-zip-lines k)
       (filter #(toml-header-for? % k))))

(defn without-line [n k]
  (filter #(not-toml-line % n)
          (trim-and-zip-lines k)))

(defn update-line [k]
  (let [line (get-line k)
        coll (without-line (val line) k)]))


(defn update-toml-entry [key])
