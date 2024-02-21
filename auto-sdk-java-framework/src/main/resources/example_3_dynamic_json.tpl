{
    "browserName" : "{browserName}"
    ,"newCommandTimeout" : 3000
    ,"idleTimeout":1000
    ,"commandTimeout": 600
<#if testRunId??>
    ,"name" : "Test Run ID: ${testRunId?c}"
</#if>
    ,"applause:options" : {
      "isW3C" : "false",
      "isStrict" : "false",
      "driverName" : "{driverName}",
      "driverType" : "Browser",
      "factoryKey" : "WebDesktop",
      "driverOs"   : "Windows",
      "provider" : "${provider}"
    }
}
