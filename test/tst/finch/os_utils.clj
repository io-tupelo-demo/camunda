(ns tst.finch.os-utils
  (:use finch.os-utils tupelo.core tupelo.test)
  (:require
    [tupelo.string :as str]
    ))

(verify
  (with-redefs [system-get-property (const->fn "Windows 1776")]
    (isnt (is-linux?))
    (isnt (is-mac?))
    (is (is-windows?)))
  (with-redefs [system-get-property (const->fn "Mac OS 1776")]
    (isnt (is-linux?))
    (is (is-mac?))
    (isnt (is-windows?)))
  (with-redefs [system-get-property (const->fn "Linux Ultra 1776")]
    (is (is-linux?))
    (isnt (is-mac?))
    (isnt (is-windows?))))

