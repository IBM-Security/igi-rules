# Reset Ideas Account Passwords for All Users

## Description
This is a trivial example of a rule that may be useful in a PoC or demo scenario where you want to be able to easily reset all IGI account passwords back to a known value.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.engiweb.profilemanager.common.bean.event.EntStateBean
import com.engiweb.profilemanager.common.bean.event.EventTargetBean
import com.engiweb.profilemanager.common.bean.rule.SyncStateBean
import com.engiweb.profilemanager.common.ruleengine.action.EntitlementAction
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.toolkit.common.DBMSException
import com.engiweb.profilemanager.common.bean.AccountAttrValueList
import com.engiweb.profilemanager.common.interfaces.IAccountDirect
import com.engiweb.toolkit.interfaces.JndiNames
import com.engiweb.profilemanager.common.bean.targetattr.PwdManagementAttrValBean
import common.direct.DirectFactory
import com.crossideas.ideasconnector.common.enums.EventOperationType
import java.util.Random

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```

```java
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._AccountAction
import com.engiweb.pm.entity.Paging
```

## Rule Code
The code is:
```java
when
  eval( true )
then
  // Default Password
  String pwd = "Passw0rd!";

  // Ideas Account
  AccountBean ideasAccountBean = new AccountBean();
  ideasAccountBean.setPwdcfg_id(1L);

  // Max 10000 Account
  Paging paging = new Paging(10000);

  BeanList<AccountBean> res = _AccountAction.findAccount(sql, ideasAccountBean, paging);
  for (AccountBean accountBean : res) {
    _AccountAction.changePwd(sql, "", pwd, accountBean);
  }
```
