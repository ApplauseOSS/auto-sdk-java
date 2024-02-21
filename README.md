# Applause Java Automation SDK

The Applause Java SDK contains the bulk of the client side logic Applause Automation.  This SDK will serve as a library component distributed with the Applause Automation Java Template. 

This Library will be deployed via a self hosted maven repo, and will allow anyone to download the deployed library

## Building

Maven is supported for building

* Maven 
  * build `mvn clean install` 


## Documentation 

Detailed documentation on the use of the Applause Java Automation SDK can be found in the user guide.  

## Distribution 

This library is deployed to a maven distribution location listed below

### Maven 

The consuming package needs to specify the repository to pull the compiled library from and the version of that library to fetch.  This is done with the following maven code.

```xml
	<repositories>
		<repository>
			<id>applause-public-repo</id>
			<url>https://repo.applause.com/repository/public</url>
			<snapshots>
				<enabled>false</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</repository>
	</repositories>
```
The user can then specify the version of the SDK Library to import using the following:
```xml
        <dependency>
		<groupId>com.applause</groupId>
		<artifactId>auto-sdk-java</artifactId>
		<version>${com.applause.sdk.java.version}</version>
	</dependency>
```

## Technical Details

This section is for detailed technical information related to developing the framework.

#### Example
`mvn clean compile test -DsuiteFile=testng-unitSandbox.xml -DdriverConfig=local_chrome.json -DreportingEnabled=false -DuseLocalDrivers=true`

#### Testing

There's a large set of unit tests for this library.
  # Checking your environment.  To build/test this module require the following components be installed locally
  - Java Development Kit (JDK) 21 or latter
  - Maven 3.8.6 or latter

