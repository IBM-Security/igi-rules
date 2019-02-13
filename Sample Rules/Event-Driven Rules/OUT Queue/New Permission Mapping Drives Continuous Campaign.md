# New Permission Mapping Drives Continuous Campaign

## Description
As demonstrated in the other examples, we can dynamically put user entitlements into a continuous certification campaign.

In this case we have an Add Permission rule (Live Events/OUT/PERMISSION_ADD/Intercept Permission and Certify User). It may be that you want any added permission to feed into a continuous certification campaign, irrespective of whether it comes from an access request in the Service Center, driven by an admin in the Admin Console, or internally generated. The OUT queue is the only common point for these different actions. The code is very similar to the code in the other examples.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.PwdCfgBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.UserErcBean
import com.engiweb.profilemanager.common.bean.event.EventBean
import com.engiweb.profilemanager.common.bean.event.EventInBean
import com.engiweb.profilemanager.common.bean.event.EventOutBean
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```

```java
import com.engiweb.pm.web.bean.AbstractBean
import com.engiweb.pm.entity.BeanList
import com.engiweb.pm.entity.Paging
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.crossideas.certification.common.bean.TemplateBean
import com.crossideas.certification.backend.dao.TemplateDAO
import com.crossideas.certification.common.AttestationRes
import com.crossideas.certification.common.enumeration.AttestationTypes
```


## Rule Code
The code is:
```java
when
  userBean : UserBean( )
  orgUnitBean : OrgUnitBean( )
  eventOut : EventOutBean( )
then
// [ V1.0 - 2015-11-05 ] by LL
  BeanList entitlements = UserAction.findJobRoles(sql, userBean);

  TemplateDAO templateDAO = new TemplateDAO(logger);
  templateDAO.setDAO(sql);

  TemplateBean templateBean = new TemplateBean();
  templateBean.setName("CertifyNewHiresDataset");

  String permissionToCheck = eventOut.getValore1();

  BeanList blTemplateBean = templateDAO.find(templateBean, new Paging(4));

  if (permissionToCheck !=null &&
    permissionToCheck.equalsIgnoreCase("dummyDemoPermission")) {

    if(blTemplateBean.size()==0){
      throw new Exception("Template does not exists");
    }
    templateBean = (TemplateBean) blTemplateBean.get(0);

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
  }

```
