# Generate a string to build a Hierarchy based on OU, Department and Title

## Description
Often a reporting structure is more complex than just a HR-based organizational structure. This example builds a hierarchy based on OU (where the OUs represent geographies), department and user title.

This rule is run for each user in the system.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.profilemanager.common.bean.UserExtInfoBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.crossideas.certification.common.bean.data.ResultBean
import java.util.ArrayList

global com.engiweb.pm.dao.db.DAO sql
global com.engiweb.logger.impl.Log4JImpl logger
```


## Rule Code
The code is:
```java
when
  userBean : UserBean( )
  userInfoList : ArrayList( )
  resultBean : ResultBean( )
then
  /* First attribute is geo / OU, followed by DepartmentCode (Attr3), followed by Title (Attr4) */
  String geo=null;
  String department=null;
  String title=null;

  for (int i=0;i<userInfoList.size();i++){
    UserExtInfoBean extInfoBean = (UserExtInfoBean)userInfoList.get(i);
    String name = extInfoBean.getName();
    String value = extInfoBean.getValue();

    if(name.equalsIgnoreCase("OU")&&value!=null){
       geo = value;
    }

    if(name.equalsIgnoreCase("department")&&value!=null){
       department= value;
    }

    if(name.equalsIgnoreCase("Title")&&value!=null){
       title= value;
    }
  }

  if (geo!=null && department!=null && title!=null ) {
      resultBean.setResultString(geo+";"+department+";"+title);
  } else {
      resultBean.setResultString("root");
  }
```
