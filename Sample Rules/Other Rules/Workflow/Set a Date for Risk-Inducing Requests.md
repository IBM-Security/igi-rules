# Set End Date for Risk-Inducing Request

## Description
This rule will look at a request for an entitlement, and if it generates a risk, will force an end-date on
the entitlement.

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
```

## Rule Code
The code is:
```java
when
  requestBeanEnv : SwimRequestBean( )
then
  logger.info("!!! Begin CheckRequestIncomp...: " );

  // Beneficiary
  String beneficiaryUserid = requestBeanEnv.getBeneficiary_userid();

  // We need the UserBean
  UserBean user = new UserBean();
  user.setCode(requestBeanEnv.getBeneficiary_userid());

  BeanList<UserBean> users = UserAction.find(sql, user);
  user = users.get(0);
  logger.info("!!! User Considered: " + user.getCode());

  // We need the roles list to check risk
  SwimConverter2PM converter2PM = new SwimConverter2PM();
  BeanList<EntitlementBean> ee = new BeanList<EntitlementBean>();

  for (SwimEntitlementBean tmpBean : requestBeanEnv.getRolesToAdd()) {
    ee.add(converter2PM.SwimEntBean2EntBean(tmpBean));
  }

  logger.info("!!! Before Risk Check..." );

  // Check risk information
  CheckRiskDirect riskD = new CheckRiskDirect();
  RiskInfo rI = riskD.checkUser(user, ee, null, null, null, sql);

  if (rI.getRiskNumber() > 0) {
    List<RiskBean> lRiskBean = rI.getAllRisk();

    for (RiskBean riskBean : lRiskBean) {
      logger.info("!!! Risk Name --> " + riskBean.getName());

      switch (riskBean.getRiskLevel().intValue()) {
        case RiskInfo.LOW:
          logger.info("!!! RISK LEVEL --> LOW");
          break;
        case RiskInfo.MEDIUM:
          logger.info("!!! RISK LEVEL --> MEDIUM ");
          break;
        case RiskInfo.HIGH:
          logger.info("!!! RISK LEVEL --> HIGH ");
          break;
        default:
      }// switch

      String riskType = riskBean.getRiskType_name();
      logger.info("!!! Risk Type --> " + riskType);

      if (riskType!=null && riskType.contains("SoD")) {
        for (SwimEntitlementBean tmpBean2 : requestBeanEnv.getRolesToAdd()) {
          logger.info("!!! Processing SWIMENT --> " + tmpBean2.getName());

          // Set new End Date
          Calendar currentTime = Calendar.getInstance();
          logger.info("!!! Current Time: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(currentTime.getTime()));
          currentTime.add(Calendar.DAY_OF_MONTH, 1);
          Date newEntDate = currentTime.getTime();
          tmpBean2.setEnddate(newEntDate);
          String stringDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(newEntDate);
          logger.info("!!! New End Date: " + stringDate);
        }
      }// if
    }// for

  } else {
    logger.info("!!! NO RISKS FOUND!" );
    return;
  }

  logger.info("!!! End CheckRequestIncomp");
```
