(ns leiningen.ezbake-core
  (:import (java.util.jar JarEntry JarFile))
  (:require [clojure.java.io :as io]
            [leiningen.core.main :as lein-main]
            [puppetlabs.ezbake.core :as core]
            [puppetlabs.ezbake.dependency-utils :as deputils]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Constants

(def project-resource-path
  "META-INF/leiningen/puppetlabs/lein-ezbake/project.clj")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Jar Helpers

(defn- get-jar-file-path
  "Return path to JarFile of the lein-ezbake project."
  []
  (->> (io/resource project-resource-path)
       .getPath
       (re-find #":(.*)!")
       second))

(defn- copy-jar-resources
  [resource-prefix resource-path]
  (let [jar-file-path (get-jar-file-path)
        jar-file (JarFile. jar-file-path)
        jar-entries (deputils/find-files-in-dir-in-jar
                      jar-file
                      resource-prefix)]
    (lein-main/info (format "Copying lein-ezbake resources from %s to %s"
                            jar-file-path resource-path))
    (deputils/cp-files-from-jar jar-entries jar-file resource-path)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Consumable API

(defn prepare-resource-dir
  "Prepare a local resource directory for use by ezbake core when populating the
  packaging staging directory.

  TODO: Add configuration option to clone a git repository containing ezbake
  resources instead of pulling them from the lein-ezbake jar."
  [project]
  (let [template-type (get-in project [:lein-ezbake
                                       :resources
                                       :type]
                              :jar)]
    (case template-type
      :git (throw (RuntimeException.
                   (format "Resource type, %s, not implemented."
                           (str template-type))))
      :jar (copy-jar-resources core/resource-prefix core/resource-path))))
