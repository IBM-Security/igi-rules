# Create Custom Attributes and Use them in Data Mapping Rules

## Description
The following example demonstrates the creation of a custom variable, which can be used in visual mapping views like any of the IGI attributes. For the scope of the rule, the custom variable name is "CUSTOM_DESC".

This is a pre-mapping rule.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The following imports are needed:
```java
import com.engiweb.logger.impl.Log4JImpl;
import com.engiweb.pm.entity.BeanList;
import com.engiweb.profilemanager.backend.dao.db.SQLH;
import com.engiweb.profilemanager.common.bean.AccountBean;
import com.engiweb.profilemanager.common.bean.Block;
import com.engiweb.profilemanager.common.enumerations.LockType;
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean;
import com.engiweb.profilemanager.common.bean.ExternalInfo;
import com.engiweb.profilemanager.common.bean.OrgUnitBean;
import com.engiweb.profilemanager.common.bean.OrgUnitErcBean;
import com.engiweb.profilemanager.common.bean.PwdCfgBean;
import com.engiweb.profilemanager.common.bean.UserBean;
import com.engiweb.profilemanager.common.bean.UserErcBean;
import com.engiweb.profilemanager.common.bean.event.EventBean;
import com.engiweb.profilemanager.common.bean.event.EventInBean;
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction;
import com.engiweb.profilemanager.common.ruleengine.action.UserAction;
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction;
import com.engiweb.profilemanager.common.ruleengine.action.JobRoleAction;
import com.engiweb.profilemanager.backend.business.direct.AccountDirect;
import com.engiweb.toolkit.common.DBMSException;

global com.engiweb.profilemanager.backend.dao.db.SQLH sql;
global com.engiweb.logger.impl.Log4JImpl logger;
```
```java
import com.crossideas.ideasconnector.core.databean.DataBean;
import com.crossideas.ideasconnector.core.databean.Event;
import com.crossideas.ideasconnector.drivers.ideas.IdeasDriver;
import java.util.ArrayList;
```


## Rule Code
The code is:
```java
when
    event : Event(  )  
    srcDriver : IdeasDriver(  )
then
Long userErcId = (Long) event.getBean().getCurrentAttribute("USER_ERC_ID");

  ArrayList attributes = new ArrayList();
  attributes.add("GIVEN_NAME");
  attributes.add("SURNAME");
  attributes.add("EMAIL");

  DataBean queryBackFilter = new DataBean("USER");
  queryBackFilter.setCurrentAttributeValue("ID", userErcId);

  DataBean res = srcDriver.readObject(queryBackFilter, attributes);
  if (res == null) {
    throw new Exception("User not found");
  }

  Object GIVEN_NAME = res.getCurrentAttribute("GIVEN_NAME");
  Object SURNAME = res.getCurrentAttribute("SURNAME");
  Object EMAIL = res.getCurrentAttribute("EMAIL");

  event.setEventAttribute("GIVEN_NAME", GIVEN_NAME);
  event.setEventAttribute("SURNAME", SURNAME);
  event.setEventAttribute("EMAIL", EMAIL);

  DataBean eventBean = event.getBean();
  eventBean.setCurrentAttributeValue("CUSTOM_DESC", "Test Description" + " - " + GIVEN_NAME + " - " + SURNAME + " - " + EMAIL);

```
