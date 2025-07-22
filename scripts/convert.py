#!/usr/bin/env python3
import sys
import os
import re

# Conversion script for migrating Automation v5.0.x projects to Automation v6.0.0
# Finds and replaces a number of common strings to their v6.0.0 equivalents.
# Note that this script will only get you "part of the way" there -
# some further work will be required.
def replace_imports(contents):
    imports = [
      ("com.applause.auto.pageobjectmodel.elements.BaseElement", "com.applause.auto.pageobjectmodel.base.BaseElement"),
      ("com.applause.allure.appenders", "com.applause.auto.helpers.allure.appenders"),
      ("com.applause.allure", "com.applause.auto.helpers.allure"),
      ("com.applause.email", "com.applause.auto.helpers.email"),
      ("com.applause.google", "com.applause.auto.helpers.google"),
      ("com.applause.http.mapping", "com.applause.auto.helpers.http.mapping"),
      ("com.applause.http.restassured.client", "com.applause.auto.helpers.http.restassured.client"),
      ("com.applause.http.restassured", "com.applause.auto.helpers.http.restassured"),
      ("com.applause.jira.annotations.scanner", "com.applause.auto.helpers.jira.annotations.scanner"),
      ("com.applause.jira.annotations", "com.applause.auto.helpers.jira.annotations"),
      ("com.applause.jira.clients.modules.jira", "com.applause.auto.helpers.jira.clients.modules.jira"),
      ("com.applause.jira.clients.modules.xray", "com.applause.auto.helpers.jira.clients.modules.xray"),
      ("com.applause.jira.clients", "com.applause.auto.helpers.jira.clients"),
      ("com.applause.jira.constants", "com.applause.auto.helpers.jira.constants"),
      ("com.applause.jira.dto.jql", "com.applause.auto.helpers.jira.dto.jql"),
      ("com.applause.jira.dto.requestmappers", "com.applause.auto.helpers.jira.dto.requestmappers"),
      ("com.applause.jira.dto.responsemappers.iteration", "com.applause.auto.helpers.jira.dto.responsemappers.iteration"),
      ("com.applause.jira.dto.responsemappers.steps", "com.applause.auto.helpers.jira.dto.responsemappers.steps"),
      ("com.applause.jira.dto.responsemappers", "com.applause.auto.helpers.jira.dto.responsemappers"),
      ("com.applause.jira.dto.shared", "com.applause.auto.helpers.jira.dto.shared"),
      ("com.applause.jira.exceptions", "com.applause.auto.helpers.jira.exceptions"),
      ("com.applause.jira.helper", "com.applause.auto.helpers.jira.helper"),
      ("com.applause.jira.listeners", "com.applause.auto.testng.listeners"),  # Corrected path here
      ("com.applause.jira.restclient", "com.applause.auto.helpers.jira.restclient"),
      ("com.applause.mobile.deeplinks", "com.applause.auto.helpers.mobile.deeplinks"),
      ("com.applause.mobile.file_uploading.SauceLabs", "com.applause.auto.helpers.mobile.fileuploading.saucelabs"),
      ("com.applause.mobile", "com.applause.auto.helpers.mobile"),
      ("com.applause.testdata.yaml", "com.applause.auto.helpers.testdata.yaml"),
      ("com.applause.testdata", "com.applause.auto.helpers.testdata"),
      ("com.applause.util", "com.applause.auto.helpers.util"),
      ("com.applause.web", "com.applause.auto.helpers.web"),
    ]

    for old_import, new_import in imports:
        contents = re.sub(old_import, new_import, contents)

    lines = contents.split('\n')
    lines_without_duplicates = []

    existing_imports = set()
    for line in lines:
        if line in existing_imports:
            continue
        elif line[:6] == "import":
            existing_imports.add(line)
        lines_without_duplicates.append(line)
    return '\n'.join(lines_without_duplicates)

def add_import_if_not_present(import_statement, contents):
    lines = contents.split('\n')
    lines_without_duplicates = []

    existing_imports = set()
    last_import = 1
    contains_import = False
    for i, line in enumerate(lines):
        if line in existing_imports:
            continue
        elif line[:6] == "import":
            existing_imports.add(line.rstrip())
            last_import = i
            if line.rstrip().endswith(import_statement):
                contains_import = True
        lines_without_duplicates.append(line.rstrip())
    if not contains_import:
        lines_without_duplicates.insert(last_import, import_statement)
    return '\n'.join(lines_without_duplicates)

def fix_locators(contents):
    locators = [
      ("@Locate\(\s*id = ", "using = Strategy.ID, value = "),
      ("@Locate\(\s*css = ", "using = Strategy.CSS, value = "),
      ("@Locate\(\s*xpath = ", "using = Strategy.XPATH, value = "),
      ("@Locate\(\s*className = ", "using = Strategy.CLASSNAME, value = "),
      ("@Locate\(\s*name = ", "using = Strategy.NAME, value = "),
      ("@Locate\(\s*tagName = ", "using = Strategy.TAGNAME, value = "),
      ("@Locate\(\s*linkText = ", "using = Strategy.LINKTEXT, value = "),
      ("@Locate\(\s*accessibilityId = ", "using = Strategy.ACCESSIBILITYID, value = "),
      ("@Locate\(\s*androidUIAutomator = ", "using = Strategy.ANDROID_UIAUTOMATOR, value = "),
      ("@Locate\(\s*iOSClassChain = ", "using = Strategy.IOS_CLASSCHAIN, value = "),
      ("@Locate\(\s*iOSNsPredicate = ", "using = Strategy.IOS_NSPREDICATE, value = "),
      ("@Locate\(\s*appiumClassName = ", "using = Strategy.APPIUM_CLASSNAME, value = "),
      ("@Locate\(\s*jQuery = ", "using = Strategy.JQUERY, value = "),
      ("@Locate\(\s*javascript = ", "using = Strategy.JAVASCRIPT, value = "),
    ]

    for old_style_locator, new_style_locator in locators:
        if old_style_locator in contents:
            contents = add_import_if_not_present("import com.applause.auto.pageobjectmodel.base.Strategy;", contents)
            break

    for old_style_locator, new_style_locator in locators:
        contents = re.sub(old_style_locator , "@Locate(" + new_style_locator, contents)

    return contents

def check_for_sub_components(contents):
    allLocates = re.finditer("@Locate\(.*\)", contents)
    for i in allLocates:
        if "using" not in i.group():
            return "@Locate annotation found without an underlying locator. If no locator is tied to the element, please add the @SubComponent annotation."
    return None

def check_for_get_web_element(contents):
    if re.findall("getWebElement", contents):
        return "getWebElement() method found. Please replace with getUnderlyingWebElement()"
    return None
def check_for_applause_config_usage(contents):
    return re.sub("ApplauseEnvironmentConfigurationManager.get()", "ApplauseEnvironmentConfigurationManager.INSTANCE.get()", contents)

def check_for_config_usage(contents):
    return re.sub("EnvironmentConfigurationManager.get()", "EnvironmentConfigurationManager.INSTANCE.get()", contents)

def remove_sdk_helper_create(contents):

    result = re.search("SdkHelper.create\(.+.class\);", contents)
    while result is not None:
        className = result.group().split('(')[1].split('.class')[0]
        contents = contents[:result.start()] + "PageObjectBuilder.withContext(SdkHelper.getDriverContext()).forBaseComponent(" + className + ".class).initialize();" + contents[result.end():]
        contents = add_import_if_not_present("import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;", contents)
        result = re.search("SdkHelper.create\(.+.class\)", contents)
    return contents

def convert_java(filepath):
    contents = ''

    warnings = []
    with open(filepath, 'r') as infile:
        contents = infile.read()
        contents = replace_imports(contents)
        contents = fix_locators(contents)
        contents = check_for_applause_config_usage(contents)
        contents = check_for_config_usage(contents)
        contents = remove_sdk_helper_create(contents)
        warnings.append(check_for_sub_components(contents))

    with open(filepath, 'w') as outfile:
        outfile.write(contents)

    print('Converted ' + filepath)
    return warnings


def convert_pom(filepath):
    new_pom = ''

    with open(filepath, 'r') as infile:
        new_pom = infile.read()
    pom_substitutions = [
        ("<com.applause.sdk.java.version>.+</com.applause.sdk.java.version>", "<com.applause.sdk.java.version>6.0.0</com.applause.sdk.java.version>"),
        ("<source>17</source>", "<source>21</source>"),
        ("<target>17</target>", "<target>21</target>"),
        ("<dependency>\s+<groupId>com.applause</groupId>\s+<artifactId>auto.sdk.java</artifactId>\s+<version>.*</version>\s+<type>pom</type>\s+<scope>import</scope>\s+</dependency>""",
            """<dependency>
                <groupId>com.applause</groupId>
                <artifactId>auto-sdk-java-page-object</artifactId>
                <version>${com.applause.sdk.java.version}</version>
            </dependency>
            <dependency>
                <groupId>com.applause</groupId>
                <artifactId>auto-sdk-java-testng</artifactId>
                <version>${com.applause.sdk.java.version}</version>
            </dependency>"""),
        (r"\s*<repository>\s*<id>github</id>\s*<name>GitHub OWNER Apache Maven Packages</name>\s*<url>https://maven.pkg.github.com/ApplauseAuto/helper-sdk.auto</url>\s*<releases>\s*<enabled>true</enabled>\s*<updatePolicy>always</updatePolicy>\s*</releases>\s*<snapshots>\s*<enabled>true</enabled>\s*</snapshots>\s*</repository>\s*", ""), # Remove helper repo
        (r"\s*<dependency>\s*<artifactId>helper-sdk</artifactId>\s*<groupId>com.applause</groupId>\s*<version>\${helper.sdk.version}</version>\s*</dependency>\s*", ""), # Remove helper dependency
        (r"(</dependencies>)", r"""<dependency>
                    <groupId>com.applause</groupId>
                    <artifactId>auto-sdk-java-helpers</artifactId>
                    <version>${com.applause.sdk.java.version}</version>
                </dependency>\n\1""") # Add auto-sdk-java-helpers if not present
    ]

    for old_line, new_line in pom_substitutions:
        new_pom = re.sub(old_line, new_line, new_pom, flags=re.DOTALL | re.MULTILINE)

    with open(filepath, 'w') as outfile:
        outfile.write(new_pom)

    print('Converted ' + filepath)


# main ... execution starts here
args_processed = 0
warnings = {}
for filepath in sys.argv[1:]:
    args_processed = args_processed + 1
    if os.path.isfile(filepath) and filepath[-5:] == '.java':
        convert_java(filepath)
    elif os.path.isfile(filepath) and filepath == 'pom.xml':
        convert_pom(filepath)
    elif os.path.isdir(filepath):
        for root, dirs, files in os.walk(filepath):
            for path in files:
                if path[-5:] == '.java':
                    warnings[path] = convert_java(os.path.join(root, path))
                if path == "pom.xml":
                    convert_pom(path)
    else:
        print(filepath + ' is not a valid Java file, pom.xml, or directory.')

for filepath, warning_list in warnings.items():
    if not warning_list:
        continue
    if len(list(filter(lambda i : i != None, warning_list))) == 0:
        continue
    print(filepath + " warnings:")
    for warning in warning_list:
        if warning != None:
            print("  - " + warning)

if (args_processed == 0):
    print("Usage: %s [<path-to-java-file> | <path-to-pom> | <path-to-directory>]+")
    print("Converts files from v5.0.x API to v6.0.x API.  NOTE:  Overwrites files. This conversion is not a guaranteed fix for all issues and may generate bad code. Please review all changes before committing.")
    print("   <path-to-java-file>    :  relative or absolute path to java file to convert")
    print("   <path-to-pom>          :  relative or absolute path to pom.xml file to convert")
    print("   <path-to-directory>    :  relative or absolute path to a directory with .java or pom.xml files to convert")
