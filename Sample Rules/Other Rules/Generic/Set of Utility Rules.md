# Set of Utility Rules

## Description
This is a compilation of a series of different Java Rules intended to be consumed in different individual Rules. The topics covered in the compilation are:
* Set Ideas Password to a specific value - *setIdeasPassword*
* Disable Orphan Accounts - *disableOrphanAccount*
* Match an Orphan / Unmatched Account - *matchAccount*
* Set the Expiry date of ideas Account based on the Contract End Date / Validity (Expiry Date) specified against the User Profile - *setIdeasAccountExpiryFromUser*
* Modify the date related metadata for an account. For e.g. update the last login time and the password change date in the Account Metadata - *SetDatesOnAccount*
* Update specific variables in the User specific DataBean - *setVariablesUser*
* Update specific variables in the Account specific DataBean - *setVariablesAccount*
* Send an email to the concerned on user termination - *sendEmailForTermination*

## Package Imports
The imports for this rule:
```java
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.crossideas.ideasconnector.core.databean.DataBean;
import com.crossideas.ideasconnector.core.databean.Event;
import com.engiweb.logger.impl.Log4JImpl;
import com.engiweb.pm.entity.BeanList;
import com.engiweb.pm.entity.Paging;
import com.engiweb.profilemanager.backend.dao.db.SQLH;
import com.engiweb.profilemanager.common.bean.AccountBean;
import com.engiweb.profilemanager.common.bean.Block;
import com.engiweb.profilemanager.common.bean.UserBean;
import com.engiweb.profilemanager.common.bean.UserErcBean;
import com.engiweb.profilemanager.common.bean.event.EventTargetBean;
import com.engiweb.profilemanager.common.enumerations.LockType;
import com.engiweb.profilemanager.common.ruleengine.action.UserAction;
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction;
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._AccountAction;
```

## Java Code
The list of methods to implement the above rules are documented in 
[Utility Rules](../../../JavaRules/UtilityRules.java).

