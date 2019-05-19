(ns screen-streamer.core
  "GUI portion of the program"
  (:use [screen-streamer.const :only [program-name about-message]]
        [screen-streamer.server :only [start-server stop-server]]
        [screen-streamer.client :only [start-client stop-client]]
        [clojure.core.async :only [thread]]
        seesaw.core)
  (:gen-class :main true))

(def f (frame ; :on-close :exit
        :width 640 :height 480))

(def client-file-menu
  (menu
   :text "File"
   :items [(action
            :name "Connect"
            :handler (fn [e] (thread (start-client)))
            :tip "Establish connection.")
           (action
            :name "Disconnect"
            :handler (fn [e] (stop-client))
            :tip "Disestablish connection.")
           (action
            :name "Exit"
            :handler (fn [e] (.dispose (to-frame e)))
            :tip "Exit program.")]))

(def server-file-menu
  (menu
   :text "File"
   :items [(action
            :name "Start broadcast"
            :handler (fn [e] (thread (start-server)))
            :tip "List active connections.")
           (action
            :name "End broadcast"
            :handler (fn [e] (stop-server))
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
            :tip about-message)]))



(defn launch-frame [f]
  (invoke-later (show! (pack! f))))

(defn launch-client []
  (config! f :menubar (menubar :items [client-file-menu record-menu help-menu]))
  (config! f :title (str program-name " client"))
  (launch-frame f))

(defn launch-server []
  (config! f :menubar (menubar :items [server-file-menu help-menu]))
  (config! f :title (str program-name " server"))
  (launch-frame f))

(defn -main [& args]
  (native!)
  (case (input "Pick the mode of operation"
               :title program-name
               :choices ["client" "server"])
    "client" (launch-client)
    "server" (launch-server)))
