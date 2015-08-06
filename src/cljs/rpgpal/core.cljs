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

(defn ten-previous-rolls []
  (take 10 (rest @results)))

(defn roll-formula []
  (go (let [formula @formula
            response (<! (http/get (str "/" formula)))
            body (:body response)]
        (swap! results conj (assoc body :formula formula)))))

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
                                            :class       "formula-input"}]])

(defn roll-view [roll]
  [:li.roll {:key (:id roll)}
   [:div
    [:p.formula (:formula roll)]
    [:p.result (:result roll)]]])

(defn results-view []
  [:div.results
   [:div.roll-result (-> (last-result) :result str)]
   [:h4 "Here are your previous rolls"]
   [:ul.rolls
    (for [r (previous-rolls)]
      (roll-view r))]])
;; -------------------------
;; Views

(defn home-page []
  [:div
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
