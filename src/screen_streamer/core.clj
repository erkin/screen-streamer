(ns screen-streamer.core
  "GUI portion of the program"
  (:use screen-streamer.const
        [screen-streamer.network start-server stop-server burst-frame]
        seesaw.core)
  (:require [clojure.tools.logging :as log])
  (:gen-class :main true))

(def f (frame ; :on-close :exit
        :width 640 :height 480))

(def client-file-menu
  (menu
   :text "File"
   :items [(action
            :name "Connect"
            :handler (fn [e] (alert "connecting"))
            :tip "Establish connection.")
           (action
            :name "Disconnect"
            :handler (fn [e] (alert "disconnecting"))
            :tip "Disestablish connection.")
           (action
            :name "Exit"
            :handler (fn [e] (.dispose (to-frame e)))
            :tip "Exit program.")]))

(def server-file-menu
  (menu
   :text "File"
   :items [(action
            :name "List"
            :handler (fn [e] (alert "listing"))
            :tip "List active connections.")
           (action
            :name "Close"
            :handler (fn [e] (alert "closing"))
            :tip "Close session to client.")
           (action
            :name "Exit"
            :handler (fn [e] (.dispose (to-frame e)))
            :tip "Exit program.")]))

(def record-menu
  (menu
   :text "Record"
   :items [(action
            :name "Grab screen"
            :handler (fn [e] (alert "grabbing"))
            :tip "Saves screenshot to a file.")
           (action
            :name "Record screen"
            :handler (fn [e] (alert "recording"))
            :tip "Starts recording the screen.")]))

(def help-menu
  (menu
   :text "Help"
   :items [(action
            :name "Help"
            :handler (fn [e] (alert "helping"))
            :tip "Display help message.")
           (action
            :name "About"
            :handler (fn [e] (alert f about-message
                                   :title "About"
                                   :type :info))
            :tip "About this program.")]))



(defn launch-frame [f]
  (invoke-later (show! (pack! f))))

(defn launch-client []
  (log/info "Launching client.")
  (config! f :menubar (menubar :items [client-file-menu record-menu help-menu]))
  (config! f :title (str program-name " client"))
  (launch-frame f))

(defn launch-server []
  (log/info "Launching server.")
  (config! f :menubar (menubar :items [server-file-menu help-menu]))
  (config! f :title (str program-name " server"))
  (launch-frame f))

(defn -main [& args]
  (log/debug "Localhost:" (.getHostAddress (get-localhost-address)))
  (log/debug "Broadcast:" (.getHostAddress (get-broadcast-address)))
  ;; (native!)
  (case (input "Pick the mode of operation"
               :title program-name
               :choices ["client" "server"])
    "client" (launch-client)
    "server" (launch-server)))
