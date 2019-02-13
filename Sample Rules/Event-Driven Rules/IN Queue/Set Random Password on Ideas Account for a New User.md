# Set Random Password for Ideas Account on New User

## Description
When any new person is defined in IGI, they are automatically granted an Ideas account (their
account to access the IGI Service Center). The following code will generate a random password and
assign it to the Ideas account.

It demonstrates some of the UserActions as well as working with the Ideas account for a user.

This would be on one of the IN user event flows.

## Package Imports
There imports needed for this rule:

```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.enumerations.LockType
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.OrgUnitErcBean
import com.engiweb.profilemanager.common.bean.PwdCfgBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.UserErcBean
import com.engiweb.profilemanager.common.bean.event.EventBean
import com.engiweb.profilemanager.common.bean.event.EventInBean
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction
import com.engiweb.profilemanager.common.ruleengine.action.JobRoleAction
import com.engiweb.profilemanager.backend.business.direct.AccountDirect
import com.engiweb.toolkit.common.DBMSException

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```

## Rule Code
The code is:
```java
when
  user : UserBean( )
  orgUnit : OrgUnitBean( )
  extInfo : ExternalInfo( )
then
  // [ V1.1 - 2014-05-26 ]
  // Gets account cfg
  PwdCfgBean ideasAccountCfg = new PwdCfgBean();

  // The account type (configuration) is the Ideas account (i.e. 1L)
  ideasAccountCfg.setId(1L);

  // Generate a random password that complies with the strength rules for this account type
  String pwd = UserAction.getRandomPwd(sql, ideasAccountCfg);

  // Set an empty account bean of type Ideas and use it to search for the account for this user
  AccountBean userAccount = new AccountBean();
  userAccount.setPwdcfg_id(ideasAccountCfg.getId());
  BeanList accounts = UserAction.findAccount(sql, user, userAccount);

  // Get the found account (there should only be one) and set the new password on it
  userAccount = (AccountBean) accounts.get(0);
  UserAction.changePwd(sql, userAccount, pwd);
  logger.debug("Random password set");
```
