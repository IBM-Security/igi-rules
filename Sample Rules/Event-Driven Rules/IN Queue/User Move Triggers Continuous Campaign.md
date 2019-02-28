# User Move Triggers Continuous Certification Campaign

## Description
IGI has the concept of continuous certification campaigns. Rather than running a campaign for a fixed period of time on a static dataset, a campaign can be fed changes continuously that a reviewer must review.

In this example, a user move event (i.e. the user has changed OU) will put the entitlements for that user into the certification dataset for a continuous campaign. The term "template" means certification dataset. The rule will find the named certification dataset (templateDAO.find), get the entitlements for that user (UserAction.findJobRoles), build a list of entitlements (sets of user:entitlement elements) and eventually writes these to the certification dataset (templateDAO.addEntity).

This is one of the supplied sample rules with IGI. It runs on the IN queue for the user modify/move event.

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
```java
when
  userBean : UserBean( )
  orgUnitBean : OrgUnitBean( )
then
// [ V1.3 - 2015-04-22 ]

  // Templatename --> DefaultEmptyTemplate included in User Transfer Campaign
  String tName = "DefaultEmptyTemplate";

  TemplateBean templateBean = new TemplateBean();
  templateBean.setName(tName);
  TemplateDAO templateDAO = new TemplateDAO(logger);
  templateDAO.setDAO(sql);
  BeanList blTemplateBean = templateDAO.find(templateBean, new Paging(4));

  if (blTemplateBean.size()==0) {
    throw new Exception("Template does not exists!");
  }
  templateBean = (TemplateBean) blTemplateBean.get(0);

  BeanList entitlements = UserAction.findJobRoles(sql, userBean);

  BeanList listBean = new BeanList();
  for (int i = 0; i < entitlements.size(); i++) {
    AbstractBean[] element = new AbstractBean[2];
    element[0] = userBean;
    element[1] = (EntitlementBean) entitlements.get(i);
    listBean.add(element);
  }

  if (listBean.size() > 0) {
    templateDAO.addEntity(listBean, AttestationRes.TEMPLATE_ENTITY_USERENT, templateBean, AttestationTypes.PERSON_ENTITLEMENT.getValue());
  }
```

Note that, this sample contains a hardcoded campaign dataset of "DefaultEmptyTemplate". You can definitely change this to match up to your dataset.
