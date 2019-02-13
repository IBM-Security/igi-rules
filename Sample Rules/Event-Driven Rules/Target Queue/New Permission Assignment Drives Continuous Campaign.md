# New Permission Assignments Drive a Continuous Campaign

## Description
This rule is similar to the IN-Q rule relating to a user move driving a continuous certification campaign. The rule will capture a change to permission assignment and put this in the continuous campaign dataset.

This rule is for Live Events/TARGET/PERMISSION_ADD. Note that is using older DAO methods that shouldn't be used for new rules.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports needed for this rule:

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

```java
import com.crossideas.certification.common.bean.TemplateBean
import com.crossideas.certification.backend.dao.TemplateDAO
import com.engiweb.pm.entity.Paging
import com.engiweb.pm.web.bean.AbstractBean
import com.crossideas.certification.common.AttestationRes
import com.crossideas.certification.common.enumeration.AttestationTypes
```


## Rule Code
The code is:
```
when
  userBean : UserBean( )
  event : EventTargetBean( )
  syncBean : SyncStateBean( )
then

  String targetApplicationContentToRecertify = "PadLock";
  String templateNametoUse = "Continuous Campaign User Entitlement";

  // Check if we have the right Application
  if (!event.getTarget().equalsIgnoreCase(targetApplicationContentToRecertify) ) {
    logger.info("Target is not " + targetApplicationContentToRecertify + ", skip");
    return;
  }

  // Create the Template Bean
  TemplateBean templateBean = new TemplateBean();
  templateBean.setName(templateNametoUse);

  // Create the DAO to find the DataSet
  TemplateDAO templateDAO = new TemplateDAO(logger);
  templateDAO.setDAO(sql);

  // Find the requested template to use
  BeanList blTemplateBean = templateDAO.find(templateBean, new Paging(4));
  if(blTemplateBean.size()==0){
    throw new Exception("Template does not exists");
  }

  // Link to the found Template
  templateBean = (TemplateBean) blTemplateBean.get(0);

  // Create the Bean List to add to the Continuous Campaign
  AbstractBean[] element = new AbstractBean[2];
  element[0] = userBean;
  element[1] = syncBean.getProfile();
  BeanList listBean = new BeanList();
  listBean.add(element);

  // Add To the Campaign
  if (listBean.size() > 0) {
    templateDAO.addEntity(listBean, AttestationRes.TEMPLATE_ENTITY_USERENT, templateBean, AttestationTypes.PERSON_ENTITLEMENT.getValue());
  }
```

Note the rule is hardcoded to apply to the "PadLock" application and the "Continuous Campaign User Entitlement" campaign dataset.
