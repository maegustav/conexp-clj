;; Copyright ⓒ the conexp-clj developers; all rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns conexp.io.implications
  (:require [conexp.fca.implications :refer :all]
            [conexp.io.util          :refer :all]
            [conexp.io.json          :refer :all]
            [clojure.data.json :as json]
            [json-schema.core  :as json-schema]))

;;; Input format dispatch

(define-format-dispatch "implication")
(set-default-implication-format! :json)

;;; Formats

;; Json helpers

(defn- implication->json
  [impl]
  {:premise (premise impl)
   :conclusion (conclusion impl)})

(defn implications->json
  [impl]
  {:implications
     (mapv implication->json impl)})

(defn- json->implication
  [json-impl]
  (make-implication (into #{} (:premise json-impl))
                    (into #{} (:conclusion json-impl))))

(defn json->implications
  [json]
  (let [impl (:implications json)]
    (map json->implication impl)))

;; Json Format

(add-implication-input-format :json
                               (fn [rdr]
                                 (try (json-object? rdr)
                                      (catch Exception _))))

(define-implication-output-format :json
  [impl file]
  (with-out-writer file
    (print (json/write-str (implications->json impl)))))

(define-implication-input-format :json
  [file]
  (with-in-reader file
    (let [impl (json/read *in* :key-fn keyword)]
      (assert (matches-schema? impl "implications_schema_v1.0.json")
              "The input file does not match the schema given at src/main/resources/schemas/implications_schema_v1.0.json.")
      (json->implications impl))))
