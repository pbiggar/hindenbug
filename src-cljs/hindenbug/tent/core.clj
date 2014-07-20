(ns hindenbug.tent.core)

(defmacro with-defaults [options & body]
  `(binding [defaults ~options]
     ~@body))

(defmacro with-url [new-url & body]
  `(binding [url ~new-url]
     ~@body))
