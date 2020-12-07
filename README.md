# IBM ODM DC XOM Extractor

![GitHub last commit](https://img.shields.io/github/last-commit/ODMDev/odm-tools-dc-xom-extractor)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Introduction
==============
Since 8.8.1, you can  benefit from the new "XOM deployment in Decision Center" feature, as explained here:
https://www.ibm.com/support/knowledgecenter/SSQP76_8.8.1/com.ibm.odm.dcenter.deploy/topics/con_deploy_xom.html

If you choose to do so, Decision Center includes both the XOM and the RuleApp upon deployment.

If you choose to implement an offline deployment strategy, driven by the API calls, you need a convenient way to access the XOM as well as the RuleApp.

Generating a RuleApp on disk, for example, to be able to publish it to a binary artifact repository such as Nexus, Artifactory or Code Station, can easily be achieved through the `IlrDeploymentFacility` of Decision Center, by using the `deployDSRuleAppArchive` as documented here:
https://www.ibm.com/support/knowledgecenter/SSQP76_8.8.1/com.ibm.odm.dcenter.ref.dc/html/api/html/ilog/rules/teamserver/model/IlrDeploymentFacility.html
and as demonstrated in the [odm-dc-tools-ruleapp-extractor](https://github.com/ODMDev/odm-tools-dc-ruleapp-extractor) sample.

However, downloading a copy of the XOM that was used to generate said RuleApp requires using several APIs in sequence. This asset shows how to achieve this.

Moreover, this asset demonstrates how to upload a XOM, so that you can replace the existing one by a version you would have compiled with your toolchain, so that you are sure that the rule artifacts get generated with the exact same xom that was produced by your compilation chain.


Software Prerequisites
========================
IBM Operational Decision Manager, including Decision Center

Version(s) Supported
======================
IBM ODM 8.8.1 (and later)

Usage Instructions
===================
Clone the git repository locally.

In the `build.xml` file at the root, set the value of the `teamserver.home` property to the location where you have installed Decision Center.

Then run:

`ant usage` to discover the command line argument

`ant build` to compile the java class

`ant [required params] download` to execute the extraction

`ant [required params] upload` to execute the upload

Third Parties
====================
- [Apache Commons CLI 1.3](https://commons.apache.org/proper/commons-cli/index.html)

# Notice
© Copyright IBM Corporation 2020.

# License
```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````
