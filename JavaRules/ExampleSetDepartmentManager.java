package rules.system.in.user_add;

import java.util.Set;

import com.engiweb.logger.impl.Log4JImpl;
import com.engiweb.pm.entity.BeanList;
import com.engiweb.profilemanager.backend.dao.db.SQLH;
import com.engiweb.profilemanager.common.bean.ExternalInfo;
import com.engiweb.profilemanager.common.bean.OrgUnitBean;
import com.engiweb.profilemanager.common.bean.UserBean;
import com.engiweb.profilemanager.common.bean.UserErcBean;
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean;
import com.engiweb.profilemanager.common.bean.event.EventInBean;
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction;
import com.engiweb.profilemanager.common.ruleengine.action.UserAction;
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction;

public class ExampleSetDepartmentManager {

    public static void run(Set<Object> inputBeans) throws Exception {

        Log4JImpl logger = (Log4JImpl) getObjectByType(inputBeans, Log4JImpl.class);
        SQLH sql = (SQLH) getObjectByType(inputBeans, SQLH.class);
        ExternalInfo extInfoBean = (ExternalInfo) getObjectByType(inputBeans, ExternalInfo.class);
        UserErcBean userErcBean = (UserErcBean) getObjectByType(inputBeans, UserErcBean.class);
        EventInBean event = (EventInBean) getObjectByType(inputBeans, EventInBean.class);
        UserBean userBean = (UserBean) getObjectByType(inputBeans, UserBean.class);
        OrgUnitBean orgUnitBean = (OrgUnitBean) getObjectByType(inputBeans, OrgUnitBean.class);

        /*
        ----- Rule Name ----
        [EXAMPLE] SET DEPARTMENT MANAGER
        ----- Rule Desc ----	
        [ V1.1 - 2014-05-26 ] - USER_ERC.ATTR2 = Y/N
        --------------------
        */

        try {

            // ---- Rule body ----

            // [ V1.1 - 2014-05-26 ]

            String MANAGER_ROLE_NAME = "Department Manager";
            String IS_MANAGER_ATTR = "ATTR2"; // Y/N

            String isManager = (String) userErcBean.getAttribute(IS_MANAGER_ATTR);

            if (isManager != null) {

                if (isManager.toUpperCase().equals("Y")) {

                    // Get manager role object
                    EntitlementBean role = UtilAction.findEntitlementByName(sql, MANAGER_ROLE_NAME);
                    if (role == null) {
                        throw new Exception("Role with name " + MANAGER_ROLE_NAME + " not found!");
                    }

                    BeanList roles = new BeanList();
                    roles.add(role);

                    OrgUnitAction.addRoles(sql, orgUnitBean, roles, false);
                    UserAction.addRole(sql, userBean, orgUnitBean, roles, null, null, false, false);
                    UtilAction.addResourcesToEmployment(sql, userBean, role, orgUnitBean);
                }
            }

            // -------------------

        } catch (Exception e) {
            throw e;
        }

    }

    private static Object getObjectByType(Set<Object> inputBeans, Class type) {

        for (Object bean : inputBeans) {

            if (type.isInstance(bean)) {
                return bean;
            }
        }
        return null;

    }

}
