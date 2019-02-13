# IGI Rules

This GitHub contains the IBM Security Identity Governance and Intelligence (IGI) Rules Guide and sample rules. Rules are how we extend IGIs functionality, but can be complex and a topic that is not well documented. This GitHub attempts to address that by providing samples of rules that you can use in your own IGI deployment.

## GitHub Structure
The **Rules Guide** (*IGI Rules Guide v03.pdf*) is stored in this folder. This document should be the first thing you read to start working with Rules (it currently provides a better introduction to rules that the official product documentation).

There is also a document on getting access to the Eclipse Rules plugin.

There are two major folders of rules:
* **Sample Rules** - a collection of rules stored in text documents (.md or markdown files)
* **JavaRules** - a collection of rules in complete .java files allowing them to be used in the Eclipse rules plugin

Within the *Sample Rules* folder, there are sub-folders for different types of rules:

* Enterprise Connector Mapping Rules - Sample rules for attribute mapping in Enterprise Connectors. 
* Event-Driven Rules - Sample rules to be used in the event flow against the queues, and
* Other Rules - Sample rules for other uses, like workflow, account management and certification campaigns.

Within the *JavaRules* folder, there are files that correspond to what we typically use in the Eclipse plugin. You can view these files in isolation OR we got them linked up in the individual .md files as well where you can read-up additional description around the rules.

Some of the rules provided here ship with the product and others have been built for specific customer needs as part of a Proof of Concept or deployment. The code samples are provided as-is and are not supported by IBM. See the License information below.

## Sample Rules Files
The sample rules are stored in .md files (markdown text files) and are structured with the following sections:
* *Description* - Description of the rule.
* *Package Imports* - Any additional package imports needed.
* *Rule Code* - The sample rule code itself.
* *Java Code* - This is an optional section of the document. If you happen to find this section in the document, you would find a link to the corresponding Java Code that could be imported directly in the Rule Engine Toolkit and used from there.

The sample rule files follow a naming convention; **short description**.md. For example:
* *Set Random Password on Ideas Account for a New User.md* - A rule to run on the IN queue to set a random password on an Ideas account for a new userAccount
* *New Permission Assignment Drives Continuous Campaign.md* - A rule to capture a change in the permission assignment of a user and put the corresponding user in the continuous campaign dataset.
* *Pass Arguments Between Rules in a Flow.md* - A rule that purely demonstrates how we could pass arguments from one rule to the other, of course in the same flow.

The rules are organized under different folders, under **Sample Rules**, each of them signifying a different area of rules

* **Enterprise Connector Mapping Rules** - Enterprise Connector mapping rules.
* **Event-Driven Rules** - Rules on the IN, OUT & Target queues. These rules are scattered across folders within:
    * *IN Queue* - Rules on the IN event queue. This in turn 
    * *OUT Queue* - Rules on the OUT event queue.
    * *Target Queue* - Rules on the Target (TGT) event queue.
* **Other Rules** -  Rules around Account Management, Password Management, Workflow. These rules are scattered across folders within:
    * *Advanced* - Rules pertaining account management, certification campaigns et. al.
    * *Generic* - Basic rules that could be used agnostic of module.
    * *Hierarchy* - Rules pertaining hierarchy management.
    * *Workflow* - Rules pertaining the pre- and post-actions in Workflow.
 
Notes: 
1. If there are multi-step rules, they are put up under the same folder, with a proper call out on the Step 1, Step 2 et. al.
2. The import statements are arranged in 2 separate sections. The first set of import statements in a given .md file are the ones that are available by default. The second set of import statements in that same .md file are the ones that need to be explicitly specified in the Rule Imports, whenever you are writing that rule. At times, there would just be one section and if that's the case, likely that no further imports beyond the defaults would be needed.



## Rules in the Product Documentation
The following sections of the IGI Knowledge Center contain rule information or examples (based on the 5.2.5 docs).

### General References to Rules
* [Rules Introduction](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/administering/cpt/cpt_ac_rules_introduction.html#cpt_ac_rules_introduction)
* [Intro to AGC - Rules](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/CrossIdeas_Topics/AGC/rulez.html)
* [Introduction to Rules Engine](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/CrossIdeas_Topics/RULES_ENGINE/RUD_Introduction_to_Rule_Engine.html)
* [Introduction to Process Designer - Rules](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/CrossIdeas_Topics/PD/GestioneRules.html)
* [Examples of Rules](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/CrossIdeas_Topics/II/Examples_of_Rules.html)

### Event-driven Rules
* [Adding the rule for target account password synchronization](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/administering/tsk/tsk_ac_password_sync_rule.html)
* [Rules overview - some OU examples](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/reference/cpt/cpt_rules_reference_overview.html)
* [Enabling a flow of rules to be deferred](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/administering/tsk/tsk_ac_enable_a_flow_of_rules_to_be_deferred.html)
* [Importing account attributes](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/administering/ref/ui_account_attributes_import.html)
* [Manual Fulfillment configuration](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/CrossIdeas_Topics/AGC/GestioneCfgPwd_manualfulfillment.html)

### Enterprise Connector Mapping Rules
* [Channels and Rules](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/CrossIdeas_Topics/ECONN/Channels_Rules.html#Channels_Rules__Pre)
* [Managing pre-mapping rules, post-mapping rules, or response rules for a connector](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/administering/tsk/tsk_ac_connectors_premap_postmap.html)
* [Pre-mapping and post-mapping rule examples](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/CrossIdeas_Topics/ECONN/prepost_mapping_rule_examples.html)

### Other Rules
* [Creating custom password rules](https://www.ibm.com/support/knowledgecenter/SSGHJR_5.2.5/com.ibm.igi.doc/administering/tsk/tsk_ac_password_create_custom_rules.html)

## License
See [LICENSE](LICENSE).
