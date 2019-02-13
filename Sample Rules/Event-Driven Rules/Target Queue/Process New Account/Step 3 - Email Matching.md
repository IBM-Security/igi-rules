# Process a New Account from a Target - Step 3 - Email Matching

## Description
Whenever a reconciliation is run against a target system, whether it is the first or subsequent, you may get accounts sent to IGI that IGI wasn’t aware of, along with any account-permission mappings. As IGI needs to associate people with permissions, it needs to match the new account with an existing user or create it in IGI as an unmatched account. There will normally be some business logic to try to find a matching user, such as looking up users by a common userid, email address or name found in the account attributes.

This set of examples is based on the supplied code assigned to the Live Events/TARGET/ACCOUNT_CREATE event and has the following flow:
1. Check if account is already matched – Check to see if IGI already has an account-user match for this account.
2. Create Account [Userid Matching] – Attempt to find a user by userid (account id) and create the account (or update if it’s already there as an unmatched account).
3. Create Account [Email Matching-] – Attempts to find a user by email address and create the account (or update if it’s already there as an unmatched account).
4. Create Account [Name-Surname Matching-] – Attempt to find a user by firstname and surname and create the account (or update if it’s already there as an unmatched account).
5. Create Account [Post Matching] – If the new account has fallen through all of the matching attempts without matching, go create an unmatched account in IGI for it.

```
Note: All the above rules are co-located in the folder: "Process New Account".
```

This example code is for **Step 3 - Email Matching**. This rule will attempt to match a user by the email for the account.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.logger.impl.Log4JImpl;
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.pm.entity.BeanList;
import com.engiweb.profilemanager.common.bean.AccountBean;
import com.engiweb.profilemanager.common.bean.Block;
import com.engiweb.profilemanager.common.bean.OrgUnitBean;
import com.engiweb.profilemanager.common.bean.UserBean;
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean;
import com.engiweb.profilemanager.common.bean.event.EntStateBean;
import com.engiweb.profilemanager.common.bean.event.EventTargetBean;
import com.engiweb.profilemanager.common.bean.rule.SyncStateBean;
import com.engiweb.profilemanager.common.ruleengine.action.EntitlementAction;
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction;
import com.engiweb.profilemanager.common.ruleengine.action.UserAction;
import com.engiweb.toolkit.common.DBMSException;
import com.engiweb.profilemanager.common.bean.AccountAttrValueList
import com.engiweb.profilemanager.common.interfaces.IAccountDirect
import com.engiweb.toolkit.interfaces.JndiNames
import com.engiweb.profilemanager.common.bean.targetattr.PwdManagementAttrValBean
import common.direct.DirectFactory

global com.engiweb.profilemanager.backend.dao.db.SQLH sql;
global com.engiweb.logger.impl.Log4JImpl logger;
```

## Rule Code
The code is:
```java
when
    event : EventTargetBean(  )  
    accountBean : AccountBean(  )
then
// [ V1.5 - 2014-05-26 ]

  // exit if already matched by a customized rule
  if (accountBean.getPerson_id() != null) {
     return;
  }

  // Gets the email
  String email = event.getEmail();
  if (email == null) {
     logger.info("Empty email, matching on email not applicable... exit!");
     return;
  }

  // Look for the User into IDEAS
  UserBean userFilter = new UserBean();
  userFilter.setEmail(email);
  BeanList ul = UserAction.find(sql, userFilter);

  boolean found = !ul.isEmpty();
  if (found) {
    logger.info("Account Matched by email!");

    userFilter = (UserBean) ul.get(0);

    accountBean.setPerson_id(userFilter.getId());

    String eventUserCode = event.getCode();
    if (eventUserCode != null) {
      accountBean.setCode(eventUserCode);
    }

    if (accountBean.getId() != null) {
      //The account already exists but it is unmatched/orphan
      UserAction.updateAccount(sql, accountBean);
      logger.info("Account exists but it is unmatched/orphan");
    } else {
      UserAction.addAccount(sql, accountBean);
      logger.info("Account : " + accountBean.getCode() + " created!");
    }
  }
```
