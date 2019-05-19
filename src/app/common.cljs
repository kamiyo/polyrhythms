(ns app.common)

(defonce worker (atom (js/Worker. "/js/worker.js")))
