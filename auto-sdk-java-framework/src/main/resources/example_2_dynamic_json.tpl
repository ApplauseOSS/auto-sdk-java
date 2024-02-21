<#-- This isn't valid JSON.  It's written in Freemarker and the Applause SDK will convert
     this to JSON.

     Notice at the first line outside these comments, we get the current time and use that
     to build the name attribute

     When this is run through Freemarker, it will produce something like:

{
    "browserName" : "chrome"
    ,"newCommandTimeout" : 3000
    ,"idleTimeout":1000
    ,"commandTimeout": 600
    ,"name" : "Test on Mar 16, 2021 1:44:33 PM"
    ,"applause:options" : {
      "isW3C" : "false",
      "isStrict" : "false",
      "driverName" : "Chrome",
      "driverType" : "Browser",
      "factoryKey" : "WebDesktop",
      "driverOs"   : "Windows",
      "provider" : "SAUCELABS"
    }
}
     Notice that the Date will be different
-->
<#assign timeNow = .now>
{
    "browserName" : "chrome"
    ,"newCommandTimeout" : 3000
    ,"idleTimeout":1000
    ,"commandTimeout": 600
    ,"name" : "Test on ${timeNow}"
    ,"applause:options" : {
      "isW3C" : "false",
      "isStrict" : "false",
      "driverName" : "Chrome",
      "driverType" : "Browser",
      "factoryKey" : "WebDesktop",
      "driverOs"   : "Windows",
      "provider" : "SAUCELABS"
    }
}
