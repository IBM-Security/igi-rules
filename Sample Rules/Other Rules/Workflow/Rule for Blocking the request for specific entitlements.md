# Workflow Rule for Blocking the request for specific entitlements

## Description
This rule came from a customer requirement where there was a need to flag some entitlements as blocked and not allow users to request them. This approach is an alternative to setting up visibility settings for entitlements against organizational units (or other attribute hierarchy groups).

To flag entitlements as blocked, one of the entitlement attributes (in this case “Tolerance”) is set to the string “BLOCK”. The rule is assigned as a pre-action.

This is a *Pre-Action* rule.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
global com.engiweb.pm.dao.db.DAO sql
global com.engiweb.logger.impl.Log4JImpl logger
```

```java
import com.crossideas.ap.common.bean.SwimRequestBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.crossideas.ap.backend.util.SwimConverter2PM
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.engiweb.rpd.backend.business.direct.CheckRiskDirect
import com.engiweb.rpd.common.risk.RiskInfo
import java.util.List
import com.engiweb.rpd.common.bean.RiskBean
import com.crossideas.ap.common.bean.SwimEntitlementBean
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import com.crossideas.ap.backend.dao.SwimRequestDAO
import com.crossideas.ap.backend.util.enums.RequestStatus
import com.engiweb.rpd.backend.dao.risk.CheckRiskDAO
import com.engiweb.rpd.common.risk.RiskInfoFull
import com.engiweb.rpd.backend.dao.risk.RiskDAO
import com.crossideas.ap.backend.dao.context.auth.AuthorizeIncompatibility
```

## Rule Code
The code is:
```java
when
  request : SwimRequestBean( )
then
// Revoke request on tolerance Risk Level

  logger.info("Request n. " + request.getId());

  SwimRequestDAO swr = new SwimRequestDAO(logger);
  swr.setDAO(sql);
  request = swr.findRequestDetail(request);

  Integer reqStatus = request.getReqstatus();
  if (reqStatus != null && reqStatus.equals(RequestStatus.ESCALATION.getCode())) {

    List<SwimEntitlementBean> listToRem = request.getRolesToRemove();
    List<SwimEntitlementBean> listToAdd = request.getRolesToAdd();

    logger.info("listToRem " + listToRem.size());
    logger.info("listToAdd " + listToAdd.size());

    SwimConverter2PM converter = new SwimConverter2PM();
    BeanList<EntitlementBean> entToAdd = converter.listSwim2PM(listToAdd);
    BeanList<EntitlementBean> entToRem = converter.listSwim2PM(listToRem);

    logger.info("EntToRem " + entToRem.size());
    logger.info("EntToAdd " + entToAdd.size());

    Long beneficiaryId = request.getBeneficiary_id();
    UserBean userBean = new UserBean();
    userBean.setId(beneficiaryId);

    CheckRiskDAO riskDao = new CheckRiskDAO(logger);
    riskDao.setDAO(sql);

    RiskInfoFull riskInfoFull = riskDao.checkUserFull(userBean, entToAdd, entToRem, null, false);
    List<RiskBean> lRiskBean = riskInfoFull.getAfter().getAllRisk();

    if (lRiskBean == null || lRiskBean.isEmpty()) {
      logger.info("No risk, skip! ");
      return;
    }

    for (RiskBean riskBean : lRiskBean) {

      RiskDAO lrb = new RiskDAO(logger);
      lrb.setDAO(sql);

      riskBean = (RiskBean) lrb.checkAB(riskBean, true);

      String currentTolerance = riskBean.getTolerance();

      logger.info("Current risk: " + riskBean.getName());
      logger.info("Current tolerance: " + currentTolerance);

      if (currentTolerance != null && currentTolerance.equals("BLOCK")) {
        logger.info("BLOCK FOUND!");

        UserBean userApplicantBean = new UserBean();
        userApplicantBean.setCode(request.getApplicant_userid());

        AuthorizeIncompatibility authDao = new AuthorizeIncompatibility(logger);

        authDao.setDAO(sql);
        authDao.initialize("IDEAS", request.getEscalationPermission(), userApplicantBean);
        authDao.authorize(request, false);

        return;
      }
    }
  }
```
