#!/usr/bin/env python3
import sys
import os

unused_applause_options = [
    ("isStrict", ["true", "false"]),
    ("isMobileNative", ["false"]),
]

# A list of Appium capabilities that need to be updated to use the appium prefix.
# Each entry in the list is a tuple containing the capability name and a list of matching scopes for update.
capabilities_to_update = [
    ("adbExecTimeout",["all"]),
    ("adbPort",["all"]),
    ("absoluteWebLocations",["all"]),
    ("additionalWebviewBundleIds",["all"]),
    ("allowDelayAdb",["all"]),
    ("allowProvisioningDeviceRegistration",["all"]),
    ("allowTestPackages",["all"]),
    ("androidInstallTimeout",["all"]),
    ("app",["all"]),
    ("appActivity",["all"]),
    ("appInstallStrategy",["all"]),
    ("appPackage",["all"]),
    ("appPushTimeout",["all"]),
    ("appWaitActivity",["all"]),
    ("appWaitDuration",["all"]),
    ("appWaitForLaunch",["all"]),
    ("appWaitPackage",["all"]),
    ("autoAcceptAlerts",["all"]),
    ("autoDismissAlerts",["all"]),
    ("autoFillPasswords",["all"]),
    ("autoGrantPermissions",["all"]),
    ("autoLaunch",["all"]),
    ("autoWebview",["all"]),
    ("autoWebviewName",["all"]),
    ("autoWebviewTimeout",["all"]),
    ("automationName",["all"]),
    ("avd",["all"]),
    ("avdArgs",["all"]),
    ("avdEnv",["all"]),
    ("avdLaunchTimeout",["all"]),
    ("avdReadyTimeout",["all"]),
    ("buildToolsVersion",["all"]),
    ("bundleId",["all"]),
    ("calendarAccessAuthorized",["all"]),
    ("calendarFormat",["all"]),
    ("chromeLoggingPrefs",["all"]),
    ("chromeOptions",["all"]),
    ("chromedriverArgs",["all"]),
    ("chromedriverChromeMappingFile",["all"]),
    ("chromedriverDisableBuildCheck",["all"]),
    ("chromedriverExecutable",["all"]),
    ("chromedriverExecutableDir",["all"]),
    ("chromedriverPort",["all"]),
    ("chromedriverPorts",["all"]),
    ("chromedriverUseSystemExecutable",["all"]),
    ("clearDeviceLogsOnStart",["all"]),
    ("clearSystemFiles",["all"]),
    ("commandTimeouts",["all"]),
    ("connectHardwareKeyboard",["all"]),
    ("customSSLCert",["all"]),
    ("derivedDataPath",["all"]),
    ("deviceName",["mobileweb_saucelabs", "mobilenative_all", "native_all"]),
    ("disableAutomaticScreenshots",["all"]),
    ("disableSuppressAccessibilityService",["all"]),
    ("disableWindowAnimation",["all"]),
    ("dontStopAppOnReset",["all"]),
    ("enableAsyncExecuteFromHttps",["all"]),
    ("enablePerformanceLogging",["all"]),
    ("enableWebviewDetailsCollection",["all"]),
    ("enforceAppInstall",["all"]),
    ("enforceFreshSimulatorCreation",["all"]),
    ("ensureWebviewsHavePages",["all"]),
    ("extractChromeAndroidPackageFromContextName",["all"]),
    ("forceAppLaunch",["all"]),
    ("forceSimulatorSoftwareKeyboardPresence",["all"]),
    ("fullContextList",["all"]),
    ("fullReset",["all"]),
    ("gpsEnabled",["all"]),
    ("hideKeyboard",["all"]),
    ("ignoreHiddenApiPolicyError",["all"]),
    ("includeDeviceCapsToSessionInfo",["all"]),
    ("includeSafariInWebviews",["all"]),
    ("injectedImageProperties",["all"]),
    ("intentAction",["all"]),
    ("intentCategory",["all"]),
    ("intentFlags",["all"]),
    ("iosInstallPause",["all"]),
    ("iosSimulatorLogsPredicate",["all"]),
    ("isHeadless",["all"]),
    ("keepKeyChains",["all"]),
    ("keyAlias",["all"]),
    ("keyPassword",["all"]),
    ("keychainPassword",["all"]),
    ("keychainPath",["all"]),
    ("keychainsExcludePatterns",["all"]),
    ("keystorePassword",["all"]),
    ("keystorePath",["all"]),
    ("language",["all"]),
    ("launchWithIDB",["all"]),
    ("locale",["all"]),
    ("localeScript",["all"]),
    ("localizableStringsDir",["all"]),
    ("logcatFilterSpecs",["all"]),
    ("logcatFormat",["all"]),
    ("maxTypingFrequency",["all"]),
    ("mjpegScreenshotUrl",["all"]),
    ("mjpegServerPort",["all"]),
    ("mockLocationApp",["all"]),
    ("nativeWebScreenshot",["all"]),
    ("nativeWebTap",["all"]),
    ("nativeWebTapStrict",["all"]),
    ("networkSpeed",["all"]),
    ("newCommandTimeout",["all"]),
    ("noReset",["all"]),
    ("noSign",["all"]),
    ("optionalIntentArguments",["all"]),
    ("orientation",["all"]),
    ("otherApps",["all"]),
    ("permissions",["all"]),
    ("platformVersion",["all"]),
    ("prebuiltWDAPath",["all"]),
    ("printPageSourceOnFindFailure",["all"]),
    ("processArguments",["all"]),
    ("recreateChromeDriverSessions",["all"]),
    ("reduceMotion",["all"]),
    ("reduceTransparency",["all"]),
    ("remoteAdbHost",["all"]),
    ("remoteAppsCacheLimit",["all"]),
    ("resetLocationService",["all"]),
    ("resetOnSessionStartOnly",["all"]),
    ("resultBundlePath",["all"]),
    ("resultBundleVersion",["all"]),
    ("safariAllowPopups",["all"]),
    ("safariGarbageCollect",["all"]),
    ("safariGlobalPreferences",["all"]),
    ("safariIgnoreFraudWarning",["all"]),
    ("safariIgnoreWebHostnames",["all"]),
    ("safariInitialUrl",["all"]),
    ("safariLogAllCommunication",["all"]),
    ("safariLogAllCommunicationHexDump",["all"]),
    ("safariOpenLinksInBackground",["all"]),
    ("safariSocketChunkSize",["all"]),
    ("safariWebInspectorMaxFrameLength",["all"]),
    ("scaleFactor",["all"]),
    ("screenshotQuality",["all"]),
    ("shouldTerminateApp",["all"]),
    ("shouldUseSingletonTestManager",["all"]),
    ("showChromedriverLog",["all"]),
    ("showIOSLog",["all"]),
    ("showXcodeLog",["all"]),
    ("shutdownOtherSimulators",["all"]),
    ("simpleIsVisibleCheck",["all"]),
    ("simulatorDevicesSetPath",["all"]),
    ("simulatorPasteboardAutomaticSync",["all"]),
    ("simulatorStartupTimeout",["all"]),
    ("simulatorTracePointer",["all"]),
    ("simulatorWindowCenter",["all"]),
    ("skipDeviceInitialization",["all"]),
    ("skipLogCapture",["all"]),
    ("skipLogcatCapture",["all"]),
    ("skipServerInstallation",["all"]),
    ("skipUnlock",["all"]),
    ("suppressKillServer",["all"]),
    ("systemPort",["all"]),
    ("timeZone",["all"]),
    ("udid",["all"]),
    ("uiautomator2ServerInstallTimeout",["all"]),
    ("uiautomator2ServerLaunchTimeout",["all"]),
    ("uiautomator2ServerReadTimeout",["all"]),
    ("uninstallOtherPackages",["all"]),
    ("unlockKey",["all"]),
    ("unlockStrategy",["all"]),
    ("unlockSuccessTimeout",["all"]),
    ("unlockType",["all"]),
    ("updatedWDABundleId",["all"]),
    ("useJSONSource",["all"]),
    ("useKeystore",["all"]),
    ("useNativeCachingStrategy",["all"]),
    ("useNewWDA",["all"]),
    ("usePrebuiltWDA",["all"]),
    ("usePreinstalledWDA",["all"]),
    ("useSimpleBuildTest",["all"]),
    ("useXctestrunFile",["all"]),
    ("userProfile",["all"]),
    ("waitForIdleTimeout",["all"]),
    ("waitForQuiescence",["all"]),
    ("wdaBaseUrl",["all"]),
    ("wdaConnectionTimeout",["all"]),
    ("wdaEventloopIdleDelay",["all"]),
    ("wdaLaunchTimeout",["all"]),
    ("wdaLocalPort",["all"]),
    ("wdaStartupRetries",["all"]),
    ("wdaStartupRetryInterval",["all"]),
    ("webDriverAgentUrl",["all"]),
    ("webkitResponseTimeout",["all"]),
    ("webviewConnectRetries",["all"]),
    ("webviewConnectTimeout",["all"]),
    ("webviewDevtoolsPort",["all"]),
    ("xcodeConfigFile",["all"]),
    ("xcodeOrgId",["all"]),
    ("xcodeSigningId",["all"]),
    ("commandTimeout", ["all"]),
    ("deviceOrientation", ["all"]),
    ("idleTimeout",["all"]),

]

# Conversion script for migrating appium capabilities to use the appium prefix. This is required for the new version of appium 
# that is imported by the Java SDK 6.1.0 and later.
def update_appium_capabilities(filepath):
    with open(filepath, 'r') as infile:
        contents = infile.read()
        
    # Check for unused applause options and remove them
    for option, values in unused_applause_options:
        for value in values:
            if f"\"{option}\": \"{value}\"," in contents:
                print(f"Removing unused option: {option} with value: {value} from {filepath}")
                contents = contents.replace(f"\"{option}\": \"{value}\",", "")
            elif f"\"{option}\" : \"{value}\"," in contents:
                print(f"Removing unused option: {option} with value: {value} from {filepath}")
                contents = contents.replace(f"\"{option}\" : \"{value}\",", "")


    for capability, cap_scope in capabilities_to_update:
        if f"\"{capability}\"" not in contents:
            continue  # Skip if capability is not present in the file
        # Check if the capability is in the correct scope
        if "all" in cap_scope:
            contents = update_single_capability(filepath, contents, capability)
            continue

        for scope in cap_scope:

            scopeparts = scope.split('_')
            if len(scopeparts) != 2:
                print(f"Warning: Scope {scope} for capability {capability} is not recognized. Skipping.")
                continue
            
            driver_type = scopeparts[0]
            driver_scope = scopeparts[1]
            
            # Check if the capability is in the correct driver type
            if driver_type not in contents.lower():
                print(f"Driver type {driver_type} for capability {capability} not found in {filepath}. Skipping.")
                continue

            if driver_scope == "all":
                contents = update_single_capability(filepath, contents, capability)
                continue

            if driver_scope in contents.lower():
                contents = update_single_capability(filepath, contents, capability)
                continue
            
            print(f"Warning: Scope {driver_scope} for capability {capability} not found in {filepath}. Skipping.")

    with open(filepath, 'w') as outfile:
        outfile.write(contents)

def update_single_capability(filepath, contents, capability):
    print(f"Updating capability: {capability} in {filepath}")
    return contents.replace(f"\"{capability}\"", f"\"appium:{capability}\"")


# main execution starts here
args_processed = 0
for filepath in sys.argv[1:]:
    args_processed = args_processed + 1
    if os.path.isfile(filepath) and filepath[-5:] == '.json':
        update_appium_capabilities(filepath)
    elif os.path.isdir(filepath):
        for root, dirs, files in os.walk(filepath):
            for path in files:
                if path[-5:] == '.json':
                    print("Processing file: " + os.path.join(root, path))
                    update_appium_capabilities(os.path.join(root, path))
    else:
        print(filepath + ' is not a valid json file or directory.')

if (args_processed == 0):
    print("Usage: %s <path-to-directory>]+")
    print("Converts files from ")
    print("   <path-to-driver>    :  relative or absolute path to a directory with .json or pom.xml files to convert")
