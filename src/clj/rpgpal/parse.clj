(ns rpgpal.parse
  (:require [instaparse.core :as insta]))

(def ^:private parser
  (insta/parser "formula = add | term
                 add     = term {<'+'> term}
                 <term>  = dice | number
                 dice    = number <'d'> number
                 number  = #'-?[1-9][0-9]*'"))

(defn- prepare-formula
  "Replaces all instances of '-' with '+-' so we can parse all operations as additions,
  treating subtraction as additions of negative numbers.

  Ignore the first character, in case the formula starts with a negative number."
  [formula]
  (apply str (first formula)
             (clojure.string/replace (subs formula 1) "-" "+-")))

(defn parse [formula]
  (insta/parse parser (prepare-formula formula)))