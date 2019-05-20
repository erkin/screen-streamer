(ns screen-streamer.core
  "GUI portion of the program"
  (:use screen-streamer.const
        [screen-streamer.server :only [start-server stop-server]]
        [screen-streamer.client :only [start-client stop-client screens]]
        [clojure.core.async :only [thread]]
        seesaw.core)
  (:gen-class :main true))


(defonce client-running (atom false))



(def p (grid-panel :border "screen-streamer"
                   :columns 2
                   :rows 2))

(def f (frame :on-close :exit
              :content p))

(defn set-status [str] (config! p :border str))

(defn set-screen [imgs] (config! p :items imgs))

(defn clear-client-frame []
  (set-screen []))

(defn prepare-client-frame []
  (clear-client-frame)
  (while @client-running
    (Thread/sleep 70)
    (let [scrs @screens]
      (when (not (some nil? scrs))
        (set-screen (mapv #(label :icon (icon %)) scrs))))))



(def client-file-menu
  (menu
   :text "File"
   :items [(action
            :name "Start listening"
            :handler (fn [e]
                       (thread (start-client))
                       (reset! client-running true)
                       (thread (prepare-client-frame))
                       (set-status "Listening."))
            :tip "Establish connection.")
           (action
            :name "Stop listening"
            :handler (fn [e]
                       (stop-client)
                       (reset! client-running false)
                       (clear-client-frame)
                       (set-status "Not listening."))
            :tip "Disestablish connection.")
           (action
            :name "Exit"
            :handler (fn [e]
                       (stop-client)
                       (.dispose (to-frame e)))
            :tip "Exit program.")]))

(def server-file-menu
  (menu
   :text "File"
   :items [(action
            :name "Start broadcasting"
            :handler (fn [e]
                       (thread (start-server))
                       (set-status "Broadcasting."))
            :tip "List active connections.")
           (action
            :name "Stop broadcasting"
            :handler (fn [e]
                       (stop-server)
                       (set-status "Not broadcasting."))
            :tip "Close session to client.")
           (action
            :name "Exit"
            :handler (fn [e]
                       (stop-server)
                       (.dispose (to-frame e)))
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
  (config! f :size [640 :by 480])
  (set-status "Not listening.")
  (launch-frame f))

(defn launch-server []
  (config! f :menubar (menubar :items [server-file-menu help-menu]))
  (config! f :title (str program-name " server"))
  (config! f :size [320 :set 240])
  (by-status "Not broadcasting.")
  (launch-frame f))

(defn -main [& args]
  (native!)
  (case (input "Pick the mode of operation"
               :title program-name
               :choices ["client" "server"])
    "client" (launch-client)
    "server" (launch-server)
    nil      (System/exit 0)))
