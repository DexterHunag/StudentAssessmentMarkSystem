/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package studentmanagement2.JSON;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import studentmanagement2.Debug;

/**
 *
 * @author Dexter Huang (Huang Ching)
 */
public class ClassSerializer {

    public static String toJSON(Object o) {
        String str = "{ ";
        try {
            Field[] fields = o.getClass().getDeclaredFields();
            int count = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(o);
                str += name + ": " + objectToJSONValue(value);
                if (count < fields.length) {
                    str += ", ";
                }
                count++;
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ClassSerializer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ClassSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        str += "} ";
        return str;
    }

    public static <T> T fromJSON(Class<T> Class, String jsonString) {
        Object o = null;
        try {
            o = Class.newInstance();
            Field[] fields = Class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                JsonObject jo = new JsonObject(jsonString);
                Object value = jo.getValue(field.getName());
                Object co = field.get(o);
                Class dc = field.getDeclaringClass();
                if (value != null) {
                    if (co instanceof List) {
                        ParameterizedType listType = (ParameterizedType) field.getGenericType();
                        Class c = (Class) listType.getActualTypeArguments()[0];
                        field.set(o, jo.getList(c, field.getName()));
                        //Debug.LogInfo(field.getName() + "'s " + co.toString() + " array to " + jo.getList(c, field.getName()) + "   json: " + value.toString());
                    } else if (co instanceof JsonObject) {
                        JsonObject inner = (JsonObject) o;
                        value = fromJSON(field.getDeclaringClass(), inner.toString());
                        field.set(o, value);
                    } else if (co instanceof HashMap) {
                        ParameterizedType listType = (ParameterizedType) field.getGenericType();
                        Class c = (Class) listType.getActualTypeArguments()[0];
                        Class c2 = (Class) listType.getActualTypeArguments()[1];
                        HashMap<?, ?> map = jo.getHashMap(c, c2, field.getName());
                        field.set(o, map);
                        //Debug.Log(field.getName() + " is hashmap: " + map.toString());
                    } else if (co instanceof String) {
                        field.set(o, value.toString());
                    } else if (co instanceof Float) {
                        field.set(o, Float.parseFloat(value.toString()));
                    } else if (co instanceof Double) {
                        field.set(o, Double.parseDouble(value.toString()));
                    } else if (co instanceof Integer) {
                        field.set(o, Integer.parseInt(value.toString()));
                    } else {
                        field.set(o, value.toString());
                    }
                } else if (co instanceof List) {
                    Debug.LogError("cannot find " + field.getName());
                    field.set(o, new ArrayList<Object>());
                } else if (co instanceof HashMap) {
                    Debug.LogError("cannot find " + field.getName());
                    field.set(o, new HashMap<Object, Object>());
                } else {
                    Debug.LogError("cannot find " + field.getName());
                }
            }
        } catch (InstantiationException ex) {
            Logger.getLogger(ClassSerializer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ClassSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (T) o;
    }

    private static String keyPairsToJSON(KeyPairValue[] pairs) {
        String str = "{ ";
        int count = 1;
        for (KeyPairValue pair : pairs) {
            str += "\"" + pair.getKey() + "\": " + objectToJSONValue(pair.getValue());
            if (count < pairs.length) {
                str += ", ";
            }
        }
        str += "} ";
        return str;
    }

    public static String objectToJSONValue(Object object) {
        if (object instanceof String) {
            return "\"" + (String) object + "\"";
        } else if (object instanceof Integer || object instanceof Double || object instanceof Float) {
            return object + "";
        } else if (object instanceof HashMap) {
            HashMap<?, ?> map = (HashMap<?, ?>) object;
            KeyPairValue[] pairs = hashMapToKeyPairs(map);
            return keyPairsToJSON(pairs);
        } else if (object instanceof Collection) {
            Collection c = (Collection) object;
            return collectionToJSON(c);
        } else if (object instanceof JsonObject || object instanceof KeyPairValue) {
            return object.toString();
        } else if (object == null) {
            return "Null";
        } else {
            return toJSON(object);
        }
    }

    private static String collectionToJSON(Collection c) {
        String str = "{ ";
        int count = 1;
        for (Object o : c) {
            str += objectToJSONValue(o);
            if (count < c.size()) {
                str += ", ";
            }
            count++;
        }
        str += "} ";
        return str;
    }

    private static KeyPairValue[] hashMapToKeyPairs(HashMap<?, ?> map) {
        KeyPairValue[] pairs = new KeyPairValue[map.keySet().size()];
        int index = 0;
        for (Object ko : map.keySet()) {
            Object vo = map.get(ko);
            String key = ko.toString();
            KeyPairValue kpv = new KeyPairValue(key, vo);
            pairs[index] = kpv;
            index++;
        }
        return pairs;
    }
}
