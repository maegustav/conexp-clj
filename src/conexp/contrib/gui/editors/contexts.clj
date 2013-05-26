;; Copyright (c) Daniel Borchmann. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

;; This file has been written by Immanuel Albrecht, with modifications by DB

(ns conexp.contrib.gui.editors.contexts
  (:use [conexp.base :exclude (select)]
        conexp.fca.contexts
        conexp.fca.lattices
        conexp.io
        conexp.layouts
        [conexp.layouts.base :only (lattice)]
        conexp.contrib.draw.lattices
        conexp.contrib.gui.util
        conexp.contrib.gui.editors.context-editor.context-editor
        conexp.contrib.gui.plugins.base)
  (:use seesaw.core)
  (:use [clojure.string :only (replace)
                        :rename {replace replace-str}])
  (:import [java.io File]))

(ns-doc
 "Provides context-editor, a plugin for contexts for the standard GUI
  of conexp-clj.")

;;; The Plugin

(declare load-context-editor unload-context-editor)

(define-plugin context-editor
  "Context editor plugin."
  :load-hook   load-context-editor,
  :unload-hook unload-context-editor)

;;; The Actions

(defn- load-context-and-go
  "Loads context with given loader and adds a new tab with a context-editor."
  [frame]
  (when-let [^File file (choose-open-file frame)]
    (let [path (.getPath file),
          thing (read-context path)]
      (add-tab frame
               (make-context-editor thing)
               (str "Context " path)))))

(defn- clone-context-and-go
  "Loads context with given loader and adds a new tab with a context-editor."
  [frame]
  (add-tab frame
    (clone-context-view-from-panel (current-tab frame))
    (str (current-tab-title frame) "*")))

(defn- context-and-go
  "Opens given context in a new context editor tab."
  [frame context]
  (add-tab frame
           (make-context-editor context)
           "Context"))

(defn- second-op-context-and-go
  "Show the current second operand context in a new tab."
  [frame]
  (let [thing (get-current-second-operand-context)]
    (add-tab frame
             (make-context-editor thing)
             "Context")))

(defn- save-context-and-go
  "Saves context with given writer."
  [frame writer]
  (when-let [thing (get-context-from-panel (current-tab frame))]
    (when-let [^File file (choose-save-file frame)]
      (let [path (.getPath file)]
        (writer thing path)))))

(defn- show-lattice-and-go
  "Shows concept lattice of current tab."
  [frame]
  (let [thing (get-context-from-panel (current-tab frame))]
    (add-tab frame
             (make-lattice-editor frame
                                  (standard-layout (concept-lattice thing)))
             "Concept-Lattice")))

;;; The Hooks

(defn- context-menu
  "Returns the context-editor menu for a given frame"
  [frame]
  (menu :text "Context",
        :items [(menu-item :text "New Context",
                           :listen [:action (fn [_]
                                              (context-and-go frame
                                                              (make-context (set-of-range 10)
                                                                            (set-of-range 10)
                                                                            #{})))]),
                (menu-item :text "Load Context",
                           :listen [:action (fn [_]
                                              (load-context-and-go frame))]),
                (menu-item :text "Random Context",
                           :listen [:action (fn [_]
                                              (context-and-go frame (rand-context 5 5 0.4)))]),
                (menu-item :text "Second Operand Context",
                           :listen [:action (fn [_]
                                              (second-op-context-and-go frame))]),
                :separator
                (menu :text "Save Context",
                      :items (vec (map (fn [format]
                                         (menu-item :text (str (replace-str (str format) ":" "")
                                                               " format"),
                                                    :listen [:action
                                                             (fn [_]
                                                               (save-context-and-go
                                                                frame
                                                                (fn [ctx path]
                                                                  (write-context format ctx path))))]))
                                       (list-context-output-formats)))),
                (menu-item :text "Clone Current Context View"
                           :listen [:action (fn [_]
                                              (clone-context-and-go frame))]),
                :separator
                (menu-item :text "Show Concept Lattice",
                           :listen [:action (fn [_]
                                              (show-lattice-and-go frame))])]))

(let [menu-hash (atom {})]

  (defn- load-context-editor
    "Loads the context-editor plugin in frame."
    [frame]
    (swap! menu-hash
           assoc frame (add-menus frame [(context-menu frame)])))

  (defn- unload-context-editor
    "Unloads the context-editor plugin from frame."
    [frame]
    (let [menu (get @menu-hash frame)]
      (remove-menus frame [menu])
      (swap! menu-hash dissoc frame)))

  nil)

;;; The End

nil
