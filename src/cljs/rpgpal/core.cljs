(ns rpgpal.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:import goog.History))

(enable-console-print!)

(defonce formula (atom ""))
(defonce results (atom '()))

(defn last-result []
  (dissoc (first @results) :id))

(defn previous-rolls []
  (rest @results))

(defn avg [coll]
  (if (empty? coll)
    0
    (/ (reduce + coll) (count coll))))

(defn get-rolls [die-type]
  (->> @results (mapcat (comp die-type :rolls))
       (filter identity)
       (sort)))

(def avg-rolls (comp avg get-rolls))
(def roll-distribution (comp frequencies get-rolls))

(defn formulas []
  (->> @results (map :formula) distinct))

(defn roll-formula []
  (go (let [formula @formula
            response (<! (http/get (str "/roll/" formula)))
            body (:body response)]
        (swap! results conj (assoc body :formula formula)))))

(defn roll [new-formula]
  (reset! formula new-formula)
  (roll-formula))
;; -------------------------
;; View components

(defn input [atom opts]
  (let [default {:type      "text"
                 :on-change #(reset! atom (-> % .-target .-value))}
        options (merge default opts)]
    (fn []
      [:input (assoc options :value @atom)])))

(defn restricted-input [atom pattern opts]
  (let [default {:on-change (fn [event]
                              (let [value (-> event .-target .-value)]
                                (when (re-matches pattern value)
                                  (reset! atom value))))}]
    [input atom (merge default opts)]))

(defn formula-input []
  [:div
   [restricted-input formula #"[0-9d+\-]*" {:on-key-down #(case (.-keyCode %)
                                                           13 (roll-formula)
                                                           nil)
                                            :placeholder "Enter a dice formula"
                                            :class       "formula-input"}]
   [:button {:on-click #(roll-formula) :class "roll-btn"} "Roll!"]])

(defn formula-view [f]
  [:li {:key f}
    [:a {:on-click #(reset! formula f)} f]])

(defn formulas-view [formulas]
  [:ul.recent-formulas
    (for [f formulas]
      [formula-view f])])

(defn roll-view [roll]
  [:li.roll {:key (:id roll)}
   [:div
    [:p.formula (:formula roll)]
    [:p.result (:result roll)]
    [:p.rolls (str (:rolls roll))]]])

(defn results-view []
  [:div.results
   [:div.roll-result
    [:p (clojure.string/join " " [(-> (last-result) :result str)
                                  (-> (last-result) :rolls str)])]]
   [:h4 "Here are your previous rolls"]
   [:ul.rolls
    (for [r (previous-rolls)]
      (roll-view r))]])
;; -------------------------
;; Views

(defn home-page []
  [:div
   [formulas-view (take 5 (formulas))]
   [formula-input]
   [results-view]])

(defn current-page []
  [:div.container [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (session/put! :current-page #'home-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
