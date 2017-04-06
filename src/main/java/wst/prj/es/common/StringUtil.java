package wst.prj.es.common;/**
 * Created by shuting.wu on 2017/4/5.
 */

import net.sf.json.JSONArray;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import wst.prj.es.pojo.QueryParam;

import java.lang.reflect.Array;
import java.util.List;

/**
 * @author shuting.wu
 * @date 2017-04-05 13:53
 **/
public class StringUtil {
    public static ObjectMapper objectMapper;

    public static String arrayToString(List<Object> list) {
        StringBuffer result = null;
        for(Object o: list) {
            if(result != null) {
                result.append(" " + o.toString());
            } else{
                result = new StringBuffer(o.toString());
            }

        }
        return result.toString();
    }

    /**
     * @Descrption jsonStr转对象
     * @param params
     * @return
     * @author shuting.wu
     * @date 2017/4/6 14:48
    **/
    public static <T> T jsonToList(String jsonStr, TypeReference<T> valueTypeRef) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        try {
            return objectMapper.readValue(jsonStr, valueTypeRef);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
