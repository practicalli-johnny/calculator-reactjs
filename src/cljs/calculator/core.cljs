(ns calculator.core
  (:require [cljsjs.react]
            [cljsjs.react.dom]
            [clojure.string :as str]))

;; From the examples in the following tutorial
;; https://lambdaisland.com/episodes/react-app-clojurescript



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Model / State
(def app-state (atom {:display 0 :history []}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Application Actions

(defn digit-pressed [digit]
  (swap! app-state update :display #(long (str % digit))))

(defn operator-pressed [op]
  (swap! app-state (fn [state]
                     (-> state
                         (update :history #(conj % (:display state) op))
                         (assoc :display 0)))))

(defn compute [result & [op num & xs]]
  (case op
    "+" (recur (+ result num) xs)
    "-" (recur (- result num) xs)
    "/" (recur (/ result num) xs)
    "*" (recur (* result num) xs)
    result))

(defn equals-pressed [_]
  (swap! app-state (fn [state]
                     (let [history (conj (:history state) (:display state))
                           result (apply compute history)]
                       (-> state
                           (assoc :display result)
                           (assoc :history []))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Components


(defn element
  "Create react.js elements, converting clojure values into Javascript"
  [type props & children]
  (js/React.createElement type (clj->js props) children))


;; Simple page using several elements
;; (def vdom (element "div" {}
;;             (element "p" {} "Hello from React")
;;             (element "img" {:src "/parrot.JPEG"})))



(defn component
  "Creates react.js components"
  [name & {:keys [render]}]
  (js/React.createClass
   #js {:displayName name
        :render (fn []
                  (this-as t
                    (render (js->clj (.-props t) :keywordize-keys true))))}))



(def Title
  (component "Title"
             :render (fn [props]
                       (element "p" {} "Hello from React"))))

(def History
  (component "History"
    :render (fn [props]
              (element "div" {:className "history"} (str/join " " (props :history))))))

(def Display
  (component "Display"
    :render (fn [props]
              (element "div" {:className "display"} (props :value)))))

(def Button
  (component "Button"
    :render (fn [props]
              (let [label (props :label)
                    handler (props :onPress)]
                (element "button" {:className "button"
                                   :onClick #(handler label)} label)))))

(def Keypad
  (component "Keypad"
    :render (fn [props]
              (element "div" {}
                (element "div" {}
                  (element Button {:label "7" :onPress digit-pressed})
                  (element Button {:label "8" :onPress digit-pressed})
                  (element Button {:label "9" :onPress digit-pressed})
                  (element Button {:label "/" :onPress operator-pressed}))
                (element "div" {}
                  (element Button {:label "6" :onPress digit-pressed})
                  (element Button {:label "5" :onPress digit-pressed})
                  (element Button {:label "4" :onPress digit-pressed})
                  (element Button {:label "*" :onPress operator-pressed}))
                (element "div" {}
                  (element Button {:label "3" :onPress digit-pressed})
                  (element Button {:label "2" :onPress digit-pressed})
                  (element Button {:label "1" :onPress digit-pressed})
                  (element Button {:label "-" :onPress operator-pressed}))
                (element "div" {}
                  (element Button {:label "0" :onPress digit-pressed})
                  (element Button {:label "." :onPress operator-pressed})
                  (element Button {:label "=" :onPress equals-pressed})
                  (element Button {:label "+" :onPress operator-pressed}))))))

;; Component: Calculator
;; A component composed of the components, Title, History, Display, Keypad
(def Calculator
  (component "Calculator"
    :render (fn [props]
              (element "div" {}
                (element Title {})
                (element History {:history (props :history)})
                (element Display {:value (props :display)})
                (element Keypad {})))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; React functions

(defn render [state]
  (js/ReactDOM.render (element Calculator state)
                      (js/document.getElementById "app")))


(render @app-state)



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing code

;; use add-watch to call render for every state change, specifically for changes to the atom made in the REPL
;; add-watch takes three arguments: the atom to be watched, a key to identify the watch, and the callback function.
;; The callback will receive the identifier key, the atom, the old state, and the new state. We only care about the new state, so you can ignore the rest.

(add-watch app-state :redraw (fn [_ _ _ state] (render state)))

;; Testing the compute funcition
;; (compute 10 "+" 20 "-" 5)


;; Test updating of the state
;; (swap! app-state update :display inc)

;; (swap! app-state update :display #(+ 7))

(swap! app-state assoc :history 42)

;; (swap! app-state conj {:foo "bar"})

;; (reset! app-state {:display 0 :history []})
