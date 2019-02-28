# Automation Recertification of  Entitlements assigned directly on Target Application

## Description
IGI has the concept of continuous certification campaigns. Rather than running a campaign for a fixed period of time on a static dataset, a campaign can be fed changes continuously that a reviewer must review.

This  use  case  shows  how  IGI  can  identify  permissions  assignments  outside  its  governance radar, essentially direct updates in  a  target application, and immediately react to that. To be precise, after a reconciliation from a target application, IGI can  detect  permissions  assigned  directly  in  the  target  application  and  can  automatically  send  these “unauthorized” entitlements in a certification campaign for the  manager or whoever is appropriate to validate the changes.  If  the decision is to revoke those permissions, the entitlements will be automatically revoked. There's a caveat though. If the reviewer decides to revoke the new permission, and ONLY if the application is integrated through a connector the revoke will be propagated all the way to the target application, removing the entitlement from the application account. Otherwise the entitlement removal will happen only in IGI.


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
import com.engiweb.profilemanager.common.bean.event.EventTargetBean
import com.engiweb.profilemanager.common.bean.rule.SyncStateBean
```



## Rule Code
The code is:
```java
when
	userBean : UserBean(  )  
	event : EventTargetBean(  )  
	syncBean : SyncStateBean(  )
then

	String targetApplicationContentToRecertify = "PadLock";
	String templateNametoUse = "Target Assignments Dataset";
	
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
	
	// Find the requisite template to use
	BeanList blTemplateBean = templateDAO.find(templateBean, new Paging(4));
	
	if(blTemplateBean.size()==0) {
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

Note: For the scope of this example, the application to be recertified is assumed to be PadLock and the dataset to be used for certification is assumed to be "Target Assignments Dataset". Tune this per your current configuration.
