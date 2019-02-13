# Emulate Branching in Workflow

## Description
This example shows how a workflow step can be skipped based on logic in a rule. It uses the Visibility Violation flag that can be set on an Entitlement-Hierarchy mapping. If the VV is set, it will force a second level of approval. If there is no VV flag set, it will SKIP (automatically approve) the next approval step in the workflow to avoid a redundant approval step.

This is a *Post-Action* rule.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
global com.engiweb.pm.dao.db.DAO sql
global com.engiweb.logger.impl.Log4JImpl logger
```

```java
import com.engiweb.profilemanager.common.bean.event.EventOutBean
import com.engiweb.toolkit.common.enums.IdeasApplications
import com.crossideas.ap.common.bean.SwimRequestBean
import com.crossideas.ap.common.ruleengine.action.RequestFindRule
import com.crossideas.ap.backend.util.enums.RequestStatus
import java.util.List
import com.crossideas.ap.common.bean.SwimEntitlementBean
import com.crossideas.ap.backend.util.enums.RequestType
import com.crossideas.ap.common.res.SwimConstants
import com.crossideas.ap.common.ruleengine.action.RequestAuthorizationRule
```

## Rule Code
The code is:
```java
when
  eventOut : EventOutBean( )
then
// [ V1.1 - 2015-11-16 ]

  // Who Authorize the skip step
  String operator = "DPeak";

  // Alias for auth step
  String authAlias = "RG";
  Long id;

  try {
    String cOperation = eventOut.getCodiceOperazione();
    id = Long.valueOf(cOperation.substring((IdeasApplications.ACCESSREQUESTS.getAcronym() + "_").length()));
  } catch (Exception e) {
    return;
  }

  SwimRequestBean swimReqBean = new SwimRequestBean();
  swimReqBean.setId(id);
  swimReqBean = RequestFindRule.findRequestDetail(sql, swimReqBean);

  RequestStatus requestStatus = RequestStatus.get(swimReqBean.getReqstatus());

  switch (requestStatus) {
    case AUTHORIZABLE:
      break;
    default:
    logger.error("Skip Request " + swimReqBean.getId() + ", status is " + requestStatus);
    return;
  }

  List<SwimEntitlementBean> roles;
  RequestType reqType = RequestType.get(swimReqBean.getReqtype());

  switch (reqType) {
    case ROLE_ASSIGN:
      roles = swimReqBean.getRolesToAdd();
      break;
    case ROLE_REMOVAL:
      roles = swimReqBean.getRolesToRemove();
      break;
    case ROLE_RENEWAL:
      roles = swimReqBean.getRolesToUpdate();
      break;
    default:
      logger.info("Skip Request " + swimReqBean.getId() + ", type is " + reqType);
      return;
  }

  // Check if the roles list includes a role in VV
  for (SwimEntitlementBean swimEntBean : roles) {
    Long vv = swimEntBean.getVisibilityViolation();

    if (vv != null && vv.intValue() == SwimConstants.VISIBILITY_VIOLATION_ON) {
      logger.error("Request " + swimReqBean.getId() + " contains Role " + swimEntBean.getNameI18n() + " in VV, follow next approval");
      // Found some role in VV exit
      return;
    }
  }

  String nextStep = "AUTH/Auth ROwner$Access Request JBJ+Jump [Personal]";
  logger.error("Set next Step: " + nextStep);

  // No VV found, approve current request
  SwimRequestBean swimReqBean2 = RequestAuthorizationRule.authorizeRequest(sql, swimReqBean, authAlias, nextStep, operator);
  logger.error("Request " + swimReqBean2.getId() + "has been authorized, new staus is : " + RequestStatus.get(swimReqBean2.getReqstatus()));

```

Note this rule hard-codes an auto approver as "DPeak" and "next activity" / "permission name" to "AUTH/Auth ROwner$Access Request JBJ+Jump [Personal]".

