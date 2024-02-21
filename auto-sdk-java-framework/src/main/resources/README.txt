The Applause SDK allows Selenium capabilities to be generated from Apache's Freemarker language.
  See https://freemarker.apache.org/docs/index.html

  Selenium capabilities are specified in JavaScript Object Notation (JSON)

  Applause Augments the capabilities with specific Applause options that are processed by
Applause systems and ignored by others.  These capabilities can be static JSON.  For
more advanced users, they may want to take advantage of Freemarker to build capabilities
that are modified from input parameters or Applause specific data

This directory has some examples

  example_1_static_json.json :  An example capabilities as static JSON.  This will
                                Run a test on a Chrome Browser on SauceLabs
                                In order to make this work, you will need a SauceLabs account
                                or run this through the Applause servers

  example_2_dynamic_json.tpl :  A simple Freemarker example.  Note that comments are contained
                                between '<#--' and '-->'
                                It uses a 

  example_3_dynamic_json.tpl :  A more complicated template showing conditionals
